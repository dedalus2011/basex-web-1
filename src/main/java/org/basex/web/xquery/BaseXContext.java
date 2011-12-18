package org.basex.web.xquery;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.core.BaseXException;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.web.cache.CacheKey;
import org.basex.web.cache.SecondLayerCacheKey;
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
    
  /** Enabled/disable caching */
  private final static boolean CACHING = true;

  /** Do not construct me. */
  private BaseXContext() { /* void */}
  
  /**
   * This Method reads and returns the result of a whole query.
   * @param qu the query string
   * @param get GET in JSON representation
   * @param post POST in JSON representation
   * @param resp response object
   * @param req the request object
   * @param doCache should this element be cached?
   * @return the query result
   * @throws IOException exception
   */
  public static ResultPage query(final String qu, final String get,
      final String post, final HttpServletResponse resp, 
      final HttpServletRequest req, final boolean doCache)
      throws IOException {

    setReqResp(resp, req);
    
    resultPage.get().setBody(exec(qu, get, post, resp, req, doCache));
    
    return resultPage.get();
  }

  /** 
   * Executes a query string.
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @param doCache should this element be cached?
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized String exec(final String qu, final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req, final boolean doCache) throws IOException {
    if (CACHING && doCache) {
      
       CacheKey cacheKey = new CacheKey(new SecondLayerCacheKey(qu, get, post));
     
      
      Object cacheObject = cacheKey.get();
      if (cacheObject != null && cacheObject instanceof String) {
        // cache hit
        return execCache(get, post, resp, req, (String) cacheObject);
      }
      // cache miss
      String content = runQuery(qu, get, post, resp, req);
      cacheKey.set(content);
      return execCache(get, post, resp, req, content);
    }
    
    return execGeneric(qu, get, post, resp, req, null);
  }
  
  /** 
   * Executes a query string.
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @param cacheContent The value of this cache key
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized String execCache(final String get,
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
   * @param cacheContent The value of this cache key
   * @return the query result.
   * @throws IOException on error
   */
  public static synchronized String execGeneric(final String qu, final String get,
      final String post, final HttpServletResponse resp,
      final HttpServletRequest req, final String cacheContent) throws IOException {
    String ret;
    
    
    if (cacheContent == null) {
      ret = runQuery(qu, get, post, resp, req);
    } else {
      ret = cacheContent;
    }
      
    assert null != ret : "Query Result must not be ''"; 
    return ret;
  
  }
  
  /**
   * Runs the query on the BaseX database.
   * 
   * @param qu query string
   * @param get GET map
   * @param post POST map
   * @param resp response object
   * @param req request object
   * @return the query result.
   * @throws IOException on error
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
      return err(e);
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
   * Returns a HTML string containing an Error Message
   * 
   * @param e error
   * @return String with error message
   */
  private static String err(Exception e) {
    return "<div class=\"error\">" + e.getMessage() + "</div>";
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