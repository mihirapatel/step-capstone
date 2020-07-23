package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.WorkoutPlan;
import com.google.sps.utils.VideoUtils;
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
    String workoutPlanString = request.getParameter("workout-plan-videos");
    WorkoutPlan workoutPlan = new Gson().fromJson(workoutPlanString, WorkoutPlan.class);

    VideoUtils.saveWorkoutPlan(userId, datastore, workoutPlan);
  }
}
