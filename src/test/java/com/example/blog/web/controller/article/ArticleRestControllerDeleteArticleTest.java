package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerDeleteArticleTest {

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
   * DELETE /articles/{articleId}: 記事の削除に成功することを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>認証済みユーザーとして、指定された記事IDに対して DELETE リクエストを送信すると、
   *       サーバーが 204 No Content のステータスコードを返すこと。</li>
   *   <li>レスポンスボディが空であること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange: 削除対象の記事（existingArticle）のIDおよび認証情報（loggedInAuthor）は事前にセットアップ済みです。
   *   </li>
   *   <li>
   *     Act: DELETE リクエストを送信し、サーバーのレスポンスを取得します。
   *   </li>
   *   <li>
   *     Assert: レスポンスの HTTP ステータスが 204 No Content であり、レスポンスボディが空であることを検証します。
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("DELETE /articles/{articleId}: 記事の削除に成功する")
  void deleteArticle_204NoContent() throws Exception {
    // ## Arrange ##

    // ## Act ##
    // 認証済みのユーザー (loggedInAuthor) として、指定された記事ID (existingArticle.getId()) の記事削除リクエストを送信します。
    var actual = mockMvc.perform(
        delete("/articles/{articleId}", existingArticle.getId())
            .with(csrf()) // CSRF トークンを付与してセキュリティ対策
            .with(user(loggedInAuthor)) // 認証されたユーザーを設定
            .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-Typeを指定
    );

    // ## Assert ##
    // サーバーが 204 No Content のステータスを返し、レスポンスボディが空であることを検証します。
    actual
        .andExpect(status().isNoContent()) // HTTPステータスが 204 であることを確認
        .andExpect(content().string(is(emptyString()))) // レスポンスボディが空であることを確認
    ;
  }

  /**
   * DELETE /articles/{articleId}: 未ログイン状態で記事削除リクエストを送信した場合、 401 Unauthorized を返すことを検証するテストです。
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     <strong>Arrange:</strong>
   *     <ul>
   *       <li>削除対象の記事のID (existingArticle.getId()) は、事前にセットアップ済みです。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     <strong>Act:</strong>
   *     <ul>
   *       <li>
   *         認証情報を付与せず、CSRFトークンのみを含むDELETEリクエストを送信することで、未ログイン状態をシミュレートします。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <strong>Assert:</strong>
   *     <ul>
   *       <li>
   *         サーバーがHTTPステータス 401 Unauthorized を返すこと。
   *       </li>
   *       <li>
   *         レスポンスのContent-TypeがRFC7807に準拠した <code>application/problem+json</code> であること。
   *       </li>
   *       <li>
   *         レスポンスボディに、エラーの詳細情報（title, status, detail, instance）が正しく設定されていること。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("DELETE /articles/{articleId}: 未ログインのとき、401 Unauthorized を返す")
  void deleteArticle_401Unauthorized() throws Exception {
    // ## Arrange ##

    // ## Act ##
    // 認証情報を含めずに、CSRFトークンのみ付与したDELETEリクエストを送信することで、未ログイン状態を再現
    var actual = mockMvc.perform(
        delete("/articles/{articleId}", existingArticle.getId())
            .with(csrf()) // CSRF トークンは付与するが、認証情報は設定しない
            // .with(user(loggedInAuthor)) // 認証ユーザーを設定しない（未ログイン状態）
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // 送信したリクエストに対して、以下の内容を検証します：
    // ・HTTPステータスが401 Unauthorizedであること
    // ・レスポンスのContent-Typeが application/problem+json であること
    // ・レスポンスボディ内に、エラータイトル、ステータス、詳細メッセージ、リクエストURIが正しく設定されていること
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
   * DELETE /articles/{articleId}: 自分が作成した記事以外の記事を削除しようとした場合に、403 Forbidden を返すことを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>記事作成者は @BeforeEach により既に登録・作成されており、その記事に対して別ユーザーが削除リクエストを送信する。</li>
   *   <li>リクエスト送信時に、CSRF トークンが付与されているが、認証ユーザーとしては別のユーザー (otherUser) が設定されている。</li>
   *   <li>削除リクエストに対して、サーバーが 403 Forbidden を返すこと。</li>
   *   <li>レスポンスの Content-Type が RFC 7807 に準拠した <code>application/problem+json</code> であること。</li>
   *   <li>レスポンスボディ内に、エラータイトルが "Forbidden"、ステータスが 403、詳細メッセージが "リソースへのアクセスが拒否されました"、
   *       リクエスト URI が正しく設定されていることが検証される。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>@BeforeEach で設定された記事作成者による記事 (existingArticle) が存在する。</li>
   *       <li>別のユーザー (otherUser) を登録し、そのユーザー情報を基に LoggedInUser オブジェクトを生成する。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>DELETE リクエストを送信し、認証情報に otherUser を設定することで、作成者以外のユーザーからの削除リクエストをシミュレートする。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>HTTP ステータスが 403 Forbidden であることを検証する。</li>
   *       <li>レスポンスの Content-Type が <code>application/problem+json</code> であることを検証する。</li>
   *       <li>レスポンスボディ内のエラー情報 (title, status, detail, instance) が期待通りであることを検証する。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("DELETE /articles/{articleId}: 自分が作成した記事以外の記事を編削除しようとしたとき、403を返す")
  void updateArticle_403Forbidden_authorId() throws Exception {
    // ## Arrange ##
    // @BeforeEachで定義済みの作成者による記事が既に存在します。
    // 別のログインユーザー (otherUser) を登録し、そのユーザー情報から LoggedInUser オブジェクトを生成する。
    var otherUser = userService.register("test_username2", "test_password2");
    var loggedInOtherUser = new LoggedInUser(otherUser.getId(), otherUser.getUsername(),
        otherUser.getPassword(), true);

    // ## Act ##
    // DELETE リクエストを送信。CSRF トークンは付与するが、認証ユーザーは otherUser として設定することで、作成者以外からのリクエストをシミュレートする。
    var actual = mockMvc.perform(
        delete("/articles/{articleId}", existingArticle.getId())
            .with(csrf()) // CSRF トークンを追加
            .with(user(loggedInOtherUser)) // 別のログインユーザーを設定
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // サーバーが 403 Forbidden を返し、レスポンスが正しい問題詳細フォーマット (application/problem+json) であることを検証する。
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
   * DELETE /articles/{articleId}: リクエストに CSRF トークンが付加されていない場合、 サーバーが 403 Forbidden
   * を返すことを検証するテストです。
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>削除対象の記事 (existingArticle) と認証済みユーザー (loggedInAuthor) は事前にセットアップ済み。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>
   *         CSRF トークンを付与せずに DELETE リクエストを送信することで、CSRF 保護が働く状態をシミュレートします。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>HTTP ステータスが 403 Forbidden であること。</li>
   *       <li>レスポンスの Content-Type が <code>application/problem+json</code> であること。</li>
   *       <li>
   *         レスポンスボディに、エラー情報が正しく設定されていること（タイトルが "Forbidden"、ステータスが 403、
   *         詳細メッセージが "CSRFトークンが不正です"、instance が削除対象の記事の URI になっている）。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("DELETE /articles/{articleId}: リクエストに CSRF トークンが付加されていないとき 403 Forbidden を返す")
  void deleteArticle_403Forbidden_csrf() throws Exception {
    // ## Arrange ##

    // ## Act ##
    // 認証済みユーザーとしてリクエストを送信するが、CSRF トークンは付与しないため、未許可状態が発生します。
    var actual = mockMvc.perform(
        delete("/articles/{articleId}", existingArticle.getId())
            // .with(csrf()) // CSRF トークンを付与しないことで、セキュリティフィルターが403を返す
            .with(user(loggedInAuthor))
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // サーバーが 403 Forbidden を返し、レスポンスボディに期待されるエラー情報が含まれていることを検証します。
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