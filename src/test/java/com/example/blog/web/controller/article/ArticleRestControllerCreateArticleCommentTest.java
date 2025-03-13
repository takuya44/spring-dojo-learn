package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
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

  private ArticleEntity article;
  private UserEntity commentAuthor;
  private LoggedInUser loggedInCommentAuthor;

  @BeforeEach
  void beforeEach() {
    // 記事の作成者を登録し、記事を作成
    var articleAuthor = userService.register("test_username1", "test_password1");
    article = articleService.create(
        articleAuthor.getId(),
        "test_article_title",
        "test_article_body"
    );

    // コメントの作成者を登録し、認証情報を作成
    commentAuthor = userService.register("test_username2", "test_password2");
    loggedInCommentAuthor = new LoggedInUser(
        commentAuthor.getId(),
        commentAuthor.getUsername(),
        commentAuthor.getPassword(),
        commentAuthor.isEnabled()
    );
  }

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
    // @BeforeEachに移譲

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

  /**
   * POST /articles/{articleId}/comments エンドポイントのテストケースです。
   *
   * <p>
   * このテストは、リクエストボディの "body" フィールドが空の場合に、サーバーが 400 BadRequest を返すことを検証します。
   * </p>
   *
   * <ol>
   *   <li>
   *     <b>Arrange:</b>
   *     <ul>
   *       <li>記事作成者を登録し、テスト用の記事を作成します。</li>
   *       <li>コメント作成者を登録し、認証情報として使用する {@code LoggedInUser} を作成します。</li>
   *       <li>空の "body" フィールドを持つ JSON リクエストボディを定義します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Act:</b>
   *     <ul>
   *       <li>POST リクエストを実行し、指定した記事に対して空の "body" でコメント作成を試みます。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Assert:</b>
   *     <ul>
   *       <li>レスポンスが 400 BadRequest であることを検証します。</li>
   *       <li>レスポンスのコンテンツタイプが {@code MediaType.APPLICATION_PROBLEM_JSON} であることを確認します。</li>
   *       <li>レスポンスの JSON 内に、エラーに関する詳細情報（title, status, detail, instance, errors）が含まれていることを検証します。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception リクエスト実行中に発生する可能性のある例外
   * @see ArticleCommentController
   */
  @Test
  @DisplayName("POST /articles/{articleId}/comments: リクエストの body フィールドが空のとき、400 BadRequest")
  void createArticleComments_400BadRequest() throws Exception {
    // ## Arrange ##
    // @BeforeEachに移譲

    // 空の "body" フィールドを含む JSON リクエストボディを定義する
    var bodyJson = """
        {
          "body": ""
        }
        """;

    // ## Act ##
    // CSRF トークンと認証情報を付与して、指定した記事IDに対して POST リクエストを実行する
    var actual = mockMvc.perform(
        post("/articles/{articleId}/comments", article.getId())
            .with(csrf()) // CSRF トークンを含める
            .with(user(loggedInCommentAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    // レスポンスが 400 BadRequest であること、およびレスポンスの JSON 内容が期待通りのエラー情報を含むことを検証する
    actual
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(article.getId())))
        .andExpect(jsonPath("$.errors", hasItem(
            allOf(
                hasEntry("pointer", "#/body"), // エラーの原因が "body" フィールドであることを確認
                hasEntry("detail", "コメント本文は必須です。") // エラーメッセージが期待通りであることを確認
            )
        )))
    ;
  }
}