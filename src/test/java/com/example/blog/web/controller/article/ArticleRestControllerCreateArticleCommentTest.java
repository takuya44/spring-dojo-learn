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

  /**
   * 未ログイン状態で記事コメント作成エンドポイントにリクエストを送信した場合、 サーバーが 401 Unauthorized を返すことを検証するテストです。
   *
   * <p>
   * このテストでは、以下の手順で動作を確認します:
   * </p>
   * <ol>
   *   <li>
   *     <b>Arrange:</b>
   *     <ul>
   *       <li>
   *         必要なデータ（記事情報など）の初期化は {@code @BeforeEach} にて実施済みです。
   *       </li>
   *       <li>
   *         テスト用のリクエストボディとして、コメントの内容（expectedBody）を含む JSON を定義します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Act:</b>
   *     <ul>
   *       <li>
   *         未ログイン状態（ユーザー情報を付与しない）で、CSRFトークン付きのPOSTリクエストを実行し、
   *         指定した記事に対してコメント作成リクエストを送信します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Assert:</b>
   *     <ul>
   *       <li>レスポンスが 401 Unauthorized であることを検証します。</li>
   *       <li>レスポンスのコンテンツタイプが {@code MediaType.APPLICATION_PROBLEM_JSON} であることを確認します。</li>
   *       <li>レスポンスのJSON内容に、エラーメッセージやステータスコード、インスタンスの情報が正しく含まれていることを検証します。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception リクエスト実行中に発生した例外
   */
  @Test
  @DisplayName("POST /articles/{articleId}/comments: 未ログインのとき、401 Unauthorized を返す")
  void createArticleComments_401Unauthorized() throws Exception {
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
            // .with(user(loggedInCommentAuthor)) // 未ログイン状態を再現
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
        .andExpect(jsonPath("$.instance").value(
            "/articles/%d/comments".formatted(article.getId())
        ));
  }

  /**
   * POST /articles/{articleId}/comments エンドポイントに対して、CSRFトークンが付加されていない状態でリクエストを送信した場合、 サーバーが 403
   * Forbidden を返すことを検証するテストです。
   *
   * <p>
   * このテストでは以下の手順で処理を実施します:
   * </p>
   * <ol>
   *   <li>
   *     <b>Arrange:</b>
   *     <ul>
   *       <li>
   *         テスト対象のデータ（記事情報など）の初期化は {@code @BeforeEach} にて実施済みです。
   *       </li>
   *       <li>
   *         リクエストボディとして、期待するコメント内容を含む JSON を生成します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Act:</b>
   *     <ul>
   *       <li>
   *         認証済みのユーザー情報は付与しつつも、CSRFトークンを欠いた状態で POST リクエストを送信します。
   *         これにより、CSRFトークンがない場合のサーバーの動作を確認します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Assert:</b>
   *     <ul>
   *       <li>レスポンスのステータスコードが 403 Forbidden であることを検証します。</li>
   *       <li>レスポンスのコンテンツタイプが {@code MediaType.APPLICATION_PROBLEM_JSON} であることを確認します。</li>
   *       <li>レスポンスの JSON に、エラーメッセージやステータスコード、インスタンス情報が正しく含まれていることを検証します。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception リクエスト実行中に発生する可能性のある例外
   */
  @Test
  @DisplayName("POST /articles/{articleId}/comments: リクエストに CSRF トークンが付加されていないとき 403 Forbidden を返す")
  void createArticleComments_403Forbidden() throws Exception {
    // ## Arrange ##
    // 初期化処理は @BeforeEach にて実施済み

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
            // .with(csrf()) は付与せず、CSRFトークンの欠如をシミュレート
            .with(user(loggedInCommentAuthor))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    // レスポンスが 403 Forbidden であることを検証
    actual
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
        .andExpect(jsonPath("$.instance").value(
            "/articles/%d/comments".formatted(article.getId())
        ));
    ;
  }

  /**
   * POST /articles/{articleId}/comments エンドポイントに対して、 指定された記事IDが存在しない場合に404 Not
   * Foundを返すことを検証するテストです。
   *
   * <p>
   * このテストでは以下の手順で動作を確認します:
   * </p>
   * <ol>
   *   <li>
   *     <b>Arrange:</b>
   *     <ul>
   *       <li>存在しない記事ID（ここでは0）を使用して、テスト対象のリソースが存在しない状態を再現します。</li>
   *       <li>リクエストボディとして、期待されるコメント内容を含むJSONを生成します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Act:</b>
   *     <ul>
   *       <li>
   *         CSRFトークンおよび認証済みユーザー情報を付与して、指定された存在しない記事IDに対してPOSTリクエストを送信します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <b>Assert:</b>
   *     <ul>
   *       <li>レスポンスが404 Not Foundであることを検証します。</li>
   *       <li>レスポンスのコンテンツタイプが {@code MediaType.APPLICATION_PROBLEM_JSON} であることを確認します。</li>
   *       <li>レスポンスJSONに、エラーメッセージ（title, status, detail, instance）が正しく含まれていることを検証します。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception リクエスト実行中に発生する例外
   */
  @Test
  @DisplayName("POST /articles/{articleId}/comments: 指定されたIDの記事が存在しないとき、404を返す")
  void createArticleComment_404NotFound() throws Exception {
    // ## Arrange ##
    // 存在しない記事IDを定義
    var invalidArticleId = 0;

    // リクエストボディのJSON文字列を生成
    var bodyJson = """
        {
          "body": "%s"
        }
        """.formatted("記事にコメントをしました");

    // ## Act ##
    // レスポンスが404 Not Foundであることを検証
    var actual = mockMvc.perform(
        post("/articles/{articleId}/comments", invalidArticleId)
            .with(csrf()) // CSRF トークンを含める
            .with(user(loggedInCommentAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("NotFound"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("リソースが見つかりません"))
        .andExpect(
            jsonPath("$.instance").value("/articles/%d/comments".formatted(invalidArticleId)))
    ;
  }
}