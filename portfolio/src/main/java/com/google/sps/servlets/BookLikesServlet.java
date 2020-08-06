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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.utils.BooksMemoryUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles book likes by storing liked Book information, or deleting stored liked Book
 * information.
 */
@WebServlet("/book-likes")
public class BookLikesServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();

  /**
   * POST method to modify database of book likes information.
   *
   * @param request HTTP request for Book Likes servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String type = request.getParameter("type");
    int orderNum = Integer.parseInt(request.getParameter("orderNum"));
    String queryID = request.getParameter("query-id");

    if (type.equals("like")) {
      BooksMemoryUtils.likeBook(orderNum, queryID, userService, datastore);
    } else if (type.equals("unlike")) {
      BooksMemoryUtils.unlikeBook(orderNum, queryID, userService, datastore);
    }
  }
}
