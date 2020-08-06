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

  /**
   * GET method that retrieves current user information.
   *
   * @param request HTTP request for Workout Profile servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String userId = userService.getCurrentUser().getUserId();
    String userName = UserUtils.getDisplayName(userService, datastore);
    String userEmail = userService.getCurrentUser().getEmail();

    WorkoutProfile profile = new WorkoutProfile(userId, userName, userEmail);
    String json = new Gson().toJson(profile);
    response.getWriter().write(json);
  }
}
