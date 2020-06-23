package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
    response.setContentType("text/html;");
    PrintWriter out = response.getWriter();

    String loginUrl = userService.createLoginURL("/index.html");

    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
      String id = userService.getCurrentUser().getUserId();
    //   String nickname = UserUtils.getUserNickname(id, userService);
      out.println("<a class=\"link\" href=\"" + logoutUrl + "\">Logout</a>");
    } else {
      out.println("<a class=\"link\" href=\"" + loginUrl + "\">Login</a>");
    }
  }

//   @Override
//   public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//     // String nickname = request.getParameter("nickname");
//     String id = userService.getCurrentUser().getUserId();

//     DatastoreService datastore = createDataService();
//     Entity entity = new Entity("UserInfo", id);
//     entity.setProperty("id", id);
//     entity.setProperty("nickname", nickname);
//     datastore.put(entity);

//     response.sendRedirect("/travel.html");
//   }

  protected UserService createUserService() {
    return UserServiceFactory.getUserService();
  }

  protected DatastoreService createDataService() {
    return DatastoreServiceFactory.getDatastoreService();
  }

}
