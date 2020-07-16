package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.utils.BooksMemoryUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieves sessionIDs for each user (logged in or not) in order to keep track of their stored Book
 * information and deletes stored session Book Information when session is over
 */
@WebServlet("/id")
public class SessionIdServlet extends HttpServlet {

  UserService userService = createUserService();
  DatastoreService datastore = createDatastore();

  /**
   * Retrieves a unique sessionID for each user in order to keep track of their stored Book
   * information
   *
   * <p>If the user is logged in, their sessionID is their userID. Otherwise, a unique guest session
   * ID is generated for their session ID
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    String id;

    if (userService.isUserLoggedIn()) {
      id = userService.getCurrentUser().getUserId();
    } else {
      int prevId = 0;
      Query query = new Query("SessionID");
      PreparedQuery results = datastore.prepare(query);

      int numIds = 0;
      for (Entity entity : results.asIterable()) {
        Long lngValue = (Long) entity.getProperty("idNum");
        prevId = lngValue.intValue();
        datastore.delete(entity.getKey());
        ++numIds;
      }
      Entity entity = new Entity("SessionID");
      entity.setProperty("idNum", prevId + 1);
      datastore.put(entity);
      id = "guest" + Integer.toString(prevId + 1);
    }
    response.getWriter().write(id);
  }

  /** Deletes all stored Entity in Datastore that match the "session-id" parameter */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String sessionID = request.getParameter("session-id");
    BooksMemoryUtils.deleteAllStoredBookInformation(sessionID, datastore);
  }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  protected DatastoreService createDatastore() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
