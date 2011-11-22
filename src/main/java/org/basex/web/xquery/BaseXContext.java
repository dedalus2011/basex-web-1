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
import org.basex.web.cache.CacheKey;
import org.basex.web.cache.WebCache;
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
    
  private final static boolean CACHING = true;

  /** Do not construct me. */
  private BaseXContext() { /* void */}
  
  /**
   * This Method reads and returns the result of a whole query.
   * @param f the filename
   * @param get GET in JSON representation
   * @param post POST in JSON representation
   * @param resp response object
   * @param req the request object
   * @return the query result
   * @throws IOException exception
   */
  public static ResultPage query(final File f, final String get,
      final String post,
      final HttpServletResponse resp, final HttpServletRequest req)
      throws IOException {
    if (CACHING) {
      CacheKey cacheKey = new CacheKey(f, get, post);
      WebCache cache = WebCache.getInstance();
      Object cacheObject = cacheKey.get(cache);
      if (cacheObject != null && cacheObject instanceof String) {
        // cache hit
        return execCache(get, post, resp, req, (String) cacheObject);
      }
      // cache miss
      String content = runQuery(TextInput.content(new IOFile(f)).toString(), get, post, resp, req);
      cacheKey.set(content, cache);
      return execCache(get, post, resp, req, content);
    }
    
    return exec(TextInput.content(new IOFile(f)).toString(), get, post, resp, req);
  }

  /** 
   * Executes a query string.
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized ResultPage exec(final String qu, final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req) throws IOException {
    return execGeneric(qu, get, post, resp, req, null);
  }
  
  /** 
   * Executes a query string.
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized ResultPage execCache(final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req, final String cacheContent) throws IOException {
    return execGeneric(null, get, post, resp, req, cacheContent);
  }
  
  /** 
   * Executes a query string.
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized ResultPage execGeneric(final String qu, final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req, final String cacheContent) throws IOException {
    
    setReqResp(resp, req);
    
    if (cacheContent == null) {
      resultPage.get().setBody(runQuery(qu, get, post, resp, req));
    } else {
      resultPage.get().setBody(cacheContent);
    }
      
    assert null != resultPage.get().getBody() : "Query Result must not be ''"; 
    return resultPage.get();
  
  }
  
  /**
   * @param qu
   * @param get
   * @param post
   * @param resp
   * @param req
   * @return
   * @throws IOException
   */
  private static synchronized String runQuery(final String qu, final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req) throws IOException {
    setReqResp(resp, req);
    try {
      final Query q = session.query(qu);
      
      bind(get, post, req.getSession(true).getId(), q);
      
      return q.execute();
    } catch(BaseXException e) {
      return "<div class=\"error\">" + e.getMessage() + "</div>";
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