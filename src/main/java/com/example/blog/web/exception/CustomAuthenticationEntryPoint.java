package com.example.blog.web.exception;

import com.example.blog.model.Unauthorized;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * カスタム認証エントリーポイントクラス。
 *
 * <p>このクラスは {@link AuthenticationEntryPoint} を実装し、Spring Securityで未認証ユーザーによる
 * アクセスが発生した場合のカスタマイズされた応答を提供します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>未認証ユーザーのリクエストに対してHTTP 401 Unauthorizedを返す。</li>
 *   <li>エラー情報をJSON形式でクライアントに返す。</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /**
   * Jacksonの {@link ObjectMapper} を使用してJavaオブジェクトをJSON形式に変換します。
   */
  private final ObjectMapper objectMapper;

  /**
   * 認証エラーが発生した際に呼び出されるメソッド。
   *
   * <p>未認証ユーザーが保護されたリソースにアクセスしようとした場合、このメソッドが実行され、
   * 適切なHTTPレスポンスを返します。</p>
   *
   * @param request       現在のHTTPリクエスト
   * @param response      HTTPレスポンスオブジェクト
   * @param authException 認証エラーの詳細を表す例外
   * @throws IOException      入出力エラーが発生した場合
   * @throws ServletException サーブレットエラーが発生した場合
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException, ServletException {
    // HTTPステータスコードを401 Unauthorizedに設定
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    // Unauthorized エラー情報をカスタムオブジェクトに格納
    var body = new Unauthorized();
    body.setDetail("リクエストを実行するにはログインが必要です");
    body.instance(URI.create(request.getRequestURI()));

    // res.getOutputStream　で開いたストリームは
    // writeValue の中で close されるので明示的な close は不要。メモリリークしない
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
