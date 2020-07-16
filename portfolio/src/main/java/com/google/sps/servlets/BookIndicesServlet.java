package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Indices;
import com.google.sps.utils.BooksMemoryUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Retrieves indices information about previous Book request */
@WebServlet("/book-indices")
public class BookIndicesServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String sessionID = request.getParameter("session-id");
    String queryID = request.getParameter("query-id");

    int startIndex = BooksMemoryUtils.getStoredIndices("startIndex", sessionID, queryID);
    int resultsStored = BooksMemoryUtils.getStoredIndices("resultsStored", sessionID, queryID);
    int totalResults = BooksMemoryUtils.getStoredIndices("totalResults", sessionID, queryID);
    int displayNum = BooksMemoryUtils.getStoredIndices("displayNum", sessionID, queryID);

    Indices indices = new Indices(startIndex, resultsStored, totalResults, displayNum);
    String json = new Gson().toJson(indices);
    response.getWriter().write(json);
  }
}
