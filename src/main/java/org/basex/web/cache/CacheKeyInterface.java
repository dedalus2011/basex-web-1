package org.basex.web.cache;

/**
 * Interface for the various different keys for the web cache. As we cache
 * on different levels, also the key may differ. At the moment we support to cache levels,
 * but several more levels (also parallel) may be added with this interface.
 * 
 * @author Dirk Kirsten
 *
 */
public interface CacheKeyInterface {
  public String firstPart();
  public String secondPart();
}
