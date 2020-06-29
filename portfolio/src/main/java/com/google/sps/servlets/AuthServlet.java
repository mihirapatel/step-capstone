package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.utils.UserUtils;
import java.io.IOException;
import java.io.PrintWriter;
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
    String logButton;
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
      String id = userService.getCurrentUser().getUserId();
      authText = logoutUrl;
      displayName = UserUtils.getDisplayName();
      logButton = "Logout";
    } else {
      authText = loginUrl;
      displayName = "";
      logButton = "Login";
    }

    AuthOutput output = new AuthOutput(authText, displayName, logButton);
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
    String logButton;

    AuthOutput(String authText, String displayName, String logButton) {
      this.authText = authText;
      this.displayName = displayName;
      this.logButton = logButton;
    }
  }
}
