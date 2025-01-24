package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * {@link ArticleService} のテストクラス。
 *
 * <p>このクラスでは、記事作成サービスの動作を検証します。具体的には、articlesテーブルへのレコード挿入が
 * 正常に行われるかを確認します。</p>
 *
 * <p>使用するアノテーション:</p>
 * <ul>
 *   <li>{@link MybatisDefaultDatasourceTest}: MyBatisとデフォルトデータソース設定を使用したテスト環境を構築。</li>
 *   <li>{@link Import}: {@link ArticleService} をテスト環境にインポートしてテスト対象に指定。</li>
 * </ul>
 */
@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceTest {

  @Autowired
  private ArticleService cut;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * {@link ArticleService} と {@link UserRepository} が正しく初期化されていることを確認します。
   * <p>このテストはテスト環境のセットアップ確認用です。</p>
   */
  @Test
  void setup() {
    // テスト対象のインスタンスがnullでないことを検証
    assertThat(cut).isNotNull();
    assertThat(userRepository).isNotNull();
  }

  /**
   * 記事作成処理の動作を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>articlesテーブルに新しいレコードが挿入されること。</li>
   *   <li>挿入された記事データが正しい値を持つこと。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>テスト用のユーザーを準備してデータベースに挿入。</li>
   *   <li>作成する記事のタイトルと本文を準備。</li>
   *   <li>記事作成サービスを呼び出し、新しい記事を作成。</li>
   *   <li>作成された記事が期待通りのデータを持っていることを検証。</li>
   * </ol>
   */
  @Test
  @DisplayName("create: articles テーブルにレコードが insert される")
  void create_success() {
    // ## Arrange ##
    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user1"); // ユーザー名を設定
    expectedUser.setPassword("test_password1"); // パスワードを設定
    expectedUser.setEnabled(true); // 有効なユーザーであることを示す
    // ユーザーをデータベースに挿入
    userRepository.insert(expectedUser);

    // 日付を固定：この値がDBに登録される
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // 記事のタイトルと本文を準備
    var expectedTitle = "test_article_title";
    var expectedBody = "test_article_body";

    // ## Act ##
    // 記事作成サービスを呼び出し、新しい記事を作成
    var actual = cut.create(expectedUser.getId(), expectedTitle, expectedBody);

    // ## Assert ##
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getTitle()).isEqualTo(expectedTitle);
    assertThat(actual.getBody()).isEqualTo(expectedBody);
    assertThat(actual.getAuthor()).satisfies(user -> {
      assertThat(user.getId()).isEqualTo(expectedUser.getId());
      assertThat(user.getUsername()).isEqualTo(expectedUser.getUsername());
      assertThat(user.getPassword()).isNull(); // パスワードはnullであるべき（セキュリティ対策）
      assertThat(user.isEnabled()).isEqualTo(expectedUser.isEnabled());
    });
    assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
    assertThat(actual.getUpdatedAt()).isEqualTo(actual.getCreatedAt()); // 作成日時と更新日時が一致していることを確認
  }

  /**
   * 記事が存在しない場合の findAll メソッドの動作をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>テーブルに記事データが存在しない場合、findAll メソッドが空のリストを返すこと。</li>
   *   <li>エラーが発生せず、正常にメソッドが動作すること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テストの実行前に {@code DELETE FROM articles;} によってテーブルがクリアされている。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("findAll: 記事が存在しないとき、空のリストが取得できる")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void findAll_returnEmptyList() {
    // ## Arrange ## SQlで実行済み

    // ## Act ##
    var actual = cut.findAll(); // 記事がない状態で findAll を呼び出す

    // ## Assert ##
    assertThat(actual).isEmpty(); // 空のリストが返されることを確認
  }

  /**
   * 記事が存在する場合の findAll メソッドの動作をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>テーブルに複数の記事が存在する場合、findAll メソッドが正しい順序でそれらの記事を返すこと。</li>
   *   <li>返されるリストのサイズが記事の件数と一致すること。</li>
   *   <li>返される記事の内容が期待値と一致すること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テスト開始時にテーブルが空である（{@code DELETE FROM articles;} を実行済み）。</li>
   *   <li>新規ユーザーを登録し、そのユーザーに紐づけて記事を作成する。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("findAll: 記事が存在するとき、リストを返す")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void findAll_returnMultipleArticle() {
    // ## Arrange ##
    // 固定された日時を使用
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2022, 1, 1, 10, 10))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 10, 20));

    // ユーザーを作成
    var user1 = new UserEntity();
    user1.setUsername("test_username1");
    user1.setPassword("test_password1");
    user1.setEnabled(true);
    userRepository.insert(user1); // ユーザーをデータベースに登録

    // 記事を作成
    var expectedArticle1 = cut.create(user1.getId(), "test_title1", "test_body1");
    var expectedArticle2 = cut.create(user1.getId(), "test_title2", "test_body2");

    // ## Act ##
    // 記事をすべて取得
    var actual = cut.findAll();

    // ## Assert ##
    // リストサイズを検証
    assertThat(actual).hasSize(2);

    // 記事の順序と内容を検証
    assertThat(actual.get(0)).isEqualTo(expectedArticle2); // 新しい記事が先頭にくる
    assertThat(actual.get(1)).isEqualTo(expectedArticle1);
  }
}