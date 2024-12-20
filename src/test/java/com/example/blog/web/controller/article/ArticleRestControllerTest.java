package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;

  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
  }

  /**
   * 記事の新規作成成功時の動作を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>ユーザーがCSRFトークンを付与したリクエストを送信し、記事の新規作成に成功すること。</li>
   *   <li>HTTPステータスコードが201 Createdであること。</li>
   *   <li>レスポンスヘッダーに記事のURLが含まれていること。</li>
   *   <li>レスポンスボディが正しい記事データを含むこと。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>リクエストボディに含める記事データ（タイトル、本文）を準備。</li>
   *   <li>MockMvcを使用して記事作成リクエストを送信。</li>
   *   <li>レスポンスのステータスコード、ヘッダー、ボディを検証。</li>
   * </ol>
   *
   * @throws Exception リクエスト送信やレスポンス検証中にエラーが発生した場合
   */
  @Test
  @DisplayName("POST /articles: 記事の新規作成に成功する")
  void createArticle_success() throws Exception {
    // ## Arrange ##
    // 記事データを準備
    var expectedTitle = "test_title";
    var expectedBody = "test_body";
    var expectedUsername = "user1";
    var bodyJson = """
        {
          "title": "%s",
          "body": "%s"
        }
        """.formatted(expectedTitle, expectedBody);

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf())
            .with(user(expectedUsername))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(header().string("Location", matchesPattern("/articles/\\d+")))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.title").value(expectedTitle))
        .andExpect(jsonPath("$.body").value(expectedBody))
        .andExpect(jsonPath("$.author.id").isNumber())
        .andExpect(jsonPath("$.author.username").value(expectedUsername))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.updatedAt").isNotEmpty())
    ;
  }

  @Test
  @DisplayName("POST /articles: 未ログインのとき、401 Unauthorized を返す")
  void createArticle_401Unauthorized() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf())
        // .with(user("user1")) // 未ログイン状態でテストするため、コメントアウトしている
    );

    // ## Assert ##
    actual
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
        .andExpect(jsonPath("$.instance").value("/articles"))
    ;
  }

  /**
   * POST /articles: CSRFトークンが付与されていない場合の動作を検証するテスト。
   *
   * <p>このテストでは、リクエストにCSRFトークンが付与されていない場合にエラーレスポンスが返されることを確認します。</p>
   *
   * <p>テスト内容:</p>
   * <ul>
   *   <li>CSRFトークンなしでリクエストを送信すると、ステータスコード403 Forbiddenが返されること。</li>
   *   <li>レスポンスに適切なエラーメッセージが含まれていること。</li>
   * </ul>
   *
   * <p>テスト手順:</p>
   * <ol>
   *   <li>認証情報を付与したPOSTリクエストを送信するが、CSRFトークンは付与しない。</li>
   *   <li>レスポンスのステータスコードが403 Forbiddenであることを確認。</li>
   *   <li>レスポンスボディにエラーメッセージが正しく含まれていることを検証。</li>
   * </ol>
   *
   * @throws Exception リクエスト処理中にエラーが発生した場合
   */
  @Test
  @DisplayName("POST /articles: リクエストに CSRFトークンが付与されていない場合は 403 Forbidden を返す")
  void createArticle_403Forbidden() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            // .with(csrf()) // CSRFトークンを付与しない
            .with(user("user1"))
    );

    // ## Assert ##
    actual
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
        .andExpect(jsonPath("$.instance").value("/articles"))
    ;
  }
}