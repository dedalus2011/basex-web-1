package org.basex.web.cache;

/**
 * The key for the second cache layer on a xq-level.
 * 
 * @author Dirk Kirsten
 *
 */
public class SecondLayerCacheKey implements CacheKeyInterface {
  /** The query to be cached */
  private final String query; 
  /** GET parameters of the request */
  private final String get;
  /** POST parameters of the request */
  private final String post;
  
  /**
   * @param q The query to be cached
   * @param g GET parameters of the request
   * @param p POST parameters of the request
   */
  public SecondLayerCacheKey(final String q, final String g, final String p) {
      query = q;
      get = g;
      post = p;
  }

  @Override
  public String getFirstPart() {
    return query;
  }

  @Override
  public String getSecondPart() {
    return get + post;
  }
  
  @Override
  public String getKeyString() {
    return getFirstPart() + getSecondPart();
  }
}
