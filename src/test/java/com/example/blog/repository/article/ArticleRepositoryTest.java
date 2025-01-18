package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@MybatisDefaultDatasourceTest
class ArticleRepositoryTest {

  // テスト対象（cut: class under test）のArticleRepositoryを自動的に注入
  @Autowired
  private ArticleRepository cut;
  @Autowired
  private UserRepository userRepository;

  // 単純なテストメソッド。cut（ArticleRepository）がnullではないことを検証。
  // これにより、Springのコンテナが正常に動作し、ArticleRepositoryが正しく注入されているかどうかを確認します。
  @Test
  public void test() {
    assertThat(cut).isNotNull();
  }

  /**
   * このテストメソッドは、引数で指定されたIDの記事が存在する場合に、 {@link ArticleRepository#selectById(int)} が正しく
   * {@link ArticleEntity} を返すことを検証します。
   *
   * <p>テストの準備段階として、@Sqlアノテーションを使用して指定された記事のデータを
   * データベースに挿入しています。この場合、ID 999 の記事が 'title_999', 'content_999' という内容で 挿入されています。</p>
   *
   * <p>テストの主な検証点は次の通りです:</p>
   * <ul>
   *   <li>IDが999である記事が正しく返されること。</li>
   *   <li>返された記事のタイトルが 'title_999' であること。</li>
   *   <li>返された記事の本文が 'content_999' であること。</li>
   *   <li>作成日時と更新日時が正しいこと。</li>
   *   <li>記事の作成者（著者）のID、ユーザー名、パスワード、アカウント有効状態が正しいこと。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>@Sqlアノテーションを使用して、テスト用データ（ユーザーと記事）をデータベースに挿入。</li>
   *   <li>ArticleRepository#selectById メソッドを呼び出して、記事データを取得。</li>
   *   <li>取得した記事データが期待通りの内容であることを検証。</li>
   * </ol>
   *
   * @throws AssertionError テストが失敗した場合
   */
  @Test
  @DisplayName("selectById: 引数で指定されたIDの記事が存在するとき、ArticleEntity を返す")
  @Sql(statements = {"""
      DELETE FROM articles;
      DELETE FROM users;

      INSERT INTO users (id, username, password, enabled)
      VALUES (1, 'test_user1', 'test_password_1', true);

      INSERT INTO articles (id, user_id, title, body, created_at, updated_at)
      VALUES (999, 1, 'title_999', 'content_999', '2010-10-01 00:00:00', '2010-11-01 00:00:00');
      """
  })
  public void selectById_returnArticleEntity() {
    // ## Arrange ##
    // ## テストデータは@Sqlアノテーションにより事前に挿入されている

    // ## Act ##
    var actual = cut.selectById(999);

    // ## Assert ##
    assertThat(actual)
        .isPresent()
        .hasValueSatisfying(article -> {
          assertThat(article.getId()).isEqualTo(999);
          assertThat(article.getTitle()).isEqualTo("title_999");
          assertThat(article.getBody()).isEqualTo("content_999");
          assertThat(article.getCreatedAt()).isEqualTo("2010-10-01T00:00:00+09:00");
          assertThat(article.getUpdatedAt()).isEqualTo("2010-11-01T00:00:00+09:00");

          assertThat(article.getAuthor().getId()).isEqualTo(1);
          assertThat(article.getAuthor().getUsername()).isEqualTo("test_user1");
          assertThat(article.getAuthor().getPassword()).isNull();
          assertThat(article.getAuthor().isEnabled()).isEqualTo(true);
        });
  }

