package org.basex.web.cache;

import java.util.Map;

import voldemort.client.ClientConfig;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.SocketStoreClientFactory;


/**
 * Voldemort facade
 * 
 * @author Dirk Kirsten
 */
public final class WebCache {
  /** Cache items namespace. */
  private static final String NAMESPACE = "basex-web";
  
  /** voldemort IP address. */
  private static final String VOLDEMORT_IP = "127.0.0.1";
  
  /** voldemort port. */
  private static final String VOLDEMORT_PORT = "6667";

  /** The instance. */
  private static WebCache instance;

  /** Voldemort client. */
  private static StoreClient<String, Map<String, String>> c;

  /**
   * Sets up a single client.
   */
  private WebCache() {
    String bootstrapUrl = "tcp://" + VOLDEMORT_IP + ":" + VOLDEMORT_PORT;
    StoreClientFactory factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(bootstrapUrl));
    c = factory.getStoreClient(NAMESPACE);
  }

  /**
   * Factory method.
   * @return cache instance.
   */
  public static synchronized WebCache getInstance() {
    if (instance == null) {
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
  public void set(final CacheKeyInterface key, final String o) {
    Map <String, String> map = getFirst(key);
    map.put(key.secondPart(), o);
    c.put(key.firstPart(), map);
  }
  
  /**
   * Sets an Item.
   * @param key the key
   * @param o object to set
   */
  public void set(final CacheKeyInterface key, final Map<String, String> o) {
    c.put(key.firstPart(), o);
  }

  /**
   * Gets an item from the cache.
   * @param key the key
   * @return the cached object
   */
  public Map<String, String> getFirst(final CacheKeyInterface key) {
    final Map<String, String> o = c.getValue(key.firstPart());
    if(o == null) {
      System.out.println("Cache MISS for KEY: " + key);
    } else {
      System.out.println("Cache HIT for KEY: " + key);
    }
    return o;
  }

  /**
   * Gets an item from the cache.
   * @param key the key
   * @return the cached object
   */
  public String get(final CacheKeyInterface key) {
    String o;
    final Map<String, String> map = c.getValue(key.firstPart());
    if (map == null) {
      System.out.println("Cache MISS for KEY: " + key);
      o = null;
    } else {
      o = map.get(key.secondPart());
      if (o == null) {
        System.out.println("Cache MISS for KEY: " + key);
      } else {
        System.out.println("Cache HIT for KEY: " + key);
      }
    }
    return o;
  }
  
  /**
   * Removes the object <code>key</code> from the cache pool.
   * @param key the key
   * @return the object that has been just deleted.
   */
  public boolean deleteAll(final CacheKeyInterface key) {
    return c.delete(key.firstPart());
  }
  
  /**
   * Removes the object <code>key</code> from the cache pool.
   * @param key the key
   * @return the object that has been just deleted.
   */
  public Object delete(final CacheKeyInterface key) {
    Map<String, String> map = c.getValue(key.firstPart());
    boolean del = map.remove(key.secondPart()) != null;
    set(key, map);
    return del;
  }
  
  /**
   * Flushes voldemort and removes all objects.
   */
  public void flushAll() {
    /*TODO */
    return;
  }

  /**
   * Gets an cache instance.
   * @return a cache client.
   */
  public StoreClient<String, Map<String, String>> getCache() {
    return c;
  }
}
