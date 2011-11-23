package org.basex.web.cache;

import java.io.File;
import java.io.IOException;

public class CacheKey {
  private static final int KEY_SIZE = 200;
  private static final String DELIMITER = "|";
  private final File file; 
  private final String get;
  private final String post;
  private String hash;
  private int position;
  private int hashPosition;
  
  /**
   * @param file
   * @param get
   * @param post
   */
  public CacheKey(final File f, final String get, final String post) {
    this.file = f;
    this.get = get;
    this.post = post;
    this.position = 0;
    this.hash = null;
  }
  
  /**
   * @return
   */
  public String getMemcachedHash() throws IOException {
    if (hash == null || position != hashPosition) {
      String plainString = file.getCanonicalPath() + get + post + position;
      byte[] plain = plainString.getBytes();
      int hashLength = Math.min(KEY_SIZE, plain.length);
      byte[] hash = new byte[hashLength];
      
      //TODO really needed?!?
      for (int i = 0; i < hashLength; ++i)
         hash[i] = 0;
      
      for (int i = 0; i < plain.length; i = i + KEY_SIZE) {
        for (int j = 0; j < hashLength; ++j)
          hash[j] = (byte) (hash[j] ^ plain[i * KEY_SIZE + j]);
      }
      
      this.hash = new String(hash);
      this.hashPosition = position;
    }
    
    return this.hash;
  }
  
  public String getUniqueKey() throws IOException {
    return file.getCanonicalPath() + get + post;
  }
  
  public void collision() {
    ++position;
  }
  
  public Object get(WebCache cache) throws IOException {
    String key;
    String[] s;
    String cacheContent;
    
    do {
      cacheContent = (String) cache.get(getMemcachedHash());
      if (cacheContent == null)
        return null;
      
      s = cacheContent.split("\\|", 2);
      key = s[0];
      collision();
    } while (!key.equals(getUniqueKey()));
    
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
    
    cache.set(getMemcachedHash(), getUniqueKey() + DELIMITER + content);
  }
}
