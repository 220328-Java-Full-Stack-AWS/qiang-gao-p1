package com.revature.repositories;

import com.revature.database.MockDB;
import com.revature.models.User;

import java.util.ArrayList;
import java.util.Optional;

public class UserDAO {
  static ArrayList<User> users;
  private MockDB mockDB = MockDB.getInstance();


  /**
   * Should retrieve a User from the DB with the corresponding username or an empty optional if there is no match.
   */
  public Optional<User> getByUsername(String username) {
    User user = mockDB.getUserByUsername(username);
    return user == null ? Optional.empty() : Optional.of(user);
  }

  /**
   * <ul>
   *     <li>Should Insert a new User record into the DB with the provided information.</li>
   *     <li>Should throw an exception if the creation is unsuccessful.</li>
   *     <li>Should return a User object with an updated ID.</li>
   * </ul>
   */
  public User create(User userToBeRegistered) {
    return userToBeRegistered;
  }

}