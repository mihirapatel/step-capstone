package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.WorkoutProfile;
import com.google.sps.utils.UserUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Retrieves information about the user: id, name, email */
@WebServlet("/workout-user-profile")
public class WorkoutProfileServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    // Get workout profile parameters
    String userId = userService.getCurrentUser().getUserId();
    String userName = UserUtils.getDisplayName(userService, datastore);
    String userEmail = userService.getCurrentUser().getEmail();

    // Create WorkoutProfile
    WorkoutProfile profile = new WorkoutProfile(userId, userName, userEmail);
    String json = new Gson().toJson(profile);
    response.getWriter().write(json);
  }
}
