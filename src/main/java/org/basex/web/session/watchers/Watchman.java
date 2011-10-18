package org.basex.web.session.watchers;

import org.basex.core.BaseXException;
import org.basex.core.cmd.XQuery;
import org.basex.server.EventNotifier;
import org.basex.util.Util;
import org.basex.web.session.commands.Command;

/**
 * Watches for any webserver related events
 * @author Michael Seiferle, <ms@basex.org>
 *
 */
public class Watchman implements EventNotifier {
  
  /** What to do? */
  private enum TODO {
    /** flash*/
    FLASH,
    /** set-cookie*/
    SETCOOKIE,
    /** get cookie */
    GETCOOKIE
    
    
  };
  @Override
  public void notify(String value) {
//    System.out.println("Receiving " + value);
    try {
      TODO valueOf = TODO.valueOf(new XQuery(String.format("string(%s/@name)", value)).
          execute(Command.CTX));
      switch(valueOf){
        case FLASH: /* */; break;
        case SETCOOKIE: /* */; break;
        case GETCOOKIE: /* */; break;
        default: Util.notexpected("This action can not be watched");
      }
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      Util.notexpected(e);
    }
    }
  
}
