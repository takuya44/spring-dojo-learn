package com.example.blog.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  public JsonUsernamePasswordAuthenticationFilter(
      SecurityContextRepository securityContextRepository,
      SessionAuthenticationStrategy sessionAuthenticationStrategy,
      AuthenticationManager authenticationManager) {
    super();
    // SecurityContextRepositoryを設定:例
    setSecurityContextRepository(securityContextRepository);

    // セッション認証戦略を設定: 認証成功後のセッション管理を設定
    setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

    // ユーザー名とパスワードの検証を行う
    setAuthenticationManager(authenticationManager);

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

    var objectMapper = new ObjectMapper();
    LoginRequest jsonRequest;

    try {
      // リクエストボディからJSONを読み込み、LoginRequestクラスのオブジェクトにマッピング
      jsonRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
    } catch (IOException e) {
      // JSON読み取りに失敗した場合にAuthenticationServiceExceptionをスロー
      throw new AuthenticationServiceException("failed to read request as json", e);
    }

    // ユーザー名とパスワードがnullでないかを確認し、nullの場合は空文字列にする
    var username = jsonRequest.username != null ? jsonRequest.username : "";
    var password = jsonRequest.password != null ? jsonRequest.password : "";

    // ユーザー名とパスワードを使って認証リクエストを作成
    var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
        username, password);

    // 認証リクエストにリクエストの詳細を設定
    setDetails(request, authRequest);

    // AuthenticationManagerを使って認証処理を実行
    return this.getAuthenticationManager().authenticate(authRequest);
  }

  private record LoginRequest(String username, String password) {

  }

}
