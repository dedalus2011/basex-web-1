package org.basex.web.cache;

import java.io.File;
import java.io.IOException;

/**
 * Is a key for the first layer (before the actual file system access)
 * 
 * @author Dirk Kirsten
 *
 */
public class FirstLayerCacheKey implements CacheKeyInterface {
  /** The file which is called */
  private final File file; 
  /** GET parameters of the request */
  private final String get;
  /** POST parameters of the request */
  private final String post;
  
  /**
   * @param f The requested file
   * @param g GET parameters of the request
   * @param p POST parameters of the request
   */
  public FirstLayerCacheKey(final File f, final String g, final String p) {
      file = f;
      get = g;
      post = p;
  }

  @Override
  public String firstPart() {
    try {
      return file.getCanonicalPath();
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String secondPart() {
    return get + post;
  }
}
