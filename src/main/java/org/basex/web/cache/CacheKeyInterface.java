package org.basex.web.cache;

import java.io.IOException;

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
   * @param position used for collision detection. When the hash collides, the position-counter is incremented
   * @return Returns the full plain key
   * @throws IOException A key may depend on some file(s), if anything is wrong there IOException is thrown
   */
  public String getPlainKey(int position) throws IOException;
  /**
   * @return Returns the unique key used for memcached, as memcached does not handle collisions
   * @throws IOException A key may depend on some file(s), if anything is wrong there IOException is thrown
   */
  public String getUniqueKey() throws IOException;
}
