package com.example.blog.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public final class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    
    // CSRFトークンをリクエスト属性から取得
    CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

    // トークンの値をCookieにレンダリングするためにトークンを取得（この操作によってトークンが生成される）
    csrfToken.getToken();

    // 他のフィルタにリクエストとレスポンスを渡す
    filterChain.doFilter(request, response);
  }
}
