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
import com.google.sps.data.Friend;
import com.google.sps.utils.UserUtils;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles user authentication */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

  UserService userService = createUserService();
  DatastoreService datastore = createDatastore();

  /**
   * GET method that handles http request for login or logout services.
   *
   * @param request HTTP request
   * @param response Writer to return http response to input request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    String loginUrl = userService.createLoginURL("/oauth2");
    String authText;
    String displayName;
    String logButton;
    String photoUrl;
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
      String id = userService.getCurrentUser().getUserId();
      Friend user = UserUtils.getUser(userService, datastore);
      authText = logoutUrl;
      displayName = UserUtils.getDisplayName(userService, datastore, user);
      photoUrl = UserUtils.getPhotoUrl(userService, datastore, user);
      logButton = "Logout";
    } else {
      authText = loginUrl;
      displayName = "";
      logButton = "Login";
      photoUrl = "images/android.png";
    }

    AuthOutput output = new AuthOutput(authText, displayName, logButton, photoUrl);
    String json = new Gson().toJson(output);
    System.out.println(json);
    response.getWriter().write(json);
  }

  /**
   * Post method that handles http request for login services
   *
   * @param request HTTP request
   * @param response Writer to return http response to input request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (userService.isUserLoggedIn()) {
      response.sendRedirect("/oauth2");
    }
  }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  protected DatastoreService createDatastore() {
    return DatastoreServiceFactory.getDatastoreService();
  }

  class AuthOutput {
    String authText;
    String displayName;
    String logButton;
    String photoUrl;

    AuthOutput(String authText, String displayName, String logButton, String photoUrl) {
      this.authText = authText;
      this.displayName = displayName;
      this.logButton = logButton;
      this.photoUrl = photoUrl;
    }
  }
}
