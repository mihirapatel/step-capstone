package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Saves workout plans to user profile when save button is clicked */
@WebServlet("/save-workouts")
public class SaveWorkoutsServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String userId = userService.getCurrentUser().getUserId();
    // String workoutPlanString = request.getParameter("workout-plan-videos");
    String wp = request.getParameter("workout-plan-videos");
    // System.out.println(wp);
    String json = new Gson().toJson(wp);
    System.out.println(json);
    // WorkoutPlan workoutPlan = new Gson().fromJson(wp, WorkoutPlan.class);

    // VideoUtils.saveWorkoutPlan(userId, datastore, workoutPlan);
  }
}
