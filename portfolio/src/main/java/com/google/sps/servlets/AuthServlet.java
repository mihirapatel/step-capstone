package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.utils.UserUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader; 
import java.io.File;
import java.nio.file.Files; 
import java.nio.file.Paths;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

  UserService userService = createUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    String loginUrl = userService.createLoginURL("/index.html");
    String authText;
    String displayName;
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
      String id = userService.getCurrentUser().getUserId();
      authText = logoutUrl;
      displayName = UserUtils.getDisplayName();
    } else {
      authText = loginUrl;
      displayName = "";
    }

    AuthOutput output = new AuthOutput(authText, displayName);
    String json = new Gson().toJson(output);
    System.out.println(json);
    response.getWriter().write(json);
  }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  class AuthOutput {
    String authText;
    String displayName;

    AuthOutput(String authText, String displayName) {
      this.authText = authText;
      this.displayName = displayName;
    }
  }
}
