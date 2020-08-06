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
import com.google.gson.Gson;
import com.google.sps.data.Indices;
import com.google.sps.utils.BooksMemoryUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet retrieves indices information about a certain queryId and sends it to the front-end
 * to create the book display table information.
 */
@WebServlet("/book-indices")
public class BookIndicesServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * GET method to retrieve bookshelf index information.
   *
   * @param request HTTP request for Book Indices servlet
   * @param response Writer to return http response to input request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String sessionID = request.getParameter("session-id");
    String queryID = request.getParameter("query-id");

    int startIndex = BooksMemoryUtils.getStoredIndices("startIndex", sessionID, queryID, datastore);
    int resultsStored =
        BooksMemoryUtils.getStoredIndices("resultsStored", sessionID, queryID, datastore);
    int totalResults =
        BooksMemoryUtils.getStoredIndices("totalResults", sessionID, queryID, datastore);
    int displayNum = BooksMemoryUtils.getStoredIndices("displayNum", sessionID, queryID, datastore);

    Indices indices = new Indices(startIndex, resultsStored, totalResults, displayNum);
    String json = new Gson().toJson(indices);
    response.getWriter().write(json);
  }
}
