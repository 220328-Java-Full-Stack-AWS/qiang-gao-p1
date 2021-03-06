package com.revature.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.revature.database.PostgreSQLDatabase;
import com.revature.models.Reimbursement;
import com.revature.repositories.UserDao;
import com.revature.services.AuthService;
import com.revature.services.ReimbursementService;
import com.revature.services.UserService;
import com.revature.util.Util;

public class ReimbursementServlet extends HttpServlet {
  private Gson gson;
  private AuthService authService;
  private UserService userService;
  private UserDao userDao;
  private Connection connection;
  private ReimbursementService reimbursementService;

  @Override
  public void init() {
    try {
      connection = PostgreSQLDatabase.getConnection();
      userDao = new UserDao(connection);
      userService = new UserService(userDao);
      this.authService = new AuthService(userService);
      gson = new Gson();
      reimbursementService = new ReimbursementService(connection);
    } catch (SQLException e) {
      System.out.println("Database connection error");
      e.printStackTrace();
    } catch (UnavailableException e) {
      System.out.println("Server error");
      e.printStackTrace();
    }
  }

  private void setAccessControlHeaders(HttpServletResponse res) {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE");
  }

  // preflight request
  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
    setAccessControlHeaders(res);
    res.setStatus(HttpServletResponse.SC_OK);
  }

  // GET @/reimbursements
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    setAccessControlHeaders(res);

    // setup response
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    // only registered users can access this
    // Get the token from the request
    String token = Util.getToken(req);

    // authorize the token
    if (token == null) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    if (!authService.isTokenValid(token)) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // authenticate the user
    // identify admin or user
    boolean isUser = authService.isTokenValid(token);
    boolean isAdmin = authService.isAdmin(token);

    // @/reimbursements?status={statusId}
    int statusTypeId = getStatus(req);

    // @/reimbursements/:reimbursementId
    int reimbursementId = req.getPathInfo() == null ? -1 : Integer.parseInt(req.getPathInfo().substring(1));

    // get userId from query string
    // @/reimbursements?userId=1
    int userId = req.getParameter("userId") == null ? -1 : Integer.parseInt(req.getParameter("userId"));

    // @/reimbursements
    // as admin, get all reimbursements
    if (isAdmin && reimbursementId == -1 && userId == -1 && statusTypeId == -1) {
      try {
        out.println(gson.toJson(reimbursementService.getAllReimbursements()));
        res.setStatus(HttpServletResponse.SC_OK);
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // @/reimbursements?status={statusId}
    // as admin, can filter reimbursements by status
    if (isAdmin && reimbursementId == -1 && userId == -1 && statusTypeId != -1) {
      try {
        out.println(gson.toJson(reimbursementService.getReimbursementsByStatus(statusTypeId)));
        res.setStatus(HttpServletResponse.SC_OK);
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // @/reimbursements/:reimbursementId
    // as admin, get any reimbursement by reimbursementId
    if (isAdmin && reimbursementId != -1 && userId == -1) {
      try {
        Reimbursement reimbursement = reimbursementService.getReimbursementById(reimbursementId);
        if (reimbursement == null) {
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        res.setStatus(HttpServletResponse.SC_OK);
        out.println(gson.toJson(reimbursement));
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // @/reimbursements?userId=1
    // as admin, get all reimbursements by userId
    if (isAdmin && reimbursementId == -1 && userId != -1) {
      try {
        out.println(gson.toJson(reimbursementService.getAllReimbursementsByUserId(userId)));
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // @/reimbursements
    // as user get all reimbursements belonging to user

    if (isUser && reimbursementId == -1 && userId == -1 && statusTypeId == -1) {
      try {
        List<Reimbursement> reimbursements = reimbursementService
            .getAllReimbursementsByUserId(authService.getUserId(token));
        res.setStatus(HttpServletResponse.SC_OK);
        out.println(gson.toJson(reimbursements));
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // @/reimbursements/?status=1
    // as user get all reimbursements belonging to user by status

    if (isUser && reimbursementId == -1 && userId == -1 && statusTypeId != -1) {
      try {
        List<Reimbursement> reimbursements = reimbursementService
            .getAllReimbursementsByUserIdAndStatus(authService.getUserId(token), statusTypeId);

        res.setStatus(HttpServletResponse.SC_OK);
        out.println(gson.toJson(reimbursements));
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }
    // @/reimbursements/:reimbursementId
    // as user get reimbursement by id belonging to userself
    if (isUser && reimbursementId != -1 && userId == -1) {
      try {
        Reimbursement reimbursement = reimbursementService.getReimbursementById(reimbursementId);
        if (reimbursement.getAuthorId() == authService.getUserId(token)) {
          out.println(gson.toJson(reimbursement));
        } else {
          res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

  }

  // POST @/reimbursements
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    setAccessControlHeaders(res);

    // Get the token from the request
    // validate token
    String token = Util.getToken(req);
    if (token == null) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    if (!authService.isTokenValid(token)) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // get userId from token
    int userId = authService.getUserId(token);
    if (userId == -1) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // get reimbursement from request body
    String body = Util.getBody(req);

    // parse reimbursement from json
    Reimbursement reimbursement = gson.fromJson(body, Reimbursement.class);
    reimbursement.setAuthorId(userId);

    try {
      Reimbursement savedReimbursement = reimbursementService.add(reimbursement);
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().println(gson.toJson(savedReimbursement));
    } catch (SQLException e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
  }

  // PUT @/reimbursements/:id
  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    setAccessControlHeaders(res);

    // Get the token from the request
    String token = Util.getToken(req);
    if (token == null) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // validate token
    if (!authService.isTokenValid(token)) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // identify admin or user
    boolean isAdmin = authService.isAdmin(token);

    // @/reimbursements/:reimbursementId
    int reimbursementId = req.getPathInfo() == null ? -1 : Integer.parseInt(req.getPathInfo().substring(1));

    if (reimbursementId == -1) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // as admin, update only status
    // @/reimbursements/:reimbursementId?status=1
    // 1 PENDING
    // 2 APPROVED
    // 3 DENIED
    int status = getStatus(req);
    if (isAdmin && status != -1 && reimbursementId != -1) {
      try {
        int adminId = authService.getUserId(token);
        reimbursementService.updateStatus(reimbursementId, status, adminId);
        res.setStatus(HttpServletResponse.SC_OK);
        return;
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // as user, update only amount, description, reimbursementTypeId
    // when status is PENDING
    if (!isAdmin && reimbursementId != -1) {
      String body = Util.getBody(req);
      try {
        Reimbursement reimbursementFromDatabase = reimbursementService.getReimbursementById(reimbursementId);
        if (reimbursementFromDatabase.getAuthorId() != authService.getUserId(token)) {
          res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
        if (reimbursementFromDatabase.getStatusId() != 1) {
          res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
        Reimbursement reimbursementFromRequest = gson.fromJson(body, Reimbursement.class);
        reimbursementFromDatabase.setAmount(reimbursementFromRequest.getAmount());
        reimbursementFromDatabase.setDescription(reimbursementFromRequest.getDescription());
        reimbursementFromDatabase.setReimbursementTypeId(reimbursementFromRequest.getReimbursementTypeId());
        reimbursementService.update(reimbursementFromDatabase);
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    } else {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

  }

  private int getStatus(HttpServletRequest req) {
    return req.getParameter("status") == null ? -1 : Integer.parseInt(req.getParameter("status"));
  }

  // DELETE @/reimbursements/:id
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    setAccessControlHeaders(res);

    // Get the token from the request
    String token = Util.getToken(req);
    if (token == null) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    // verify token
    if (!authService.isTokenValid(token)) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    // get reimbursementId from request
    int reimbursementId = req.getPathInfo() == null ? -1 : Integer.parseInt(req.getPathInfo().substring(1));

    try {
      // get reimbursement from database
      Reimbursement reimbursement = reimbursementService.getReimbursementById(reimbursementId);
      if (reimbursement == null) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      // check if status is PENDING
      if (reimbursement.getStatusId() != 1) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      // verify userId
      if (reimbursement.getAuthorId() != authService.getUserId(token)) {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      // delete reimbursement
      reimbursementService.delete(reimbursementId);
      res.setStatus(HttpServletResponse.SC_OK);
    } catch (SQLException e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
  }

  public void destroy() {
    try {
      PostgreSQLDatabase.disconnect();
      if (connection.isClosed()) {
        System.out.println("Databse connection closed");
      } else {
        System.out.println("database disconnect failed");
      }
    } catch (SQLException e) {
      System.out.println("database disconnect failed");
      e.printStackTrace();
    }
  }
}
