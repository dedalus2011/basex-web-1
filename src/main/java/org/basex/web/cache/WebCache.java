package org.basex.web.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

/**
 * Memcache Facade.
 * @author Michael Seiferle, BaseX Team
 * @author Dirk Kirsten
 * @author Sudarshan Acharyam
 * http://sacharya.com/using-memcached-with-java/
 */
public final class WebCache {

  /** Number of cache instances. */
  private static final int NUM_CONN = 31;

  /** Cache items namespace. */
  private static final String NAMESPACE = "basex:";
  
  /** memcached IP address. */
  private static final String MEMCACHED_IP = "127.0.0.1";
  
  /** memcached port. */
  private static final String MEMCACHED_PORT = "11211";
  
  /** standard time-to-live for cache values */
  private static final int STANDARD_TTL = 3600;

  /** The instance. */
  private static WebCache instance;

  /** Memcache clients. */
  private static MemcachedClient[] m;

  /**
   * Sets up a connection pool of initial clients.
   */
  private WebCache() {

    try {
      m = new MemcachedClient[WebCache.NUM_CONN];
      for(int i = 0; i < WebCache.NUM_CONN; i++) {
        MemcachedClient c = new MemcachedClient(new BinaryConnectionFactory(),
            AddrUtil.getAddresses(MEMCACHED_IP + ":" + MEMCACHED_PORT));
        m[i] = c;
      }
    } catch(Exception e) { }
  }

  /**
   * Factory method.
   * @return cache instance.
   */
  public static synchronized WebCache getInstance() {
    // System.out.println("Instance: " + instance);
    if(instance == null) {
      System.out.println("Creating a new instance");
      instance = new WebCache();
    }
    return instance;
  }

  /**
   * Sets an Item.
   * @param key the key
   * @param o object to set
   */
  public void set(final String key, final Object o) {
    set(key, STANDARD_TTL, o);
  }
  
  /**
   * Sets an Item.
   * @param key the key
   * @param ttl time to live in seconds
   * @param o object to set
   */
  public void set(final String key, final int ttl, final Object o) {
    System.out.println("Set for KEY " + NAMESPACE + key + ", LENGTH = " + key.length());
    getCache().set(NAMESPACE + key, ttl, o);
  }

  /**
   * Gets an item from the cache.
   * @param key the key
   * @return the cached object
   */
  public Object get(final String key) {
    System.out.println("key LENGTH = " + key.length());
    final Object o = getCache().get(NAMESPACE + key);
    if(o == null) {
      System.out.println("Cache MISS for KEY: " + NAMESPACE + key);
    } else {
      System.out.println("Cache HIT for KEY: " + NAMESPACE + key);
    }
    return o;
  }

  /**
   * Removes the object <code>key</code> from the cache pool.
   * @param key the key
   * @return the object that has been just deleted.
   */
  public Object delete(final String key) {
    return getCache().delete(NAMESPACE + key);
  }
  /**
   * Flushes memcached and removes all objects.
   */
  public void flushAll() {
    System.out.println("Flushing caches");
    getCache().flush();
    return;
  }

  /**
   * Gets an cache instance.
   * @return a cache client.
   */
  public MemcachedClient getCache() {
    MemcachedClient c = null;
    try {
      int i = (int) (Math.random() * (NUM_CONN - 1));
      c = m[i];
    } catch(Exception e) { }
    return c;
  }
}
