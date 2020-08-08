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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.utils.BooksMemoryUtils;
import com.google.sps.utils.BooksSetUpHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet retrieves sessionIDs for each user (logged in or not) in order to keep track of
 * their stored Book information and deletes stored session Book Information when session is over.
 */
@WebServlet("/id")
public class SessionIdServlet extends HttpServlet {
  private static Logger log = LoggerFactory.getLogger(BooksSetUpHelper.class);
  UserService userService = createUserService();
  DatastoreService datastore = createDatastore();

  /**
   * Retrieves a unique sessionID for each user in order to keep track of their stored Book
   * information.
   *
   * <p>If the user is logged in, their sessionID is their userID. Otherwise, a unique guest session
   * ID is generated for their session ID
   *
   * @param request HTTP request for Session ID servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    String id;

    // Set up book likes for demo
    try {
      BooksSetUpHelper.setUpBookLikes(datastore);
    } catch (FileNotFoundException e) {
      log.error("Could not set up liked books for test users.");
    }

    if (userService.isUserLoggedIn()) {
      id = userService.getCurrentUser().getUserId();
    } else {
      int prevId = 0;
      Query query = new Query("SessionID");
      PreparedQuery results = datastore.prepare(query);

      int numIds = 0;
      for (Entity entity : results.asIterable()) {
        Long lngValue = (Long) entity.getProperty("idNum");
        prevId = lngValue.intValue();
        datastore.delete(entity.getKey());
        ++numIds;
      }
      Entity entity = new Entity("SessionID");
      entity.setProperty("idNum", prevId + 1);
      datastore.put(entity);
      id = "guest" + Integer.toString(prevId + 1);
    }
    response.getWriter().write(id);
  }

  /**
   * Deletes all stored Entity in Datastore that match the "session-id" parameter
   *
   * @param request HTTP request for Session ID servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String sessionID = request.getParameter("session-id");
    BooksMemoryUtils.deleteAllStoredBookInformation(sessionID, datastore);
  }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  protected DatastoreService createDatastore() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
