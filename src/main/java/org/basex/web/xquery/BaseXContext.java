package org.basex.web.xquery;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.core.BaseXException;
import org.basex.io.IOFile;
import org.basex.io.in.TextInput;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.web.servlet.util.ResultPage;
import org.basex.web.session.SessionFactory;

/**
 * Provides static methods to access BaseX.
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle <ms@basex.org>
 */
public final class BaseXContext {
  /** Thread local resultpage. */
  private final static ThreadLocal<ResultPage> resultPage =
      new ThreadLocal<ResultPage>() {
    @Override
    protected ResultPage initialValue() {
      return ResultPage.getEmpty();
    }
  };
  
  
  
  /** Context. */
//  private static final Context ctx = new Context();
  /** Session. */
//  final static LocalSession session = new LocalSession(new Context());
    final static Session session = SessionFactory.get(); 

  /** Do not construct me. */
  private BaseXContext() { /* void */}
  
  /**
   * This Method reads and returns the result of a whole query.
   * @param f the filename
   * @param get GET in JSON representation
   * @param post POST in JSON representation
   * @param re response object
   * @param rq the request object
   * @return the query result
   * @throws IOException exception
   */
  public static ResultPage query(final File f, final String get,
      final String post,
      final HttpServletResponse re, final HttpServletRequest rq)
      throws IOException {
    return exec(TextInput.content(new IOFile(f)).toString(), get, post, re, rq);
  }

  /** 
   * Executes a query string.
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param rp response object
   * @param rq request object
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized ResultPage exec(final String qu, final String get,
      final String post, final HttpServletResponse rp,
      final HttpServletRequest rq) throws IOException {
    
    setReqResp(rp, rq);
    try {
      final Query q = session.query(qu);
      
      bind(get, post, rq.getSession(true).getId(), q);
      
      resultPage.get().setBody(q.execute());
      assert null != resultPage.get().getBody() : "Query Result must not be ''"; 
      return resultPage.get();
    } catch(BaseXException e) {
        return err(rp, rq, e);
    }
  }

  /**
   * Set Request/Response.
   * @param rp response
   * @param rq request
   */
  private static void setReqResp(final HttpServletResponse rp,
      final HttpServletRequest rq) {
    resultPage.get().setReq(rq);
    resultPage.get().setResp(rp);
  }

  /**
   * Binds the GET & POST Parameters.
   * Binds the SESSION ID to $SESSION
   * @param get get
   * @param post post
   * @param sess session id
   * @param q the query
   * @throws IOException on error.
   */
  private static void bind(final String get, final String post,
      final String sess, final Query q)
      throws IOException {
    q.bind("SESSION", sess);
    q.bind("GET", get, "json");
    q.bind("POST", post, "json");
  }

  /**
   * Returns a ResultPage containing an Error Message
   * @param rp response
   * @param rq request
   * @param e error
   * @return ResultPage with error message
   */
  private static ResultPage err(final HttpServletResponse rp,
      final HttpServletRequest rq, Exception e) {
    return new ResultPage("<div class=\"error\">" + e.getMessage() + "</div>",
        rp, rq);
  }


  /**
   * Returns the response.
   * @return response
   */
  static HttpServletResponse getResp() {
    return resultPage.get().getResp();
  }

  /**
   * Returns the request object.
   * @return request
   */
  static HttpServletRequest getReq() {
    return resultPage.get().getReq();
  }

}