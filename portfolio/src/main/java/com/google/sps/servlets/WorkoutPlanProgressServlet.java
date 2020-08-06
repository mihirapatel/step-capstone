/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.sps.utils.WorkoutProfileUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/workout-plan-progress")
public class WorkoutPlanProgressServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * POST method that updates Workout Plan progress when user clicks "Mark Complete" button
   *
   * @param request HTTP request for Workout Plan Progress servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    // Get workout plan parameters
    String workoutPlanString = request.getParameter("workout-plan");
    JSONObject workoutPlanJson = new JSONObject(workoutPlanString);
    String userId = (String) workoutPlanJson.get("userId");
    int workoutPlanId = (int) workoutPlanJson.get("workoutPlanId");

    // Get number of workout days completed
    int numWorkoutDaysCompleted =
        Integer.valueOf(request.getParameter("num-workout-days-completed"));

    // Update Workout Plan in datastore with number of workout days completed
    WorkoutProfileUtils workoutProfileUtils = new WorkoutProfileUtils();
    workoutProfileUtils.updateSavedWorkoutPlan(
        userId, workoutPlanId, numWorkoutDaysCompleted, datastore);
  }
}
