package org.basex.web.cache;

import java.io.IOException;

/**
 * Provides all necessary methods to access the keys/values from within memcached. 
 * 
 * @author Dirk Kirsten
 *
 */
public class CacheKey {
  /** The actual key we use */
  private CacheKeyInterface key;

  /**
   * @param k The actual used key
   */
  public CacheKey(CacheKeyInterface k) {
    this.key = k;
  }
  
  /**
   * Get the cache value of this key.
   * 
   * @return Returns the memcached value for the given key
   * @throws IOException i/o error
   */
  public String get() throws IOException {
    WebCache cache = WebCache.getInstance();
    
    return cache.get(key);
  }
  
  /**
   * Sets a new cache value for this key.
   * 
   * @param content Sets this content for the specified key in memcached
   * @throws IOException i/o error
   */
  public void set(String content) throws IOException {
    WebCache cache = WebCache.getInstance();

    cache.set(key, content);
  }
  
  /**
   * invalidates the given key if existing in memcached
   * 
   * @throws IOException i/o error
   */
  public void invalidate() throws IOException {
    WebCache cache = WebCache.getInstance();
    if (get() != null)
      cache.deleteAll(key);
  }
}
