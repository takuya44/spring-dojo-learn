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
class ArticleRestControllerCreateArticleTest {

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
    // テストで使用するユーザー情報を作成: ログイン済みユーザーを模倣
    var newUser = userService.register("test_username", "test_password");
    var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(),
        newUser.getPassword(), true);
    var expectedTitle = "test_title";
    var expectedBody = "test_body";

    // JSON形式のリクエストボディを準備
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
            .with(user(expectedUser)) // 認証されたユーザーを設定
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
        .andExpect(jsonPath("$.author.id").value(expectedUser.getUserId()))
        .andExpect(jsonPath("$.author.username").value(expectedUser.getUsername()))
        .andExpect(jsonPath("$.createdAt").isNotEmpty()) // UserServiceTestで検証済み
        .andExpect(jsonPath("$.updatedAt").isNotEmpty()) // UserServiceTestで検証済み
    ;
  }

  /**
   * POST /articles: リクエストの title フィールドがバリデーションNGのとき、400 Bad Request を返すテスト。
   *
   * <p>このテストでは、以下の条件でエラーが発生することを確認します:</p>
   * <ul>
   *   <li>title フィールドがバリデーションルールに違反している。</li>
   *   <li>サーバーが適切なエラーレスポンスを返す。</li>
   * </ul>
   *
   * <p>エラーレスポンスの詳細:</p>
   * <ul>
   *   <li>HTTP ステータスコード 400 Bad Request</li>
   *   <li>詳細なエラーメッセージを含む "errors" フィールド</li>
   * </ul>
   *
   * <p>このバリデーションルール（title は 1 文字以上 255 文字以内）は、記事のタイトルがユーザーにとって分かりやすく、適切な長さであることを保証するために重要です。
   * 長すぎるタイトルはユーザー体験を損ない、短すぎるタイトルは内容の曖昧さを引き起こす可能性があります。</p>
   *
   * @throws Exception テスト実行中に予期しない例外が発生した場合
   */
  @Test
  @DisplayName("POST /articles: リクエストの title フィールドがバリデーションNGのとき、400 BadRequest")
  void createArticles_400BadRequest() throws Exception {
    // ## Arrange ##
    // テストで使用するユーザー情報を作成: ログイン済みユーザーを模倣
    var newUser = userService.register("test_username", "test_password");
    var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(),
        newUser.getPassword(), true); // ログイン済みユーザーの模倣オブジェクトを生成

    // バリデーションに違反する title を含むリクエストボディを作成
    var bodyJson = """
        {
          "title": "",
          "body": "OK_body"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf()) // CSRF トークンを含める（セキュリティ設定で必須）:403エラー対策
            .with(user(expectedUser)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定: 415エラー対策
            .content(bodyJson)
    );

    // ## Assert ##
    // サーバーが 400 Bad Request を返すことを確認
    actual
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.instance").isEmpty())
        .andExpect(jsonPath("$.errors", hasItem(
            allOf(
                hasEntry("pointer", "#/title"), // "title" フィールドが原因であることを確認
                hasEntry("detail", "タイトルは1文字以上255文字以内で入力してください。")
            )
        )))
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