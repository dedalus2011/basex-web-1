package org.basex.web.cache;
import java.io.IOException;

import org.basex.web.cache.BaseXClient;
import org.basex.web.servlet.impl.Xails;
import org.basex.web.session.SessionFactory;
import org.basex.server.Session;
/**
 * @author Robert
 *
 */
public final class InvalidateCache {

  /**
   * 
   */
  private InvalidateCache() { }

  /**
   * 
   */
  private static InvalidateCache instance;


  /**
   * @return the String to be invalidated
   */

  public static synchronized String getinvalidatedString () {
    return Xails.tobeinvalidated;
  }

  /**
   * @return invalidation instance
   * 
   */
  public static synchronized InvalidateCache getInstance() {
    // System.out.println("Instance: " + instance);
    if(instance == null) {
      System.out.println("Creating a new invalidation instance");
      instance = new InvalidateCache();
    }
    return instance;
  }
  /**
   * Main method.
   * 
   */

  public void invalidatecache () {
    try {
      final BaseXClient clientsession =
          new BaseXClient("localhost", 1984, "admin", "admin");

      final Session serversession = SessionFactory.get(); 

      serversession.execute("create event invalidate");
      clientsession.watch("invalidate",  new Notifier());

      serversession.query("db:event('invalidate', "+getinvalidatedString()+")").execute();

      clientsession.unwatch("invalidate");
      serversession.execute("drop event invalidate");
      serversession.close();
      clientsession.close();

    } catch(final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Implementation of the event notifier interface.
   */
  private static class Notifier implements BaseXClient.EventNotifier {
    /** Constructor. */
    public Notifier() { }

    @Override
    public void notifyy(final String value) {
      
     // System.out.println("Message received: " + value);
        String valuereceived = value;
 //todo      // if (valuereceived.equals(CacheKey key)) {
         //   CacheKey.invalidate();
    }

  }

}
