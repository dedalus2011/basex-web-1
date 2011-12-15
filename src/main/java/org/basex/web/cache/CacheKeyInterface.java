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
  /**
   * Each key can be split up as two different parts. This is the first and more general part.
   * 
   * @return String representation of the first part of the key
   */
  public String getFirstPart();
  /**
   * Each key can be split up as two different parts. This is the latter part and more specialised.
   * 
   * @return String representation of the second part of the key
   */
  public String getSecondPart();
  /**
   * Get the whole key
   * 
   * @return String representation of the whole key
   */
  public String getKeyString();
}
