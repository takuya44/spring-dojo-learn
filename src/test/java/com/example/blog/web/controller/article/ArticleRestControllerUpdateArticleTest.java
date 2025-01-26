package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArticleRestController の PUT /articles/{articleId} エンドポイントに関するテストクラス。
 *
 * <p>このクラスでは、記事の編集機能が正しく動作するかを検証します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerUpdateArticleTest {

  /**
   * MockMvc オブジェクト。
   * <p>HTTP リクエストをモックしてコントローラーをテストするために使用します。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * ユーザー管理サービス。
   * <p>テストユーザーの作成や登録に使用します。</p>
   */
  @Autowired
  private UserService userService;

  /**
   * 記事管理サービス。
   * <p>テスト用の記事データの作成や取得に使用します。</p>
   */
  @Autowired
  private ArticleService articleService;


  /**
   * MockMvc とサービスの初期化確認。
   * <p>依存関係が正しくセットアップされていることを確認します。</p>
   */
  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
    assertThat(articleService).isNotNull();
  }

  /**
   * PUT /articles/{articleId}: 記事編集の正常系テスト。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>認証されたユーザーが既存の記事を編集できること。</li>
   *   <li>レスポンスにステータスコード 200 OK が含まれること。</li>
   *   <li>レスポンスボディに編集後のタイトルと本文が反映されること。</li>
   *   <li>レスポンスに正しい作成者情報が含まれること。</li>
   *   <li>更新日時が作成日時より後になっていること。</li>
   * </ul>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 記事の編集に成功する")
  void updateArticle_200() throws Exception {
    // ## Arrange ##
    // テストで使用するユーザー情報を作成: ログイン済みユーザーを模倣
    var newUser = userService.register("test_username", "test_password");
    var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(),
        newUser.getPassword(), true);
    var existingArticle = articleService.create(newUser.getId(), "test_title", "test_body");
    var updatedTitle = existingArticle.getTitle() + "_updated";
    var updatedBody = existingArticle.getBody() + "_updated";

    // JSON形式のリクエストボディを準備
    var bodyJson = """
        {
          "title": "%s",
          "body": "%s"
        }
        """.formatted(updatedTitle, updatedBody);

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", existingArticle.getId())
            .with(csrf())
            .with(user(expectedUser)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(existingArticle.getId()))
        .andExpect(jsonPath("$.title").value(updatedTitle))
        .andExpect(jsonPath("$.body").value(updatedBody))
        .andExpect(jsonPath("$.author.id").value(expectedUser.getUserId()))
        .andExpect(jsonPath("$.author.username").value(expectedUser.getUsername()))
        .andExpect(jsonPath("$.createdAt").value(existingArticle.getCreatedAt().toString()))
        .andExpect(jsonPath("$.updatedAt", greaterThan(existingArticle.getCreatedAt().toString())))
    ;
  }

  /**
   * PUT /articles/{articleId}: 指定された記事IDが存在しない場合の動作をテストするメソッド。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>存在しない記事IDを指定した場合、エンドポイントが 404 Not Found を返すこと。</li>
   *   <li>レスポンスヘッダーに正しい Content-Type（application/problem+json）が設定されること。</li>
   *   <li>レスポンスボディに適切なエラーメッセージが含まれること。</li>
   *   <li>エラーレスポンスの形式が RFC 7807 に準拠していること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>ユーザーを登録し、認証情報を作成。</li>
   *   <li>更新対象の記事IDとして存在しないID（{@code 0}）を設定。</li>
   *   <li>PUT リクエストを送信してレスポンスを取得。</li>
   *   <li>レスポンスのステータスコード、ヘッダー、ボディを検証。</li>
   * </ol>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>データベースに3件のデータが存在する。</li>
   *   <li>指定された記事IDは存在しない（無効なIDとして {@code 0} を使用）。</li>
   * </ul>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 指定されたIDの記事が存在しないとき、404を返す")
  void updateArticle_404NotFound() throws Exception {
    // ## Arrange ## 前提：DBに３件データある
    var invalidArticleId = 0;
    var newUser = userService.register("test_username", "test_password");
    var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(),
        newUser.getPassword(), true);

    // JSON形式のリクエストボディを準備
    var bodyJson = """
        {
          "title": "test_title_update",
          "body": "test_body_updated"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", invalidArticleId)
            .with(csrf())
            .with(user(expectedUser)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isNotFound()) // ステータスコードが 404 であることを確認
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("NotFound"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("リソースが見つかりません"))
        .andExpect(jsonPath("$.instance").value("/articles/" + invalidArticleId))
    ;
  }
}