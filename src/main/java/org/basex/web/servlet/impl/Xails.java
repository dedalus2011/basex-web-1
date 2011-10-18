package org.basex.web.servlet.impl;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.io.IOFile;
import org.basex.io.in.TextInput;
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

  /** XQuery controllers/action.xq in charge. */
  private File view;
  /** XQuery controllers/action.xq in charge. */
  private File controller;

  /** Current Page Buffer. **/
//  private StringBuilder pageBuffer;

  @Override
  public void get(final HttpServletResponse response,
      final HttpServletRequest req,
      final File f, final String get, final String post) throws IOException {
    // TODO Auto-generated method stub
    final StringBuilder pageBuffer = new StringBuilder(256);

    init(req);

    final String file = req.getHeader("X-Requested-With") != null ?
        "/layouts/ajax.html" : "/layouts/default.html";
    fillPageBuffer(pageBuffer, file);

    final String queryResult = buildResult(response, req, get, post);
    assert null != queryResult;
    response.setContentType("application/xml");
    response.setCharacterEncoding("UTF-8");
    if(!response.containsHeader("Location"))
      response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(pageBuffer.toString().replace("{{$content}}",
        queryResult));
  }
  /**
   * Builds the resulting XQuery file in memory and evaluates the reslt.
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
