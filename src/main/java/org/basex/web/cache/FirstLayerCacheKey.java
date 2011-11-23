package org.basex.web.cache;

import java.io.File;
import java.io.IOException;

public class FirstLayerCacheKey implements CacheKeyInterface {
  private final File file; 
  private final String get;
  private final String post;
  
  public FirstLayerCacheKey(final File file, final String get, final String post) {
      this.file = file;
      this.get = get;
      this.post = post;
  }
  
  @Override
  public String getUniqueKey() throws IOException {
    return file.getCanonicalPath() + get + post;
  }

  @Override
  public String getPlainKey(int position) throws IOException {
    return file.getCanonicalPath() + get + post + position;
  }
}
