package com.example.blog.web.controller;

import com.example.blog.api.CsrfCookieApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * CSRFCookieRestControllerは、クライアントにCSRF Cookieを提供するためのエンドポイントを実装したクラスです。
 * <p>
 * このクラスは、OpenAPI Generator（https://openapi-generator.tech）によって自動生成された {@link CsrfCookieApi}
 * インターフェースを実装しています。
 * クライアントがPOSTリクエストを送信する際に、CSRF対策のために使用される`XSRF-TOKEN`を含むクッキーを取得するためのエンドポイントを提供します。
 * </p>
 * <p>
 * このエンドポイントにGETリクエストを送信すると、204 No Contentのステータスコードが返され、 Set-Cookieヘッダーに `XSRF-TOKEN` が含まれます。
 * このトークンは、POSTリクエスト時に`X-XSRF-TOKEN` ヘッダーに設定して使用します。 なお、`XSRF-TOKEN`の値は、複数のPOSTリクエストで再利用可能です。
 * </p>
 */
@RestController
public class CSRFCookieRestController implements CsrfCookieApi {

  /**
   * CSRF Cookieをクライアントに返すエンドポイント。
   * <p>
   * リクエストに成功すると、204 No Contentのレスポンスを返します。 レスポンスの `Set-Cookie` ヘッダーに `XSRF-TOKEN`
   * が含まれており、このトークンをPOSTリクエストに使用します。
   * </p>
   *
   * @return 204 No Contentが正常な場合のレスポンスとして返されます。
   */
  @Override
  public ResponseEntity<Void> getCsrfCookie() {
    return ResponseEntity.noContent().build();
  }
}
