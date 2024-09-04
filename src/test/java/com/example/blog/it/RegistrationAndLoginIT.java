package com.example.blog.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationAndLoginIT {

  private final String TEST_USERNAME = "user99";
  private final String TEST_PASSWORD = "password1";

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void integrationTest() {
    // ユーザー登録
    var xsrfToken = getRoot();
    register(xsrfToken);

    // ログイン失敗
    // Cookie に XSRF-TOKEN がない
    // ヘッダーに X-XSRF-TOKEN がない
    // Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が異なる
    // ユーザー名が存在しない
    // パスワードがデータベースに保存されているパスワードと違う
    // ログイン成功
    // ユーザー名がデータベースに存在する
    // パスワードがデータベースに保存されているパスワードと一致する
    // Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が一致する
    // → 200 OK が返る
    // → レスポンスに Set-Cookie: JSESSIONID が返ってくる

  }


  /**
   * ルートエンドポイントに対してリクエストを送信し、XSRF-TOKENクッキーの値を取得する。
   *
   * <p>このメソッドは、次の手順で実行されます:
   * <ul>
   *   <li>ルートエンドポイント ("/") へのGETリクエストを送信</li>
   *   <li>レスポンスからXSRF-TOKENクッキーを取得</li>
   *   <li>ステータスコードが204 No Contentであることを検証</li>
   *   <li>XSRF-TOKENクッキーが存在し、その値が空でないことを検証</li>
   *   <li>検証が成功した場合、XSRF-TOKENクッキーの値を返す</li>
   * </ul>
   * </p>
   *
   * @return 取得したXSRF-TOKENクッキーの値
   * @throws AssertionError XSRF-TOKENが存在しない、または値が空である場合
   */
  private String getRoot() {
    // ## Arrange ##

    // ## Act ##
    var responseSpec = webTestClient.get().uri("/").exchange();

    // ## Action ##
    var response = responseSpec.returnResult(String.class);
    var xsrfTokenOpt = Optional.ofNullable(response.getResponseCookies().getFirst("XSRF-TOKEN"));

    // レスポンスのステータスコードが 204 No Content であることを検証
    responseSpec.expectStatus().isNoContent();

    // Cookie ヘッダーにXSRF-TOKENがある+空文字かどうかをチェック。
    assertThat(xsrfTokenOpt)
        .isPresent() // Optional<null>かOptional<xsrfTokenCookie = "aaa">の検証
        .hasValueSatisfying(xsrfTokenCookie ->
                assertThat(xsrfTokenCookie.getValue()).isNotBlank()
            // Optional<xsrfTokenCookie = "aaa">の値が空文字かどうかチェック。
        );

    return xsrfTokenOpt.get().getValue();
  }

  /**
   * 新しいユーザーを登録するためのメソッド。
   *
   * <p>このメソッドは、次の手順で実行されます:
   * <ul>
   *   <li>ユーザー名とパスワードを含むJSONボディを作成</li>
   *   <li>POSTリクエストを /users エンドポイントに対して送信</li>
   *   <li>リクエストにXSRF-TOKENクッキーとヘッダーを含める</li>
   *   <li>ステータスコードが201 Createdであることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのクッキーとヘッダーに使用される。
   * @throws AssertionError ステータスコードが201 Createdでない場合
   */
  private void register(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .header("X-XSRF-TOKEN", xsrfToken)
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec.expectStatus().isCreated();
  }
}
