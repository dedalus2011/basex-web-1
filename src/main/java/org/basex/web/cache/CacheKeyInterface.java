package org.basex.web.cache;

import java.io.IOException;

public interface CacheKeyInterface {
  public String getPlainKey(int position) throws IOException;
  public String getUniqueKey() throws IOException;
}
