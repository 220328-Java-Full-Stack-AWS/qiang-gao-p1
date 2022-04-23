// package com.revature.servlets;

// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.io.IOException;
// import java.io.PrintWriter;
// import java.sql.SQLException;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import com.revature.exceptions.UserNamePasswordNotMatchException;
// import com.revature.exceptions.UserNotExistException;
// import com.revature.models.Role;
// import com.revature.models.User;
// import com.revature.services.AuthService;

// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// @ExtendWith(MockitoExtension.class)
// public class UserServletTest {
// String NOT_EXIST_USERNAME = "NOT_EXIST_USERNAME";
// String NOT_EXIST_PASSWORD = "NOT_EXIST_PASSWORD";
// String EXIST_USERNAME = "EXIST_USERNAME";
// String WRONG_PASSWORD = "WRONG_PASSWORD";

// @Mock
// AuthService mockAuthService;

// @Mock
// HttpServletRequest mockRequest;

// @Mock
// HttpServletResponse mockResponse;

// @Mock
// PrintWriter mockOut;

// @Test
// void doPostShouldSent404ResponseWhenReqeustUserNotExist() throws IOException,
// SQLException {

// when(mockRequest.getParameter("username")).thenReturn(NOT_EXIST_USERNAME);
// when(mockRequest.getParameter("password")).thenReturn(NOT_EXIST_PASSWORD);

// when(mockResponse.getWriter()).thenReturn(mockOut);

// when(mockAuthService.login(NOT_EXIST_USERNAME,
// NOT_EXIST_PASSWORD)).thenThrow(new UserNotExistException());

// UserServlet userController = new UserServlet(mockAuthService);
// userController.doPost(mockRequest, mockResponse);

// verify(mockResponse).setStatus(404);

// verify(mockOut).print("Username not found, please go to registration");

// verify(mockOut).flush();
// }

// @Test
// void doPostShouldSent401ResponseWhenRequestUserPasswordNotMatch() throws
// IOException, SQLException {

// when(mockRequest.getParameter("username")).thenReturn(EXIST_USERNAME);
// when(mockRequest.getParameter("password")).thenReturn(WRONG_PASSWORD);

// when(mockResponse.getWriter()).thenReturn(mockOut);

// when(mockAuthService.login(EXIST_USERNAME, WRONG_PASSWORD)).thenThrow(new
// UserNamePasswordNotMatchException());

// UserServlet userController = new UserServlet(mockAuthService);
// userController.doPost(mockRequest, mockResponse);

// verify(mockResponse).setStatus(401);

// verify(mockOut).print("Username and Password not match");

// verify(mockOut).flush();
// }

// @Test
// void doPostShouldSend200RestponseWhenRequestUserPasswordNotMatch() throws
// IOException, SQLException {

// User user = new User(1, "test", "123456", "test@test.com", "john", "doe",
// Role.EMPLOYEE);

// when(mockRequest.getParameter("username")).thenReturn(user.getUsername());
// when(mockRequest.getParameter("password")).thenReturn(user.getPassword());

// when(mockResponse.getWriter()).thenReturn(mockOut);

// when(mockAuthService.login(user.getUsername(),
// user.getPassword())).thenReturn(user);

// UserServlet userController = new UserServlet(mockAuthService);
// userController.doPost(mockRequest, mockResponse);

// verify(mockResponse).setStatus(200);

// verify(mockResponse).setContentType("application/json");

// verify(mockResponse).setCharacterEncoding("UTF-8");

// verify(mockOut).print(anyString());

// verify(mockOut).flush();
// }

// }