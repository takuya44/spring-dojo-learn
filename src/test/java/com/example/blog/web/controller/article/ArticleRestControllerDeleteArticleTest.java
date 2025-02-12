package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

}