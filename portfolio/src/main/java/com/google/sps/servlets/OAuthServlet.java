/*
 * Copyright 2019 Google LLC
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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.utils.OAuthHelper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet follows the Google Authorization Code Flow by initializing and redirecting to
 * authorization request forms.
 */
@WebServlet("/oauth2")
public class OAuthServlet extends AbstractAuthorizationCodeServlet {

  UserService userService = UserServiceFactory.getUserService();
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * GET method to handle redirect for oauth request.
   *
   * @param request HTTP request for OAuth servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect("/");
  }

  /**
   * Handles succes redirect for oauth request.
   *
   * @param request HTTP request for OAuth servlet
   * @return String containing the OAuth redirect url
   */
  @Override
  protected String getRedirectUri(HttpServletRequest request) throws ServletException, IOException {
    return OAuthHelper.createRedirectUri();
  }

  /**
   * Creates authorization codeflow for current user
   *
   * @return OAuth authorization code flow for current user
   */
  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    String userID = userService.getCurrentUser().getUserId();
    return OAuthHelper.createFlow(userID);
  }

  /**
   * Retrieves the current user's ID
   *
   * @param request HTTP request for OAuth servlet
   * @return string containing current user's ID
   */
  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    return userService.getCurrentUser().getUserId();
  }
}
