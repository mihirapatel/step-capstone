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
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet follows the Google Authorization Code Flow by handling callbacks from authorization
 * request forms.
 */
@WebServlet("/oauth2callback")
public class OAuthCallbackServlet extends AbstractAuthorizationCodeCallbackServlet {
  UserService userService = UserServiceFactory.getUserService();
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static Logger log = LoggerFactory.getLogger(OAuthCallbackServlet.class);

  /**
   * Handles succes redirect for oauth request.
   *
   * @param request HTTP request for OAuth callback servlet
   * @param response Writer to return http response to input request
   * @param credential OAuth credential token
   */
  @Override
  protected void onSuccess(
      HttpServletRequest request, HttpServletResponse response, Credential credential)
      throws ServletException, IOException {
    log.info("Success callback servlet");
    log.info("Credential: " + credential.getAccessToken());
    response.sendRedirect("/");
  }

  /**
   * Handles error redirect for OAuth request.
   *
   * @param request HTTP request for OAuth callback servlet
   * @param response Writer to return http response to input request
   * @param errorResponse OAuth error message url
   */
  @Override
  protected void onError(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    log.error("Callback servlet OAuth error occured.");
    response.sendRedirect("/");
  }

  /**
   * Creates redirect url for OAuth service
   *
   * @param request HTTP request for OAuth callback servlet
   * @return string containing url for OAuth redirect
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
   * @param request HTTP request for OAuth callback servlet
   * @return string containing current user's ID
   */
  @Override
  protected String getUserId(HttpServletRequest request) throws ServletException, IOException {
    return userService.getCurrentUser().getUserId();
  }
}
