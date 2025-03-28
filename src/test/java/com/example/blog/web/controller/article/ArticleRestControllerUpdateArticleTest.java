package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
   * 日付時刻サービスのモック。
   *
   * <p>このフィールドは、Spring Boot のテストコンテキストで利用されるモックオブジェクトとして
   * {@link DateTimeService} を提供します。テスト実行時に、このモックが本来のサービスの代わりに 使用され、日付や時刻を固定するなどの制御が可能になります。</p>
   *
   * <p>{@link MockBean} アノテーションを使用することで、このモックオブジェクトが
   * Spring の依存性注入コンテナに登録されます。これにより、テスト対象のコードは通常のサービスの代わりに モックオブジェクトを使用します。</p>
   *
   * <p>使用例:</p>
   * <pre>{@code
   * when(mockDateTimeService.now())
   *     .thenReturn(LocalDateTime.of(2022, 1, 1, 12, 0));
   * }</pre>
   *
   * <p>この例では、`now()` メソッドが呼び出されると、固定された日時を返します。</p>
   */
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * テスト実行中に操作対象となる既存の記事エンティティ。 各テストケースで、作成済みの記事データを保持するために使用されます。
   */
  private ArticleEntity existingArticle;

  /**
   * テスト用に登録されたユーザーエンティティ。 記事作成時の作者情報や、認証処理でのユーザー情報として利用されます。
   */
  private UserEntity author;

  /**
   * 認証済みユーザーを模倣するためのオブジェクト。 ログイン状態を表現し、テスト内で認証されたユーザーとして利用されます。
   */
  private LoggedInUser loggedInAuthor;

  /**
   * MockMvc とサービスの初期化確認。
   * <p>依存関係が正しくセットアップされていることを確認します。</p>
   */
  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
    assertThat(articleService).isNotNull();
    assertThat(mockDateTimeService).isNotNull();
  }

  /**
   * 各テスト実行前に共通の初期化処理を行います。
   *
   * <p>
   * このメソッドでは、以下の初期設定を行います:
   * </p>
   * <ul>
   *   <li>
   *     日付の固定化: 記事の作成(create)と更新(update)時に異なる日時が設定されるようにモックを設定しています。<br>
   *     1回目の呼び出しで 2020/1/2 10:20、2回目の呼び出しで 2022/2/2 20:30 が返されるため、
   *     更新時のタイムスタンプが作成時より新しくなります。
   *   </li>
   *   <li>
   *     テスト用ユーザーの登録: ログイン済みユーザーを模倣するために、テスト用ユーザー情報を登録します。
   *   </li>
   *   <li>
   *     記事の作成: 登録したユーザーを使用して、初期状態のテスト用記事を作成します。
   *   </li>
   *   <li>
   *     認証情報の設定: テストで認証済みユーザーとして振る舞うための {@code LoggedInUser} オブジェクトを作成します。
   *   </li>
   * </ul>
   */
  @BeforeEach
  void beforeEach() {
    // 日付を固定：この値がDBに登録される
    // 1回目：create時、2回目：update時に実行される。結果、updateの方が最新になる
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 20, 30));

    // テストで使用するユーザー情報を作成: ログイン済みユーザーを模倣
    author = userService.register("test_username", "test_password");

    // 登録したユーザーを使用して、テスト用の記事を作成する。
    existingArticle = articleService.create(author.getId(), "test_title", "test_body");

    // 認証情報として利用するための LoggedInUser オブジェクトを作成する。
    loggedInAuthor = new LoggedInUser(author.getId(), author.getUsername(),
        author.getPassword(), true);
  }

  /**
   * PUT /articles/{articleId}: 記事の編集が成功する場合の動作をテストする。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>指定された記事IDに対する編集リクエストが正常に処理されること。</li>
   *   <li>レスポンスヘッダーに正しいContent-Type（application/json）が設定されていること。</li>
   *   <li>レスポンスボディに更新された記事データが正確に含まれること。</li>
   *   <li>作成日時（createdAt）が保持され、更新日時（updatedAt）が更新されること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>日時を固定してモックし、`create` と `update` のタイミングで異なる日時が設定されるようにする。</li>
   *   <li>テスト用のユーザーを登録し、そのユーザーの認証情報を用意。</li>
   *   <li>記事を作成し、そのIDを基に編集リクエストを送信。</li>
   *   <li>レスポンスのステータスコード、ヘッダー、ボディを検証。</li>
   *   <li>更新後のタイトル、本文、更新日時が正しく反映されていることを確認。</li>
   * </ol>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 記事の編集に成功する")
  void updateArticle_200() throws Exception {
    // ## Arrange ##
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

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
            .with(user(loggedInAuthor)) // 認証されたユーザーを設定
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
        .andExpect(jsonPath("$.author.id").value(author.getId()))
        .andExpect(jsonPath("$.author.username").value(author.getUsername()))
        .andExpect(jsonPath("$.createdAt").value(existingArticle.getCreatedAt().format(formatter)))
        .andExpect(
            jsonPath("$.updatedAt", greaterThan(existingArticle.getCreatedAt().format(formatter))))
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
            .with(user(loggedInAuthor)) // 認証されたユーザーを設定
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

  /**
   * PUT /articles/{articleId}: 自分が作成していない記事を編集しようとした場合の動作をテストする。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>他のユーザーが作成した記事を編集しようとした場合、403 Forbidden ステータスコードが返されること。</li>
   *   <li>レスポンスヘッダーに正しい Content-Type（application/problem+json）が設定されていること。</li>
   *   <li>レスポンスボディに適切なエラーメッセージが含まれること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>記事作成者となるユーザー（creator）を登録し、そのユーザーが記事を作成。</li>
   *   <li>別のログインユーザー（otherUser）を登録。</li>
   *   <li>別のログインユーザーが記事を編集しようとした場合、403 Forbidden が返されることを検証。</li>
   * </ol>
   *
   * <p>エラーメッセージは、RFC 7807 に準拠したフォーマットであることを確認します。</p>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 自分が作成した記事以外の記事を編集しようとしたとき、403を返す")
  void updateArticle_403Forbidden_userId() throws Exception {
    // ## Arrange ##
    // 記事作成者（creator）を登録し、記事を作成: @BeforeEachで定義済み。

    // 別のログインユーザー（otherUser）を登録
    var otherUser = userService.register("test_username2", "test_password2");
    var loggedInOtherUser = new LoggedInUser(otherUser.getId(), otherUser.getUsername(),
        otherUser.getPassword(), true);

    // JSON形式のリクエストボディを準備
    var bodyJson = """
        {
          "title": "test_title_updated",
          "body": "%test_body_updated"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", existingArticle.getId())
            .with(csrf()) // CSRF トークンを追加
            .with(user(loggedInOtherUser)) // 別のログインユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isForbidden()) // ステータスコードが 403 であることを確認
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.detail").value("リソースへのアクセスが拒否されました"))
        .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
    ;
  }

  /**
   * PUT /articles/{articleId}: CSRF トークンが付加されていない場合の動作をテストする。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>リクエストに CSRF トークンが付与されていない場合、403 Forbidden ステータスコードが返されること。</li>
   *   <li>レスポンスの Content-Type が RFC 7807 に準拠した <code>application/problem+json</code> であること。</li>
   *   <li>レスポンスボディに CSRF トークンエラーに関する適切なメッセージが含まれること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>日時を固定してモックし、<code>create</code> と <code>update</code> のタイミングで異なる日時が設定されるようにする。</li>
   *   <li>テスト用のユーザーを登録し、そのユーザーの認証情報を用意。</li>
   *   <li>記事を作成し、その ID を基に <code>PUT</code> リクエストを送信。</li>
   *   <li>リクエストに CSRF トークンを付与せずに送信し、403 Forbidden が返されることを検証。</li>
   * </ol>
   *
   * <p>エラーメッセージは RFC 7807 に準拠したフォーマットであることを確認します。</p>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: リクエストに CSRF トークンが付加されていないとき 403を返す")
  void updateArticle_403Forbidden_csrf() throws Exception {
    // ## Arrange ##
    // JSON形式のリクエストボディを準備
    var bodyJson = """
        {
          "title": "test_title_updated",
          "body": "test_body_updated"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", existingArticle.getId())
            // CSRF トークンを付与しない（.with(csrf()) をコメントアウト）
            .with(user(loggedInAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
        .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
    ;
  }

  /**
   * PUT /articles/{articleId}: 未ログインのユーザーが記事を編集しようとした場合の動作をテストする。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>未認証のユーザーが記事を編集しようとすると、401 Unauthorized ステータスコードが返されること。</li>
   *   <li>レスポンスの Content-Type が RFC 7807 に準拠した <code>application/problem+json</code> であること。</li>
   *   <li>レスポンスボディに適切なエラーメッセージが含まれること。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>日時を固定してモックし、<code>create</code> と <code>update</code> のタイミングで異なる日時が設定されるようにする。</li>
   *   <li>テスト用のユーザーを登録し、そのユーザーで記事を作成。</li>
   *   <li>ログインしていない状態で <code>PUT</code> リクエストを送信し、401 Unauthorized が返されることを検証。</li>
   * </ol>
   *
   * <p>エラーメッセージは RFC 7807 に準拠したフォーマットであることを確認します。</p>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 未ログインのとき、401 を返す")
  void updateArticle_401Unauthorized() throws Exception {
    // ## Arrange ##
    // JSON形式のリクエストボディを準備
    var bodyJson = """
        {
          "title": "test_title_updated",
          "body": "test_body_updated"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", existingArticle.getId())
            .with(csrf()) // CSRF トークンは付与するが、認証情報は設定しない
            // .with(user(loggedInAuthor)) // 認証ユーザーを設定しない（未ログイン状態）
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
        .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
    ;
  }

  /**
   * PUT /articles: リクエストの title フィールドがバリデーションNGの場合に 400 Bad Request を返すことを検証するテスト。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>リクエストボディの title フィールドが不正（空文字列）の場合、サーバーが 400 Bad Request を返すこと。</li>
   *   <li>レスポンスの Content-Type が RFC 7807 に準拠した <code>application/problem+json</code> であること。</li>
   *   <li>レスポンスボディに、"title" フィールドに起因するエラー詳細が含まれ、各エラーフィールド（status, detail, instance）が適切に設定されていること。</li>
   *   <li>レスポンスの instance プロパティには、リクエストURI（例: "/articles/{articleId}"）が設定されていること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>日時を固定して、記事作成時と更新時で異なるタイムスタンプが設定されるようにモックする。</li>
   *   <li>テスト用ユーザーを登録し、そのユーザーで記事を作成する。</li>
   *   <li>認証済み状態で PUT リクエストを送信するが、title フィールドが空であるためバリデーションエラーとなり、400 Bad Request が返されることを検証する。</li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("PUT /articles: リクエストの title フィールドがバリデーションNGのとき、400 BadRequest")
  void updateArticle_400BadRequest() throws Exception {
    // ## Arrange ##
    // JSON形式のリクエストボディを準備: title フィールドが空であるためバリデーションに失敗する
    var bodyJson = """
        {
          "title": "",
          "body": "test_body_updated"
        }
        """;

    // ## Act ##
    var actual = mockMvc.perform(
        put("/articles/{articleId}", existingArticle.getId())
            .with(csrf())
            .with(user(loggedInAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
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
        .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
        .andExpect(jsonPath("$.errors", hasItem(
            allOf(
                hasEntry("pointer", "#/title"), // "title" フィールドが原因であることを確認
                hasEntry("detail", "タイトルは1文字以上255文字以内で入力してください。")
            )
        )))
    ;
  }
}