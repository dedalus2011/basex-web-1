package org.basex.web.cache;

import java.io.File;
import java.io.IOException;

public class SecondLayerCacheKey implements CacheKeyInterface {
  private final String query; 
  private final String get;
  private final String post;
  
  public SecondLayerCacheKey(final String query, final String get, final String post) {
      this.query = query;
      this.get = get;
      this.post = post;
  }
  
  @Override
  public String getUniqueKey() throws IOException {
    return query + get + post;
  }

  @Override
  public String getPlainKey(int position) throws IOException {
    return query + get + post + position;
  }
}
