package org.basex.web.servlet.impl;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.io.IOFile;
import org.basex.io.in.TextInput;
import org.basex.web.cache.CacheKey;
import org.basex.web.cache.WebCache;
import org.basex.web.servlet.PrepareParamsServlet;
import org.basex.web.servlet.util.ResultPage;
import org.basex.web.xquery.BaseXContext;
import org.eclipse.jetty.http.HttpException;

import com.google.common.base.Objects;

/**
 * Handles all that fancy MVC stuff :-).
 * @author michael
 *
 */
public class Xails extends PrepareParamsServlet {

  /** Caching enabled/disabled */
  private static final boolean CACHING = true;
  /** XQuery controllers/action.xq in charge. */
  private File view;
  /** XQuery controllers/action.xq in charge. */
  private File controller;

  /** Current Page Buffer. **/
//  private StringBuilder pageBuffer;

  @Override
  public void get(final HttpServletResponse resp,
      final HttpServletRequest req,
      final File f, final String get, final String post) throws IOException {

    init(req);
    
    if (CACHING) {
      CacheKey cacheKey = new CacheKey(f, get, post);
      WebCache cache = WebCache.getInstance();
      Object cacheObject = cacheKey.get(cache);
      if (cacheObject != null && cacheObject instanceof String) {
        // cache hit
        resp.getWriter().write((String) cacheObject);
      } else {
        // cache miss
        String content = buildContent(resp, req, f, get, post);
        cacheKey.set(content, cache);
        resp.getWriter().write(content);
      }
    } else {
      resp.getWriter().write(buildContent(resp, req, f, get, post));
    }
  }
  
  /**
   * Builds the response content
   * 
   * @param resp the response
   * @param req request reference
   * @param f the requested file
   * @param get get variables Map
   * @param post post variables Map
   * @return the content string
   * @throws IOException on error
   */
  private String buildContent(final HttpServletResponse resp,
      final HttpServletRequest req,
      final File f, final String get, final String post) throws IOException {
    final StringBuilder pageBuffer = new StringBuilder(256);
    
    final String file = req.getHeader("X-Requested-With") != null ?
        "/layouts/ajax.html" : "/layouts/default.html";
    fillPageBuffer(pageBuffer, file);

    final String queryResult = buildResult(resp, req, get, post);
    assert null != queryResult;
    resp.setContentType("application/xml");
    resp.setCharacterEncoding("UTF-8");
    if(!resp.containsHeader("Location"))
      resp.setStatus(HttpServletResponse.SC_OK);
    return pageBuffer.toString().replace("{{$content}}", queryResult);
  }
  
  /**
   * Builds the resulting XQuery file in memory and evaluates the result.
   * @param resp the response
   * @param req request reference
   * @param get get variables Map
   * @param post post variables Map
   * @return the evaluated result
   * @throws IOException on error.
   */
  private String buildResult(final HttpServletResponse resp,
      final HttpServletRequest req, final String get, final String post)
          throws IOException {
    final StringBuilder qry = prepareQuery();
    qry.append(TextInput.content(new IOFile(view)).toString());
    final ResultPage queryResult = BaseXContext.exec(qry.toString(), get, post,
        resp, req);
    return queryResult.getBody();
  }
  
  /**
   * Adds the controller import to the view file.
   * @return controller import String
   * @throws IOException if file not found.
   */
  private StringBuilder prepareQuery() throws IOException {
    final StringBuilder qry = new StringBuilder(128);
    if(controller == null) return qry;
    final String controllername = dbname(controller.getName());

    qry.append(String.format("import module namespace "
        + "%s=\"http://www.basex.org/myapp/%s\" " + "at \"%s\";\n",
        controllername,
        controllername,
        controller.getCanonicalPath()));
    return qry;
  }
  /**
   * Gets only the filename without suffix.
   * @param n filename
   * @return chopped filename.
   */
  public final String dbname(final String n) {
    final int i = n.lastIndexOf(".");
    return (i != -1 ? n.substring(0, i) : n).replaceAll("[^\\w-]", "");
  }


  /**
   * Sets the controller and action based on the sent request.
   * Initializes the current Page Buffer.
   * @param req request.
   * @throws HttpException on error
   */
  private void init(final HttpServletRequest req) throws HttpException {
    final String cntr = Objects.firstNonNull(
        req.getAttribute("xails.controller"), "page").toString();
    assert null != cntr : "Error no controller set";

    final String ac = Objects.firstNonNull(req.getAttribute("xails.action"),
        "index").toString();
    assert null != ac : "Error no action set";

    final String vpath = String.format("views/%s/%s.xq",
        cntr,
        ac
        );
    final String cpath = String.format("controllers/%s.xq",
        cntr
    );

    try {
      controller = super.requestedFile(cpath);
    } catch(HttpException e) { }
    view = super.requestedFile(vpath);
  }

  /**
   * Reads the layout into the page Buffer.
   * @param pb StrinBuilder object containing the page's content.
   * @param file layout to fill
   * @throws IOException ex
   */
  private void fillPageBuffer(final StringBuilder pb, 
      final String file) throws IOException {
    pb.append(
        TextInput.content(new IOFile(fPath +
            file)).toString());
  }
}
