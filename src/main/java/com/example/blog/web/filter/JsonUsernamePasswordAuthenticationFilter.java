package com.example.blog.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  public JsonUsernamePasswordAuthenticationFilter(
      SecurityContextRepository securityContextRepository) {
    super();
    // SecurityContextRepositoryを設定:例
    setSecurityContextRepository(securityContextRepository);

    // 認証成功時の処理を設定
    setAuthenticationSuccessHandler((request, response, authentication) -> {
      response.setStatus(HttpServletResponse.SC_OK);
    });

    // 認証失敗時の処理を設定
    setAuthenticationFailureHandler((request, response, authentication) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    });
  }


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
