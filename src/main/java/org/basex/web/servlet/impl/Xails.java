package org.basex.web.servlet.impl;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.io.IOFile;
import org.basex.io.in.TextInput;
import org.basex.web.cache.CacheKey;
import org.basex.web.cache.FirstLayerCacheKey;
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

  /** Caching at first level enabled/disabled */
  private static final boolean CACHING_FIRSTLAYER = false;
  /** Caching at second level enabled/disabled */
  private static final boolean CACHING_SECONDLAYER = true;
  /** Max recursion depth for inclusion of xq-files */
  private static final int MAX_DEPTH = 3;
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
    
    if (CACHING_FIRSTLAYER) {
      CacheKey cacheKey = new CacheKey(new FirstLayerCacheKey(f, get, post));
      Object cacheObject = cacheKey.get();
      if (cacheObject != null && cacheObject instanceof String) {
        // cache hit
        resp.getWriter().write((String) cacheObject);
      } else {
        // cache miss
        String content = buildContent(resp, req, get, post);
        cacheKey.set(content);
        resp.getWriter().write(content);
      }
    } else {
      resp.getWriter().write(buildContent(resp, req, get, post));
    }
  }
  
  /**
   * Builds the response content
   * 
   * @param resp the response
   * @param req request reference
   * @param get get variables Map
   * @param post post variables Map
   * @return the content string
   * @throws IOException on error
   */
  private String buildContent(final HttpServletResponse resp,
      final HttpServletRequest req, final String get, final String post) throws IOException {
    final StringBuilder pageBuffer = new StringBuilder(256);
    
    final String file = req.getHeader("X-Requested-With") != null ?
        "/layouts/ajax.html" : "/layouts/default.html";
    fillPageBuffer(pageBuffer, file);

    final String queryResult = buildResult(view, resp, req, get, post, 0, CACHING_SECONDLAYER);
    assert null != queryResult;
    resp.setContentType("application/xml");
    resp.setCharacterEncoding("UTF-8");
    if(!resp.containsHeader("Location"))
      resp.setStatus(HttpServletResponse.SC_OK);
    return pageBuffer.toString().replace("{{$content}}", queryResult);
  }
  
  /**
   * Builds the resulting XQuery file in memory and evaluates the result.
   * 
   * @param f the file to be build
   * @param resp the response
   * @param req request reference
   * @param get get variables Map
   * @param post post variables Map
   * @param recursionDepth the current level of the recursion
   * @param doCache should this element be cached?
   * @return the evaluated result
   * @throws IOException on error.
   */
  private String buildResult(final File f, final HttpServletResponse resp,
      final HttpServletRequest req, final String get, final String post,
      final int recursionDepth, final boolean doCache)
          throws IOException {
    final StringBuilder qry = prepareQuery();
    String fileContent = TextInput.content(new IOFile(f)).toString();
    
    /* starts the recursive inclusive of xq-files */
    //TODO should include max recursive level to prevent loops
    Pattern incPattern = Pattern.compile("\\{\\{\\s*include(\\:no\\-cache|\\:cache)?\\s+src.*\\}\\}");
    Matcher incMatcher = incPattern.matcher(fileContent);
    while (incMatcher.find()) {
      String inc = fileContent.substring(incMatcher.start(), incMatcher.end());
      int startInc = incMatcher.start();
      int endInc = incMatcher.end();
      
      // does the user specify to not cache this object?
      boolean incDoCache = true;
      Pattern cachePattern = Pattern.compile("include\\:no\\-cache");
      Matcher cacheMatcher = cachePattern.matcher(inc);
      if (cacheMatcher.find()) 
        incDoCache = false;
      
      // get the file to be included
      Pattern srcPattern = Pattern.compile("\\ssrc\\s*=\\s*\"");
      Matcher srcMatcher = srcPattern.matcher(inc);
      srcMatcher.find();
      int startSrc = srcMatcher.end();
      srcPattern = Pattern.compile("\"");
      srcMatcher = srcPattern.matcher(inc.substring(startSrc + 1));
      srcMatcher.find();
      int endSrc = startSrc + srcMatcher.start() + 1;
      
      // replace the include string with the actual content
      File recFile = new File(f.getParent() + "/" + inc.substring(startSrc, endSrc));
      String recContent;
      if (recursionDepth < MAX_DEPTH)
        recContent = buildResult(recFile, resp, req, get, post, recursionDepth + 1, incDoCache);
      else
        recContent = "";
      fileContent = fileContent.substring(0, startInc) + recContent + fileContent.substring(endInc);
    }
    
    qry.append(fileContent);
    final ResultPage queryResult = BaseXContext.query(qry.toString(), get, post,
        resp, req, doCache);
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
