package com.revature.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.revature.database.PostgreSQLDatabase;
import com.revature.models.User;
import com.revature.repositories.UserDao;
import com.revature.services.AuthService;
import com.revature.services.UserService;
import com.revature.util.Util;

//// @WebServlet(urlPatterns = "/users")
public class UserServlet extends HttpServlet {
  private Gson gson;
  private AuthService authService;
  private UserService userService;
  private UserDao userDao;
  private Connection connection;

  @Override
  public void init(ServletConfig config) throws ServletException {
    connection = PostgreSQLDatabase.getConnection();
    userDao = new UserDao(connection);
    userService = new UserService(userDao);
    this.authService = new AuthService(userService);
    gson = new Gson();
  }

  // for testing GET @route /api/users
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();

    // test commection to doGet
    //// out.println("<h2>Hello from the UserServlet</h2>");

    // get data from params
    //// String username = request.getParameter("username");
    //// String password = request.getParameter("password");

    // recieving data from header
    String username = request.getHeader("username");
    String password = request.getHeader("password");

    // testing sending data back
    //// out.println("<h2>" + username + "</h2>");
    //// out.println("<h2>" + password + "</h2>");

    // user userService login return User object
    try {
      User loggedInUser = authService.login(username, password);
      // convert User object to json
      String userJson = gson.toJson(loggedInUser);
      out.print(userJson);
    } catch (SQLException e) {
      response.setStatus(500);
      String errorJson = gson.toJson(e);
      out.print(errorJson);
    }

    out.flush();

  }

  // Login with username and password
  // @route POST /api/users/login
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // String body = Util.getBody(request);
    String parameterString = Util.getParamsFromPost(request);

    PrintWriter out = response.getWriter();

    out.print(parameterString);
  }

  // @Override
  // protected void doPost(HttpServletRequest request, HttpServletResponse
  // response) throws IOException {
  // String username = request.getParameter("username");
  // String password = request.getParameter("password");
  // PrintWriter out = response.getWriter();

  // // use AuthService to do user authentication
  // try {
  // User user = authService.login(username, password);
  // if (user != null) {
  // String userJsonString = gson.toJson(user);
  // response.setStatus(200);
  // response.setContentType("application/json");
  // response.setCharacterEncoding("UTF-8");
  // out.print(userJsonString);
  // }
  // } catch (UserNamePasswordNotMatchException e) {
  // response.setStatus(401);
  // out.print("Username and Password not match");

  // } catch (UserNotExistException e) {
  // response.setStatus(404);
  // out.print("Username not found, please go to registration");

  // } catch (SQLException e) {
  // e.printStackTrace();
  // response.setStatus(500);
  // out.print("Something wrong with database");
  // } finally {
  // out.flush();
  // }

}
