package org.basex.web.cache;

import java.io.File;
import java.io.IOException;

public class CacheKey {
  private static final int KEY_SIZE = 200;
  private static final String DELIMITER = "|";    //TODO CAUTION: when changes, a change in get() is required
  private int position;
  private int hashPosition;
  private String hash;
  private CacheKeyInterface key;

  
  /**
   * @param file
   * @param get
   * @param post
   */
  public CacheKey(CacheKeyInterface key) {
    this.key = key;
    this.position = 0;
    this.hash = null;
  }
  
  private String getMemcachedHash() throws IOException {
    if (hash == null || position != hashPosition) {
      String plainString = key.getPlainKey(position);
      byte[] plain = plainString.getBytes();
      int hashLength = Math.min(KEY_SIZE, plain.length);
      byte[] hashByte = new byte[hashLength];
      
      //TODO really needed?!?
      for (int i = 0; i < hashLength; ++i)
        hashByte[i] = 0;
      
      for (int i = 0; i < plain.length; i = i + KEY_SIZE) {
        for (int j = 0; j < Math.min(hashLength, plain.length - i); ++j)
          hashByte[j] = (byte) (hashByte[j] ^ plain[i + j]);
      }
      
      //TODO this is totally stupid, because Java can not handle unsigned types and spymemcached just accepts
      // true string values
      for (int i = 0; i < hashLength; ++i) {
        if (hashByte[i] < 0)
          hashByte[i] *= -1;
        else if (hashByte[i] == 0 || hashByte[i] == 10 || hashByte[i] == 13 || hashByte[i] == 32)
          hashByte[i] += 1;
      }
      
      hash = new String(hashByte);
      hashPosition = position;
    }
    return this.hash;
  }
  
  private void collision() {
    ++position;
  }
  
  public Object get(WebCache cache) throws IOException {
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
  
  public void set(String content, WebCache cache) throws IOException {
    String cacheContent;
    
    do {
      cacheContent = (String) cache.get(getMemcachedHash());
      collision();
    } while (cacheContent != null);
    
    --position;
    
    cache.set(getMemcachedHash(), key.getUniqueKey() + DELIMITER + content);
  }
}
