package org.basex.web.servlet;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpException;

import com.google.gson.Gson;

/**
 * This class handles GET or POST requests and prepares a Map with the
 * respective values.
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle <ms@basex.org>
 */
public abstract class PrepareParamsServlet extends HttpServlet {
  /** Version. */
  private static final long serialVersionUID = 8548004356377035911L;
  /** the web root: *TODO* this path might be configurable in future version. */
  protected final String fPath;

  /** Constructor. */
  public PrepareParamsServlet() {
    try {
      fPath = new File("src/main/webapp").getCanonicalPath();
    } catch(final IOException e) {
      // should never happen
      throw new IOError(e);
    }
  }

  @Override
  protected final void doGet(final HttpServletRequest req,
      final HttpServletResponse resp) throws IOException {
    final String get = getMap(req);
    final String post = "{}";
     try {
      get(resp, req, requestedFile(req.getRequestURI()), get, post);
    } catch(final HttpException e) {
      resp.sendError(e.getStatus(), e.getReason());
    }
  }

  /**
   * Populates the POST variables map.
   * @param req request
   * @return POST map
   */
  private String getPost(final HttpServletRequest req) {
    return getMap(req);
  }

  /**
   * Populates the GET variables map.
   * @param req request
   * @return GET map
   */
  private String getMap(final HttpServletRequest req) {
    @SuppressWarnings("unchecked")
    Set<Map.Entry<String, String[]>>set = req.getParameterMap().entrySet();
    
    final HashMap<String, Object> result = new HashMap<String, Object>();
    for(final Map.Entry<String,String[]> entry : set) {
      final String key = entry.getKey();
      final String[] value = entry.getValue();
      if(value.length == 1){
        result.put(key, value[0]);
      }else{
        result.put(key, value);
      }
    }
    return new Gson().toJson(result);
  }

  @Override
  public final void doPost(final HttpServletRequest req,
      final HttpServletResponse resp) throws IOException {

    final String get = "{}";
    final String post = getPost(req);

    try {
      get(resp, req, requestedFile(req.getRequestURI()), get, post);
    } catch(final HttpException e) {
      resp.sendError(e.getStatus(), e.getReason());
    }
  }

  /**
   * Performs the actual get, this is needed to allow
   * {@link PrepareParamsServlet} collecting the parameters before delegating
   * the actual get or post of the implementation.
   * @param resp response object
   * @param req request object
   * @param f requested file
   * @param get GET map
   * @param post POST map
   * @throws IOException I/O exception
   */
  public abstract void get(final HttpServletResponse resp,
      final HttpServletRequest req, final File f,
      final String get, final String post) throws IOException;

  /**
   * Checks whether the file exists.
   * @param file file
   * @return File object
   * @throws HttpException HTTP exception
   */
  protected File requestedFile(final String file) throws HttpException {
    final File f = new File(fPath, file);
    if(!f.exists()) throw new HttpException(HttpServletResponse.SC_NOT_FOUND,
        "The file '" + file + "' doesn't exist on the server.");
    try {
      final File canon = f.getCanonicalFile();
      if(!canon.toString().startsWith(fPath)) throw new HttpException(
          HttpServletResponse.SC_FORBIDDEN, "The requested file '"
          + file + "' isn't below the server root.");
      return canon;
    } catch(final IOException ioe) {
      // TODO too much information / unsafe?
      throw new HttpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ioe.getLocalizedMessage());
    }
  }

}
