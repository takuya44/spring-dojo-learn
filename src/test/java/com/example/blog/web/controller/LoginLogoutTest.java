package com.example.blog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * テストクラス {@code LoginLogoutTest} は、ログインおよびログアウト機能に関する統合テストを提供します。
 *
 * <p>このクラスでは、以下の操作を検証します:</p>
 * <ul>
 *   <li>ユーザーのログイン成功時に正しいステータスコードが返されること。</li>
 *   <li>必要なヘッダーやリクエストボディが正しく処理されること。</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutTest {

  /**
   * MockMvcオブジェクト。エンドポイントへのHTTPリクエストをモックし、レスポンスを検証します。
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * テスト用のユーザーサービス。ユーザー登録などの準備処理に使用されます。
   */
  @Autowired
  private UserService userService;

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
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>ユーザーサービスを使用して、新しいユーザーを登録。</li>
   *   <li>MockMvcを使用して、登録済みユーザーの認証リクエストを送信。</li>
   *   <li>レスポンスのステータスコードを検証。</li>
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
    actual.andExpect(status().isOk());
  }
}
