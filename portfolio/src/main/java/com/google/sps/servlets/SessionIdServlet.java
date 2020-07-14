package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieves a unique sessionID for each user (logged in or not) in order to keep track of their
 * stored BookQuery information
 */
@WebServlet("/id")
public class SessionIdServlet extends HttpServlet {

  UserService userService = createUserService();
  DatastoreService datastore = createDatastore();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String id;
    String loginUrl = userService.createLoginURL("/index.html");
    String authText;

    if (userService.isUserLoggedIn()) {
      id = userService.getCurrentUser().getUserId();
    } else {
      int prevId = 0;
      Query query = new Query("GuestID");
      PreparedQuery results = datastore.prepare(query);

      int numIds = 0;
      for (Entity entity : results.asIterable()) {
        Long lngValue = (Long) entity.getProperty("idNum");
        prevId = lngValue.intValue();
        datastore.delete(entity.getKey());
        ++numIds;
      }
      Entity entity = new Entity("GuestID");
      entity.setProperty("idNum", prevId + 1);
      datastore.put(entity);
      id = "guest" + Integer.toString(prevId + 1);
    }

    response.getWriter().write(id);
  }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  protected DatastoreService createDatastore() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
