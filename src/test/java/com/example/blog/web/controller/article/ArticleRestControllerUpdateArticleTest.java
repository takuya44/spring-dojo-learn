package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import java.time.format.DateTimeFormatter;
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

    // 日付を固定：この値がDBに登録される
    // 1回目：create時、2回目：update時に実行される。結果、updateの方が最新になる
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 20, 30));

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
    // 日付を固定：この値がDBに登録される（今回のテストでは日時の検証は行わない）
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 20, 30));

    // 記事作成者（creator）を登録し、記事を作成
    var creator = userService.register("test_username1", "test_password1");
    var existingArticle = articleService.create(creator.getId(), "test_title", "test_body");

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
    // 日付を固定：この値がDBに登録される
    // 1回目：create時、2回目：update時に実行される。結果、updateの方が最新になる
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 20, 30));

    // テストで使用するユーザー情報を作成: ログイン済みユーザーを模倣
    var newUser = userService.register("test_username", "test_password");
    var existingArticle = articleService.create(newUser.getId(), "test_title", "test_body");

    var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(),
        newUser.getPassword(), true);

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
            .with(user(expectedUser)) // 認証されたユーザーを設定
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
}