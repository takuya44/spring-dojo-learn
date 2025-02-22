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
 * `POST /articles/{articleId}/comments` エンドポイントの新規コメント作成機能をテストするクラス。
 *
 * <p>このクラスでは、記事へのコメント作成に関する REST API の動作を検証します。</p>
 *
 * <p>テストの目的:</p>
 * <ul>
 *   <li>新規コメントを作成できることを検証。</li>
 *   <li>作成成功時に 201 Created ステータスが返されることを確認。</li>
 *   <li>レスポンスヘッダーに `Location` が設定されることを確認。</li>
 *   <li>レスポンスボディに作成されたコメントの情報が正しく含まれることを確認。</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerCreateArticleCommentTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;
  @Autowired
  private ArticleService articleService;

  /**
   * テストのセットアップが正しく行われていることを検証する。ここが失敗するとまずテスト通らないため
   * <p>MockMvc、UserService、ArticleService のインスタンスが適切に注入されていることを確認します。</p>
   */
  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
    assertThat(articleService).isNotNull();
  }

  /**
   * `POST /articles/{articleId}/comments` にリクエストを送信し、コメントの作成が成功することをテストする。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>リクエストの結果として 201 Created が返されること。</li>
   *   <li>レスポンスヘッダーに `Location` が含まれ、適切な URI パターンであること。</li>
   *   <li>レスポンスボディに作成されたコメントの情報が正しく含まれること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>記事の作成者を登録し、記事を作成する。</li>
   *   <li>別のユーザーを登録し、コメントを投稿するための認証情報を作成する。</li>
   *   <li>コメントの内容を含む JSON リクエストボディを作成する。</li>
   *   <li>MockMvc を使用して `POST` リクエストを実行する。</li>
   *   <li>レスポンスのステータスコード、ヘッダー、およびレスポンスボディを検証する。</li>
   * </ol>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("POST /articles/{articleId}/comments: 新規コメントの作成に成功する")
  void createArticleComment_201Created() throws Exception {
    // ## Arrange ##
    // 記事の作成者を登録し、記事を作成
    var articleAuthor = userService.register("test_username1", "test_password1");
    var article = articleService.create(
        articleAuthor.getId(),
        "test_article_title",
        "test_article_body"
    );

    // コメントの作成者を登録し、認証情報を作成
    var commentAuthor = userService.register("test_username2", "test_password2");
    var loggedInCommentAuthor = new LoggedInUser(
        commentAuthor.getId(),
        commentAuthor.getUsername(),
        commentAuthor.getPassword(),
        commentAuthor.isEnabled()
    );

    // 期待されるコメントの内容
    var expectedBody = "記事にコメントをしました";
    var bodyJson = """
        {
          "body": "%s"
        }
        """.formatted(expectedBody);

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles/{articleId}/comments", article.getId())
            .with(csrf()) // CSRF トークンを含める
            .with(user(loggedInCommentAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isCreated()) // ステータスコード 201 Created を確認
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(header().string("Location", matchesPattern(
            "/articles/" + article.getId() + "/comments/\\d+"
        )))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.body").value(expectedBody))
        .andExpect(jsonPath("$.author.id").value(commentAuthor.getId()))
        .andExpect(jsonPath("$.author.username").value(commentAuthor.getUsername()))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
    ;
  }
}