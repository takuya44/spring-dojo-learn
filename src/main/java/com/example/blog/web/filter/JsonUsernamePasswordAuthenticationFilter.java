package com.example.blog.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    return UsernamePasswordAuthenticationToken.authenticated(
        "dummy-user",     // 認証されたユーザー名
        "dummy-password",         // 認証されたパスワード（今回はダミー値）
        null                      // 認証されたユーザーの権限（今回はnull）
    );
  }
}
