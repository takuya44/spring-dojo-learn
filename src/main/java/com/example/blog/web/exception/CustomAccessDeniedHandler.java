package com.example.blog.web.exception;

import com.example.blog.model.Forbidden;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

/**
 * カスタムアクセス拒否ハンドラークラス。
 *
 * <p>このクラスは {@link AccessDeniedHandler} を実装し、Spring Security のアクセス拒否時の動作を
 * カスタマイズするために使用されます。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>CSRFトークンがない、または不正な場合にカスタムエラーレスポンスを返す。</li>
 *   <li>エラー情報をJSON形式でクライアントに返す。</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  /**
   * Jackson の {@link ObjectMapper} を使用してJavaオブジェクトをJSONにシリアライズします。
   */
  private final ObjectMapper objectMapper;

  /**
   * アクセス拒否時のハンドリングメソッド。
   *
   * <p>アクセス拒否が発生した際に呼び出され、クライアントに適切なHTTPレスポンスを返します。</p>
   *
   * @param request               現在のHTTPリクエスト
   * @param response              HTTPレスポンスオブジェクト
   * @param accessDeniedException 発生したアクセス拒否例外
   * @throws IOException      入出力エラーが発生した場合
   * @throws ServletException サーブレットエラーが発生した場合
   */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException, ServletException {
    // CSRFトークンがない、または不正な場合の処理
    if (accessDeniedException instanceof MissingCsrfTokenException
        || accessDeniedException instanceof InvalidCsrfTokenException) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN); // ステータスコード 403 を設定
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

      // Forbidden エラー情報をカスタムオブジェクトに格納
      var body = new Forbidden();
      body.setDetail("CSRFトークンが不正です");
      body.instance(URI.create(request.getRequestURI())); // エラー発生元のリクエストURIを設定

      // JavaオブジェクトをJSONデータに変換し、HTTPレスポンスボディに書き込む：ないとbodyが空になる
      // objectMapper.writeValue の内部でストリームが閉じられるため、明示的な close は不要
      objectMapper.writeValue(response.getOutputStream(), body);
    }
  }
}
