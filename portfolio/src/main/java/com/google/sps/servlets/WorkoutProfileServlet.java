package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.WorkoutPlan;
import com.google.sps.data.WorkoutProfile;
import com.google.sps.utils.VideoUtils;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieves information about the user related to workout: workout plans, completed workouts, etc.
 */
@WebServlet("/workout-user-profile")
public class WorkoutProfileServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String userId = userService.getCurrentUser().getUserId();
    ArrayList<WorkoutPlan> workoutPlans = VideoUtils.getWorkoutPlansList(userId, datastore);
    WorkoutProfile workoutProfile = new WorkoutProfile(userId, workoutPlans);

    String json = new Gson().toJson(workoutProfile);
    response.getWriter().write(json);
  }
}
