package org.basex.web.cache;

import java.io.IOException;

/**
 * Provides all necessary methods to access the keys/values from within memcached. 
 * 
 * @author Dirk Kirsten
 *
 */
public class CacheKey {
  /** The maximum size of the key. Memcached keys are limited to 256 byte and we do use a Namespace */
  private static final int KEY_SIZE = 50;
  /** A delimiter to separate the real key and the value in the memcached value */
  private static final String DELIMITER = "|";    //TODO CAUTION: when changes, a change in get() is required
  /** The number of collisions already occurred */
  private int position;
  /** The number of collisions which occurred on the last time the hash was calculated */
  private int hashPosition;
  /** stores the hash to prevent recomputation */
  private String hash;
  /** The actual key we use */
  private CacheKeyInterface key;

  /**
   * @param k The actual used key
   */
  public CacheKey(CacheKeyInterface k) {
    this.key = k;
    this.position = 0;
    this.hash = null;
  }
  
  /**
   * @return Calculates a hash which is not longer than KEY_SIZE
   * @throws IOException Some keys may access files and if something goes wrong there, this error is thrown.
   */
  private String getMemcachedHash() throws IOException {
    if (hash == null || position != hashPosition) {
      String plainString = key.getPlainKey(position);
      byte[] plain = plainString.getBytes();
      int hashLength = Math.min(KEY_SIZE, plain.length);
      char[] hashByte = new char[hashLength];
      
      //TODO really needed?!?
      for (int i = 0; i < hashLength; ++i)
        hashByte[i] = 0;
      
      for (int i = 0; i < plain.length; i = i + KEY_SIZE) {
        for (int j = 0; j < Math.min(hashLength, plain.length - i); ++j)
          hashByte[j] = (char) (hashByte[j] ^ plain[i + j]);
      }
      
      //TODO this is totally stupid, because Java can not handle unsigned types and spymemcached just accepts
      // true string values
      for (int i = 0; i < hashLength; ++i) {
        if (hashByte[i] == 32 || hashByte[i] == 0 || hashByte[i] == 11 || hashByte[i] == 13)
          hashByte[i] += 1;
      }
      
      hash = new String(hashByte);
      hashPosition = position;
    }
    return this.hash;
  }
  
  /**
   * Two hashs had a collision
   */
  private void collision() {
    ++position;
  }
  
  /**
   * Get the cache value of this key.
   * 
   * @return Returns the memcached value for the given key
   * @throws IOException i/o error
   */
  public Object get() throws IOException {
    WebCache cache = WebCache.getInstance();
    String keyString;
    String[] s;
    String cacheContent;
    
    do {
      cacheContent = (String) cache.get(getMemcachedHash());
      if (cacheContent == null)
        return null;
      
      s = cacheContent.split("\\|", 2);
      keyString = s[0];
      collision();
    } while (!keyString.equals(key.getUniqueKey()));
    
    --position;
    return s[1];
  }
  
  /**
   * Sets a new cache value for this key.
   * 
   * @param content Sets this content for the specified key in memcached
   * @throws IOException i/o error
   */
  public void set(String content) throws IOException {
    WebCache cache = WebCache.getInstance();
    String cacheContent;
    
    do {
      cacheContent = (String) cache.get(getMemcachedHash());
      collision();
    } while (cacheContent != null);
    
    --position;
    
    cache.set(getMemcachedHash(), key.getUniqueKey() + DELIMITER + content);
  }
  
  /**
   * invalidates the given key if existing in memcached
   * 
   * @throws IOException i/o error
   */
  public void invalidate() throws IOException {
    WebCache cache = WebCache.getInstance();
    if (get() != null)
      cache.delete(key.getUniqueKey());
  }
}
