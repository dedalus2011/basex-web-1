/**
 * 
 */
package org.basex.web.session;

import org.basex.core.Context;
import org.basex.server.LocalSession;
import org.basex.server.Session;

/**
 * @author Michael Seiferle, <ms@basex.org>
 * @author BaseX Team
 * 
 */
public class SessionFactory {

  /** The server instance. */
//  @SuppressWarnings("unused")
//  private static final BaseXServer bsx;
//  static {
//    try {
//      bsx = new BaseXServer();
//    } catch(Exception e) {
//      // TODO Auto-generated catch block
//      throw Util.notexpected(e);
//    }
//        
//  }
  /** My Session. */
  private static final Session INSTANCE;
  static {
      INSTANCE = new LocalSession(new Context());
  }

  /**
   * The Session.
   * @return session singleton
   */
  public static Session get() {
    // TODO Auto-generated method stub
    return INSTANCE;
  }

}
