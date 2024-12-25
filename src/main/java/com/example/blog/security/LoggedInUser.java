package com.example.blog.security;

import java.util.List;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

@Getter
public class LoggedInUser extends User {

  private final long userId;

  public LoggedInUser(
      long userId,
      String username,
      String password,
      boolean enabled
  ) {
    super(username, password, enabled, true, true, true, List.of());
    this.userId = userId;
  }
}
