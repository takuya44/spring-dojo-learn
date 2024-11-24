package com.example.blog.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link UserRestController} の統合テストクラス。
 *
 * <p>このテストクラスでは、Spring MVCのMockMvcを使用して、ユーザー関連のエンドポイントをテストします。
 * ログイン済みユーザーがアクセスする場合の応答をテストします。</p>
 *
 * <p>テストは {@link SpringBootTest} と {@link AutoConfigureMockMvc} アノテーションを使用して構成され、
 * MockMvcを通じてリクエストを模擬し、エンドポイントの応答を検証します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRestControllerTest {

  private static final String MOCK_USERNAME = "user1";

  /**
   * Spring MVCのテストを行うための {@link MockMvc} オブジェクト。
   * <p>MockMvcを使用して、コントローラーの統合テストを行います。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * MockMvcが正しく初期化されていることを検証するテスト。
   * <p>MockMvcオブジェクトがnullでないことを確認し、テスト環境が正常にセットアップされていることを確認します。</p>
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();
  }

  /**
   * /users/me: ログイン済みユーザーがアクセスしたとき、200 OK でユーザー名を返すテスト。
   *
   * <p>このテストでは、@WithMockUser アノテーションを使用して、仮のユーザー名を設定した状態でエンドポイントにアクセスします。
   * レスポンスとして、200 OKが返され、ユーザー名が含まれていることを検証します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("/users/me: ログイン済みユーザーがアクセスすると、200 OK でユーザー名を返す")
  @WithMockUser(username = MOCK_USERNAME)
  public void usersMe_return200() throws Exception {
    // ## Arrange ##

    // ## Act ##
    // GETリクエストを送信してレスポンスを取得
    var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

    // ## Assert ##
    // ステータスコード200 OKが返され、レスポンスの内容にユーザー名が含まれることを検証
    actual
        .andExpect(status().isOk())
        .andExpect(content().bytes(MOCK_USERNAME.getBytes()));

  }

  /**
   * /users/me: 未ログインユーザーがアクセスしたとき、403 Forbidden を返すテスト。
   *
   * <p>このテストでは、認証されていないユーザーが /users/me エンドポイントにアクセスした際に、
   * ステータスコード403 Forbiddenが返されることを確認します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("/users/me: 未ログインユーザーがアクセスすると、403 Forbidden を返す")
  public void usersMe_return403() throws Exception {
    // ## Arrange ##
    // 未ログイン状態でGETリクエストを作成：@WithMockUser(username = MOCK_USERNAME)なし

    // ## Act ##
    var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

    // ## Assert ##
    // ステータスコード403 Forbiddenが返されることを検証
    actual.andExpect(status().isForbidden());

  }

  /**
   * POST /users: ユーザー作成成功時の動作をテストする。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>ステータスコード 201 (Created) が返されること</li>
   *   <li>ユーザー作成に必要なJSON形式のデータをリクエストボディとして送信する</li>
   *   <li>リクエストにはCSRFトークンが含まれている</li>
   * </ul>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("POST /users：ユーザー作成に成功すると、レスポンスの Location ヘッダー、ボディが設定される")
  public void createUser_success() throws Exception {
    // ## Arrange ##
    // テスト用のユーザーデータをJSON形式で作成
    String newUserJson = """
        {
            "username": "username123",
            "password": "password123"
        }
        """;

    // ## Act ##
    // MockMvcを使用してPOSTリクエストを作成し、エンドポイントに送信
    var actual = mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定: 415エラー対策
        .with(csrf()) // CSRFトークンを含める（セキュリティ設定で必須）:403エラー対策
        .content(newUserJson)); // リクエストボディとしてユーザーデータを送信

    // ## Assert ##
    // ステータスコードが201 Createdであることを確認
    actual
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", matchesPattern("/users/\\d+")));
    // TODO: ここに レスポンスボディの検証を追加することも可能
  }
}