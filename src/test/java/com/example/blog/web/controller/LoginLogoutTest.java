package com.example.blog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * テストクラス {@code LoginLogoutTest} は、ログインおよびログアウト機能に関する統合テストを提供します。
 *
 * <p>このクラスでは、以下の操作を検証します:</p>
 * <ul>
 *   <li>ユーザーのログイン成功時に正しいステータスコードが返されること。</li>
 *   <li>正しいCSRFトークンやリクエストボディが必要であること。</li>
 *   <li>認証情報が適切に管理されていること。</li>
 * </ul>
 *
 * <p>テスト環境のセットアップには、Spring Securityを適用した {@link MockMvc} を使用します。</p>
 */
@SpringBootTest
// @AutoConfigureMockMvc ★★ 自力で MockMvc を組み上げるのでこのアノテーショは不要
@Transactional
class LoginLogoutTest {

  /**
   * MockMvcオブジェクト。エンドポイントへのHTTPリクエストをモックし、レスポンスを検証します。
   */
  //  @Autowired ★★ 自力で MockMvc を組み上げるのでこのアノテーショは不要自分でセットアップする
  private MockMvc mockMvc;

  /**
   * テスト用のユーザーサービス。ユーザー登録などの準備処理に使用されます。
   */
  @Autowired
  private UserService userService;

  /**
   * WebApplicationContextオブジェクト。
   * <p>MockMvcの構築に使用されます。</p>
   */
  @Autowired
  private WebApplicationContext context;

  /**
   * 各テスト実行前にMockMvcを初期化します。
   *
   * <p>このメソッドでは、Spring Securityを適用したMockMvcを構築します。</p>
   */
  @BeforeEach
  void beforeEach() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity()) // Spring Securityの設定を適用
        .build();
  }

  /**
   * MockMvcオブジェクトが正しく初期化されていることを確認するテスト。
   *
   * <p>このテストは、MockMvcオブジェクトが {@code null} ではないことを検証し、
   * テスト環境が正しく構築されているかを確認します。</p>
   */
  @Test
  void mockMvc() {
    assertThat(mockMvc).isNotNull();
  }

  /**
   * ログイン成功時の動作を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>登録済みのユーザーが正しいユーザー名とパスワードでログインできること。</li>
   *   <li>HTTPステータスコードが200 OKであること。</li>
   *   <li>認証が成功し、適切なユーザー名が認証情報として使用されること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>ユーザーサービスを使用して、新しいユーザーを登録。</li>
   *   <li>MockMvcを使用して、登録済みユーザーの認証リクエストを送信。</li>
   *   <li>レスポンスのステータスコードおよび認証情報を検証。</li>
   * </ol>
   *
   * @throws Exception HTTPリクエストの送信やレスポンス検証中にエラーが発生した場合
   */
  @Test
  @DisplayName("POST /login: ログイン成功")
  void login_success() throws Exception {
    // ## Arrange ##
    // ユーザー名とパスワードを指定して新しいユーザーを登録
    var username = "username123";
    var password = "password123";
    userService.register(username, password);

    // ログインリクエストのJSONボディを作成
    var requestBody = """
        {
            "username": "%s",
            "password": "%s"
        }
        """.formatted(username, password);

    // ## Act ##
    // MockMvcを使用してPOSTリクエストを送信
    var actual = mockMvc.perform(
        post("/login")
            .with(csrf()) // CSRFトークンを含める（Spring Securityの設定が有効な場合に必要）
            .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定
            .content(requestBody) // リクエストボディにログイン情報を設定
    );

    // ## Assert ##
    actual
        .andExpect(status().isOk()) // ステータスコード200 OKを確認
        .andExpect(authenticated().withUsername(username)); // 認証されたユーザー名を確認
  }

  /**
   * ログイン失敗時の動作を検証するパラメータ化テスト。
   *
   * <p>このテストでは、以下のケースで認証が失敗することを検証します:</p>
   * <ul>
   *   <li>無効なユーザー名を指定した場合。</li>
   *   <li>無効なパスワードを指定した場合。</li>
   *   <li>ユーザー名が省略された場合。</li>
   *   <li>パスワードが省略された場合。</li>
   *   <li>空のリクエストボディを送信した場合。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>ユーザーサービスを使用して、正しいユーザー名とパスワードで新しいユーザーを登録。</li>
   *   <li>無効なリクエストデータを用いて認証リクエストを送信。</li>
   *   <li>レスポンスのステータスコードおよび認証状態を検証。</li>
   * </ol>
   *
   * @param requestBody テストケースごとのリクエストボディ（無効なデータを含む）
   * @throws Exception HTTPリクエストの送信やレスポンス検証中にエラーが発生した場合
   */
  @ParameterizedTest
  @DisplayName("POST /login: ログイン失敗 > 色々なケース")
  @ValueSource(strings = {
      "{ \"username\": \"__invalid__\", \"password\": \"password123\" }",
      "{ \"username\": \"username123\", \"password\": \"__invalid__\" }",
      "{                                \"password\": \"password123\" }",
      "{ \"username\": \"username123\",                               }",
      "{                                                              }",
      "",
  })
  void login_failure(String requestBody) throws Exception {
    // ## Arrange ##
    // ユーザー名とパスワードを指定して新しいユーザーを登録
    userService.register("username123", "password123");

    // ## Act ##
    // MockMvcを使用してPOSTリクエストを送信
    var actual = mockMvc.perform(
        post("/login")
            .with(csrf()) // CSRFトークンを含める（Spring Securityの設定が有効な場合に必要）
            .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定
            .content(requestBody) // リクエストボディにログイン情報を設定
    );

    // ## Assert ##
    actual
        .andExpect(status().isUnauthorized()) // ステータスコードが401 Unauthorizedであることを確認
        .andExpect(unauthenticated()); // 認証情報が設定されないことを確認
  }
}
