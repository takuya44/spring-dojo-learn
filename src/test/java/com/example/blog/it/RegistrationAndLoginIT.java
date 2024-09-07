package com.example.blog.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationAndLoginIT {

  private static final String TEST_USERNAME = "user99";
  private static final String TEST_PASSWORD = "password1";
  private static final String DUMMY_SESSION_ID = "session_id_1";
  private static final String SESSION_COOKIE_NAME = "SESSION";

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  public UserService userService;

  @BeforeEach
  public void beforeEach() {
    userService.delete(TEST_USERNAME);
  }

  @AfterEach
  public void afterEach() {
    userService.delete(TEST_USERNAME);
  }

  @Test
  public void integrationTest() {
    // ユーザー登録
    var xsrfToken = getRoot();
    register(xsrfToken);

    // ログイン失敗
    // Cookie に XSRF-TOKEN がない
    loginFailure_NoXSRFTokenInCookie(xsrfToken);

    // ヘッダーに X-XSRF-TOKEN がない
    loginFailure_NoXSRFTokenInHeader(xsrfToken);

    // Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が異なる
    loginFailure_DifferentToken(xsrfToken);

    // ユーザー名が存在しない
    loginFailure_GivenUsernameDoesNotExistInDatabase(xsrfToken);

    // パスワードがデータベースに保存されているパスワードと違う
    loginFailure_GivenPasswordDoesNotExistInDatabase(xsrfToken);

    // ログイン成功
    loginSuccess(xsrfToken);
    // ユーザー名がデータベースに存在する
    // パスワードがデータベースに保存されているパスワードと一致する
    // Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が一致する
    // → 200 OK が返る
    // → レスポンスに Set-Cookie: SESSION_COOKIE_NAME が返ってくる

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

  /**
   * ログイン成功時のテストを行うメソッド。
   *
   * <p>このメソッドは、ユーザー名とパスワードを用いてログインリクエストを送信し、XSRFトークンとセッションIDが適切に処理されることを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>ユーザー名とパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>XSRFトークンとSESSION_COOKIE_NAMEをクッキーおよびヘッダーに設定してリクエスト</li>
   *   <li>ステータスコードが200 OKであることを検証</li>
   *   <li>レスポンスに含まれる新しいSESSION_COOKIE_NAMEが空でなく、ダミーのセッションIDと異なることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのクッキーとヘッダーに使用される。
   * @throws AssertionError ステータスコードが200 OKでない場合、またはSESSION_COOKIE_NAMEの値が期待通りでない場合
   */
  private void loginSuccess(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        .header("X-XSRF-TOKEN", xsrfToken)
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isOk()
        .expectCookie().value(SESSION_COOKIE_NAME, v -> assertThat(v)
            .isNotBlank()
            .isNotEqualTo(DUMMY_SESSION_ID)
        );
  }

  /**
   * XSRFトークンがCookieに設定されていない場合のログイン失敗をテストするメソッド。
   *
   * <p>このメソッドは、XSRFトークンがCookieに存在しない場合、ログインリクエストが拒否されることを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>ユーザー名とパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>クッキーにXSRF-TOKENを設定せず、ヘッダーにのみXSRFトークンを設定</li>
   *   <li>SESSION_COOKIE_NAMEをクッキーに設定</li>
   *   <li>ステータスコードが403 Forbiddenであることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのヘッダーに使用されるが、クッキーには含まれない。
   * @throws AssertionError ステータスコードが403 Forbiddenでない場合
   */
  private void loginFailure_NoXSRFTokenInCookie(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        /*
        .cookie("XSRF-TOKEN", xsrfToken)
        */
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        .header("X-XSRF-TOKEN", xsrfToken)
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isForbidden();
  }

  /**
   * XSRFトークンがリクエストヘッダーに設定されていない場合のログイン失敗をテストするメソッド。
   *
   * <p>このメソッドは、XSRFトークンがヘッダーに存在しない場合、ログインリクエストが拒否されることを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>ユーザー名とパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>クッキーにXSRF-TOKENを設定し、ヘッダーには設定しない</li>
   *   <li>SESSION_COOKIE_NAMEをクッキーに設定</li>
   *   <li>ステータスコードが403 Forbiddenであることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのクッキーに使用されるが、ヘッダーには含まれない。
   * @throws AssertionError ステータスコードが403 Forbiddenでない場合
   */
  private void loginFailure_NoXSRFTokenInHeader(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        /*
        .header("X-XSRF-TOKEN", xsrfToken)
        */
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isForbidden();
  }

  /**
   * クッキーに設定されたXSRFトークンとヘッダーに設定されたXSRFトークンが異なる場合のログイン失敗をテストするメソッド。
   *
   * <p>このメソッドは、クッキーに含まれるXSRFトークンとヘッダーに送信されるXSRFトークンが異なる場合に、
   * ログインリクエストが拒否されることを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>ユーザー名とパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>クッキーにXSRF-TOKENを設定し、ヘッダーには異なる値のXSRF-TOKENを設定</li>
   *   <li>SESSION_COOKIE_NAMEをクッキーに設定</li>
   *   <li>ステータスコードが403 Forbiddenであることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken クッキーに設定されるXSRFトークンの値。ヘッダーにはこれと異なる値が使用される。
   * @throws AssertionError ステータスコードが403 Forbiddenでない場合
   */
  private void loginFailure_DifferentToken(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        .header("X-XSRF-TOKEN", xsrfToken + "_invalid")
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isForbidden();
  }

  /**
   * データベースに存在しないユーザー名が与えられた場合のログイン失敗をテストするメソッド。
   *
   * <p>このメソッドは、存在しないユーザー名を使用してログインリクエストが送信された場合に、
   * サーバーが 401 Unauthorized エラーを返すことを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>無効なユーザー名（存在しないユーザー名）と有効なパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>XSRF-TOKENおよびSESSION_COOKIE_NAMEをクッキーに設定し、ヘッダーにX-XSRF-TOKENを設定</li>
   *   <li>ステータスコードが 401 Unauthorized であることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのクッキーとヘッダーに使用される。
   * @throws AssertionError ステータスコードが 401 Unauthorized でない場合
   */
  private void loginFailure_GivenUsernameDoesNotExistInDatabase(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME + "_invalid", TEST_PASSWORD);

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        .header("X-XSRF-TOKEN", xsrfToken)
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isUnauthorized();
  }

  /**
   * データベースに存在しないパスワードが与えられた場合のログイン失敗をテストするメソッド。
   *
   * <p>このメソッドは、存在しない（無効な）パスワードを使用してログインリクエストが送信された場合に、
   * サーバーが 401 Unauthorized エラーを返すことを検証します。</p>
   *
   * <p>具体的には、次の手順を行います:
   * <ul>
   *   <li>有効なユーザー名と無効なパスワードを含むJSONリクエストボディを作成</li>
   *   <li>POSTリクエストを /login エンドポイントに対して送信</li>
   *   <li>XSRF-TOKENおよびSESSION_COOKIE_NAMEをクッキーに設定し、ヘッダーにX-XSRF-TOKENを設定</li>
   *   <li>ステータスコードが 401 Unauthorized であることを検証</li>
   * </ul>
   * </p>
   *
   * @param xsrfToken XSRFトークンの値。リクエストのクッキーとヘッダーに使用される。
   * @throws AssertionError ステータスコードが 401 Unauthorized でない場合
   */
  private void loginFailure_GivenPasswordDoesNotExistInDatabase(String xsrfToken) {
    // ## Arrange ##
    var bodyJson = String.format("""
        {
          "username": "%s",
          "password": "%s"
        }
        """, TEST_USERNAME, TEST_PASSWORD + "_invalid");

    // ## Act ##
    var responseSpec = webTestClient
        .post().uri("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .cookie("XSRF-TOKEN", xsrfToken)
        .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
        .header("X-XSRF-TOKEN", xsrfToken)
        .bodyValue(bodyJson)
        .exchange();

    // ## Assert ##
    responseSpec
        .expectStatus().isUnauthorized();
  }

}