  /**
   * このテストメソッドは、引数で指定されたIDの記事が存在しない場合に、 {@link ArticleRepository#selectById(int)} が空の
   * {@link Optional} を返すことを検証します。
   *
   * <p>ここでは、IDに存在しない値（-9）を指定しています。この場合、データベース内に該当する記事は
   * 存在しないため、{@link Optional#isEmpty()}がtrueであることを確認します。</p>
   *
   * <p>主な検証点は次の通りです:
   * <ul>
   *   <li>指定されたID (-9) に該当する記事がデータベースに存在しないこと。</li>
   *   <li>その結果、メソッドが空のOptionalを返すことを確認する。</li>
   * </ul>
   * </p>
   */
  @Test
  @DisplayName("selectById: 引数で指定されたIDの記事が存在しないとき、空のOptionalを返す")
  public void selectById_returnEmpty() {
    // ## Arrange ##

    // ## Act ##
    var actual = cut.selectById(-9);

    // ## Assert ##
    assertThat(actual).isEmpty();
  }

  /**
   * 記事データの挿入処理を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>記事データが正しくデータベースに挿入されること。</li>
   *   <li>挿入された記事データをデータベースから取得し、期待通りの値が返されること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>テスト用のユーザーを準備し、データベースに挿入。</li>
   *   <li>挿入する記事データ（タイトル、本文、作成者、作成日時、更新日時）を準備。</li>
   *   <li>記事データをデータベースに挿入。</li>
   *   <li>挿入した記事データをIDで取得し、各フィールドが期待通りの値であることを検証。</li>
   * </ol>
   *
   * <p>検証内容:</p>
   * <ul>
   *   <li>記事IDが正しく生成され、データベースに格納されていること。</li>
   *   <li>記事のタイトルと本文が正しく格納されていること。</li>
   *   <li>作成者情報（ID、ユーザー名、アカウント有効状態）が正しく格納されていること。</li>
   *   <li>作成日時と更新日時が正しく格納されていること。</li>
   *   <li>作成者のパスワードが `null` であることを確認。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("insert：記事データの作成に成功する")
  void insert_success() {
    // ## Arrange ##
    // テストデータを準備する

    // 1. ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity(null, "test_username", "test_password", true);
    userRepository.insert(expectedUser); // ユーザーIDが自動生成されることを期待

    // 2. 挿入する記事エンティティを準備
    var expectedEntity = new ArticleEntity(
        null, // IDは自動生成されることを期待
        "test_title",
        "test_body",
        expectedUser,
        TestDateTimeUtil.of(2020, 1, 1, 10, 30),
        TestDateTimeUtil.of(2021, 1, 1, 10, 30)
    );

    // ## Act ##
    cut.insert(expectedEntity);

    // ## Assert ##
    var actualOpt = cut.selectById(expectedEntity.getId());
    assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
      assertThat(actualEntity.getId()).isEqualTo(expectedEntity.getId());
      assertThat(actualEntity.getTitle()).isEqualTo(expectedEntity.getTitle());
      assertThat(actualEntity.getBody()).isEqualTo(expectedEntity.getBody());
      assertThat(actualEntity.getAuthor().getId()).isEqualTo(expectedEntity.getAuthor().getId());
      assertThat(actualEntity.getAuthor().getUsername()).isEqualTo(
          expectedEntity.getAuthor().getUsername());
      assertThat(actualEntity.getAuthor().getPassword()).isNull();
      assertThat(actualEntity.getAuthor().isEnabled()).isEqualTo(
          expectedEntity.getAuthor().isEnabled());
      assertThat(actualEntity.getCreatedAt()).isEqualTo(expectedEntity.getCreatedAt());
      assertThat(actualEntity.getUpdatedAt()).isEqualTo(expectedEntity.getUpdatedAt());
    });
  }

  /**
   * selectAll メソッドがデータベースに記事が存在しない場合に空のリストを返すことを確認するテスト。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>データベースが空の状態で selectAll メソッドを呼び出した場合、空のリストが返されること。</li>
   *   <li>エラーが発生せず、正常にメソッドが動作すること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テスト開始時にテーブルが空である（{@code DELETE FROM articles;} を実行済み）。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("selectAll：空のリストを返す")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void selectAll_returnEmpty() {
    // ## Arrange ## SQL でデータベースを初期化済み

    // ## Act ##
    var actual = cut.selectAll(); // データベースが空の状態で selectAll を実行

    // ## Assert ##
    assertThat(actual).isEmpty(); // 空のリストが返されることを確認
  }
}