package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

/**
 * {@link ArticleCommentService} のユニットテストクラス。
 *
 * <p>このクラスでは、記事のコメント機能が正しく動作することを検証する。</p>
 */
@MybatisDefaultDatasourceTest
@Import({
    ArticleCommentService.class,
    ArticleService.class,
    UserService.class,
    PasswordEncoderConfig.class
})
class ArticleCommentServiceTest {

  /**
   * モック化した日時サービス
   */
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * ユーザー管理サービス
   */
  @Autowired
  private UserService userService;

  /**
   * 記事管理サービス
   */
  @Autowired
  private ArticleService articleService;

  /**
   * テスト対象のコメント管理サービス
   */
  @Autowired
  private ArticleCommentService cut;

  /**
   * 依存するサービスのインスタンスが正しく注入されていることを検証するセットアップメソッド。
   */
  @Test
  void setup() {
    // テスト対象のインスタンスがnullでないことを検証
    assertThat(cut).isNotNull();
    assertThat(articleService).isNotNull();
    assertThat(userService).isNotNull();
  }

  /**
   * コメント作成処理の正常系テスト。
   *
   * <p>以下の条件を満たすことを確認する:</p>
   * <ul>
   *   <li>コメントがデータベースに正常に登録されること</li>
   *   <li>作成されたコメントの内容が正しいこと</li>
   *   <li>コメントの作成者情報が適切に設定されていること</li>
   *   <li>作成日時が正しく設定されること</li>
   * </ul>
   */
  @Test
  @DisplayName("create: articles テーブルにレコードが insert される")
  void create_success() {
    // ## Arrange ##
    // 固定された日時をモックで設定
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // テスト用のユーザーと記事を作成
    var articleAuthor = userService.register("test_username1", "test_password");
    var commentAuthor = userService.register("test_username2", "test_password");
    var article = articleService.create(articleAuthor.getId(), "test_title", "test_body");
    var expectedComment = "コメントしました";

    // ## Act ##
    // コメントを作成
    var actual = cut.create(commentAuthor.getId(), article.getId(), expectedComment);

    // ## Assert ##
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getBody()).isEqualTo(expectedComment);

    // 作成者の情報が適切に設定されていること（パスワードを除外）
    assertThat(actual.getAuthor())
        .usingRecursiveComparison()
        .ignoringFields("password")
        .isEqualTo(commentAuthor);

    assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
  }

  /**
   * 指定された記事が存在しない場合に、createメソッドが ResourceNotFoundException をスローすることを検証するテスト。
   *
   * <p>
   * 【テストの流れ】
   * <ol>
   *   <li><b>Arrange:</b>
   *     <ul>
   *       <li>モックされた DateTimeService を利用して、現在日時を固定値 (2020/01/02 10:20) に設定。</li>
   *       <li>ユーザーサービスを通じて、コメント作成者を登録する。</li>
   *       <li>無効な記事ID (0) を指定し、存在しない記事をシミュレートする。</li>
   *       <li>テスト用のコメント内容を設定する。</li>
   *     </ul>
   *   </li>
   *   <li><b>Act & Assert:</b>
   *     <ul>
   *       <li>無効な記事IDを渡して cut.create を実行し、ResourceNotFoundException がスローされることを assertThrows で検証する。</li>
   *     </ul>
   *   </li>
   * </ol>
   * </p>
   */
  @Test
  @DisplayName("create: 指定された記事が存在しないとき ResourceNotFoundException を投げる")
  void create_articleDoesNotExist() {
    // ## Arrange ##
    // 固定された日時をモックで設定
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // コメント作成者を登録
    var commentAuthor = userService.register("test_username2", "test_password");
    // テスト用のコメント内容と、存在しない記事IDを設定
    var expectedComment = "コメントしました";
    var invalidArticleId = 0;

    // Act & Assert: 無効な記事IDの場合、ResourceNotFoundException が発生することを検証
    assertThrows(ResourceNotFoundException.class, () -> {
      cut.create(commentAuthor.getId(), invalidArticleId, expectedComment);
    });
  }

  /**
   * findByArticleId_success: 記事IDを指定して、対象記事に紐づくコメント一覧を取得できることを検証するテスト。
   *
   * <p>
   * 【テストの流れ】
   * <ol>
   *   <li><b>Arrange:</b>
   *     <ul>
   *       <li>固定日時を返すようにモックされた DateTimeService を設定。</li>
   *       <li>記事作成者を登録し、記事を作成する。</li>
   *       <li>2件のコメントを、異なるコメント作成者で作成する。</li>
   *     </ul>
   *   </li>
   *   <li><b>Act:</b>
   *     <ul>
   *       <li>対象記事IDに紐づくコメント一覧を取得する。</li>
   *     </ul>
   *   </li>
   *   <li><b>Assert:</b>
   *     <ul>
   *       <li>取得したコメント一覧のサイズが 2 であることを確認する。</li>
   *       <li>取得した1件目と2件目のコメントが、それぞれ期待したコメントと一致することを確認する。</li>
   *     </ul>
   *   </li>
   * </ol>
   * </p>
   *
   * @throws Exception リクエスト実行中に発生する例外
   */
  @Test
  @DisplayName("findByArticleId: 記事IDを指定して記事コメントの一覧を取得できる")
  void findByArticleId_success() {
    // ## Arrange ##
    // 固定された日時をモックで設定
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2021, 1, 2, 10, 20))
        .thenReturn(TestDateTimeUtil.of(2023, 1, 2, 10, 20));

    // テスト用のユーザーと記事を作成
    var articleAuthor = userService.register("test_username1", "test_password");
    var article = articleService.create(articleAuthor.getId(), "test_title", "test_body");

    // 2件のコメントを作成（それぞれ別のコメント作成者）
    var commentAuthor1 = userService.register("test_username2", "test_password");
    var comment1 = cut.create(commentAuthor1.getId(), article.getId(), "1 コメントしました");

    var commentAuthor2 = userService.register("test_username3", "test_password");
    var comment2 = cut.create(commentAuthor2.getId(), article.getId(), "2 コメントしました");

    // ## Act ##
    var actual = cut.findByArticleId(article.getId());

    // ## Assert ##
    // 取得結果のサイズと内容を検証
    // controllerで取得したオブジェクトの値が期待された値かどうか検証済みなので簡単に済ませる
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0)).isEqualTo(comment1);
    assertThat(actual.get(1)).isEqualTo(comment2);
  }
}