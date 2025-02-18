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

  /**
   * selectAll メソッドが記事データを正しく返すことを確認するテスト。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>データベースに複数の記事が存在する場合、findAll メソッドがそれらを正しい順序で返すこと。</li>
   *   <li>返されるリストのサイズが期待通りであること。</li>
   *   <li>記事データの内容が期待通りであること。</li>
   *   <li>返される著者データにパスワードフィールドが含まれないこと。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テーブルが初期化済み（{@code DELETE FROM articles;} を実行）。</li>
   *   <li>テスト内でユーザーと記事を新規作成。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("selectAll: 記事が存在するとき、リストを返す")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void selectAll_returnMultipleArticle() {
    // ## Arrange ##
    // ユーザーを作成
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_username1");
    expectedUser.setPassword("test_password1");
    expectedUser.setEnabled(true);
    userRepository.insert(expectedUser); // ユーザーをデータベースに登録

    // 記事の作成日時を設定
    var datetime1 = TestDateTimeUtil.of(2022, 1, 1, 10, 10);
    var datetime2 = TestDateTimeUtil.of(2022, 2, 1, 10, 20);

    // 記事を作成
    var expectedArticle1 = new ArticleEntity(null, "test_title1", "test_body1", expectedUser,
        datetime1,
        datetime1);
    var expectedArticle2 = new ArticleEntity(null, "test_title2", "test_body2", expectedUser,
        datetime2,
        datetime2);
    cut.insert(expectedArticle1);
    cut.insert(expectedArticle2);

    // ## Act ##
    // 記事をすべて取得
    var actual = cut.selectAll();

    // ## Assert ##
    // リストサイズを検証
    assertThat(actual).hasSize(2);

    // 記事の順序と内容を検証
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .ignoringFields("author.password")// パスワードフィールドを無視
        .isEqualTo(expectedArticle2); // 新しい記事が先頭にくる

    assertThat(actual.get(1))
        .usingRecursiveComparison()
        .ignoringFields("author.password")
        .isEqualTo(expectedArticle1);
  }

  /**
   * update_success: 記事の title、body、および updatedAt を更新することを検証するテストです。
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange: 更新後に期待されるタイトル、本文、作成日時、および更新日時を定義し、テスト用ユーザーを作成してデータベースに登録します。
   *     次に、初期状態の記事（作成時の createdAt と updatedAt は同一）を作成・登録します。
   *   </li>
   *   <li>
   *     Act: 既存の記事のタイトル、本文、および更新日時を更新するためのオブジェクトを作成し、update メソッドを呼び出して記事を更新します。
   *   </li>
   *   <li>
   *     Assert: データベースから更新後の記事を取得し、再帰的な比較により各フィールドが期待通りに更新されているかを検証します。<br>
   *     なお、セキュリティの観点から author.password は比較対象から除外しています。
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("update: 記事の title/body/updatedAt を更新する")
  void update_success() {
    // ## Arrange ##
    // 更新後に期待されるタイトルと本文を定義
    var expectedTitle = "test_title_update";
    var expectedBody = "test_body_update";

    // 記事作成時の日時を定義（作成日時）
    var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 1, 10, 30);
    // 更新日時は作成日時の1日後とする
    var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

    // テスト用ユーザーを生成（ID は自動生成されるため null で初期化）
    var expectedUser = new UserEntity(null, "test_username", "test_password", true);
    // ユーザーをデータベースに登録
    userRepository.insert(expectedUser);

    // 初期状態の記事を生成
    // 新規作成のため、createdAt と updatedAt は同じ日時です。
    var articleToCreate = new ArticleEntity(
        null,
        "test_title",
        "test_body",
        expectedUser,
        expectedCreatedAt,
        expectedCreatedAt // 記事を新規作成したため、createAt = updateAt
    );
    // 作成した記事をデータベースに登録
    cut.insert(articleToCreate);

    // 更新対象となる記事オブジェクトを生成
    // ここでは、タイトル、本文、および更新日時 (updatedAt) を更新します。
    var articleToUpdate = new ArticleEntity(
        articleToCreate.getId(),          // 既存の記事のIDを利用
        expectedTitle,                    // 更新後のタイトル
        expectedBody,                     // 更新後の本文
        articleToCreate.getAuthor(),      // 作者情報は変更なし
        articleToCreate.getCreatedAt(),   // 作成日時は変更なし
        expectedUpdatedAt                 // 更新日時を更新
    );

    // ## Act ##
    // update メソッドを呼び出して記事を更新する
    cut.update(articleToUpdate);

    // ## Assert ##
    // データベースから更新後の記事を取得し、期待される内容と一致しているか検証
    var actual = cut.selectById(articleToUpdate.getId());
    assertThat(actual).hasValueSatisfying(actualArticle -> {
      // 再帰的比較により、すべてのフィールドが articleToUpdate と一致することを確認
      // ただし、セキュリティ上、author.password は無視して比較する
      assertThat(actualArticle)
          .usingRecursiveComparison()
          .ignoringFields("author.password")
          .isEqualTo(articleToUpdate);
    });
  }

  /**
   * update_invalidArticleId: 指定された記事IDが存在しない場合、update メソッドがデータベースに登録済みの記事を更新しないことを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>存在する記事に対して、無効な記事ID（0L）を指定した更新操作が行われた場合、既存の記事は変更されない。</li>
   *   <li>更新操作後も、記事の内容は初期作成時の状態と一致している。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>
   *         更新後に期待されるタイトル、本文、作成日時、更新日時を定義します。
   *       </li>
   *       <li>
   *         テスト用ユーザーを生成し、データベースに登録します。
   *       </li>
   *       <li>
   *         テスト用ユーザーを使用して初期状態の記事を作成し、データベースに登録します。
   *         ※記事作成時は createdAt と updatedAt が同一となっています。
   *       </li>
   *       <li>
   *         更新対象の記事オブジェクトを生成しますが、その際、記事IDとして存在しない値（0L）を指定します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>
   *         update メソッドを呼び出して、記事の更新操作を実行します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>
   *         データベースに登録されている元の記事（作成時の記事）が更新されていないことを検証します。
   *         ここでは、再帰的比較を使用して、全フィールドが初期状態と一致しているかを確認します。
   *       </li>
   *       <li>
   *         再帰的比較では、オブジェクトの全てのフィールドや入れ子になっているオブジェクトも含めて比較します。
   *         このテストでは、セキュリティ上の理由から author.password フィールドは比較対象から除外しています。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("update: 指定された記事IDが存在しないとき、更新しない")
  void update_invalidArticleId() {
    // ## Arrange ##
    // 更新後に期待されるタイトルと本文を定義
    var expectedTitle = "test_title_update";
    var expectedBody = "test_body_update";

    // 記事作成時の日時を定義（作成日時）
    var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 1, 10, 30);
    // 更新日時は作成日時の1日後とする
    var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

    // テスト用ユーザーを生成（ID は自動生成されるため null で初期化）
    var expectedUser = new UserEntity(null, "test_username", "test_password", true);
    // ユーザーをデータベースに登録
    userRepository.insert(expectedUser);

    // 初期状態の記事を生成
    // 新規作成のため、createdAt と updatedAt は同じ日時です。
    var articleToCreate = new ArticleEntity(
        null,
        "test_title",
        "test_body",
        expectedUser,
        expectedCreatedAt,
        expectedCreatedAt // 記事を新規作成したため、createAt = updateAt
    );
    // 作成した記事をデータベースに登録
    cut.insert(articleToCreate);

    // 更新対象となる記事オブジェクトを生成
    // ここでは、タイトル、本文、および更新日時 (updatedAt) を更新するが、
    // 記事IDとして存在しない値（0L）を指定する
    var articleToUpdate = new ArticleEntity(
        0L,
        expectedTitle,                    // 更新後のタイトル
        expectedBody,                     // 更新後の本文
        articleToCreate.getAuthor(),      // 作者情報は変更なし
        articleToCreate.getCreatedAt(),   // 作成日時は変更なし
        expectedUpdatedAt                 // 更新日時を更新
    );

    // ## Act ##
    // update メソッドを呼び出して記事を更新する
    cut.update(articleToUpdate);

    // ## Assert ##
    // データベースから更新後の記事を取得し、更新操作が無視されていることを検証
    // すなわち、記事は作成時の状態 (articleToCreate) のままであることを確認する
    var actual = cut.selectById(articleToCreate.getId());
    assertThat(actual).hasValueSatisfying(actualArticle -> {
      // 再帰的比較により、すべてのフィールドが articleToUpdate と一致することを確認
      // ただし、セキュリティ上、author.password は無視して比較する
      assertThat(actualArticle)
          .usingRecursiveComparison()
          .ignoringFields("author.password")
          .isEqualTo(articleToCreate);
    });
  }

  /**
   * update_invalidAuthorId: 著者以外のユーザーIDが指定された場合に、記事の更新が行われず、元の状態が保持されることを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>記事作成者(author)と異なるユーザー(otherUser)が更新対象として指定された場合、記事の内容が変更されない。</li>
   *   <li>更新後のデータベース上のレコードが、初期作成時の状態(articleToCreate)と一致すること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>更新後に期待されるタイトル、本文、作成日時、更新日時を定義します。</li>
   *       <li>著者ユーザー(author)を生成してデータベースに登録します。</li>
   *       <li>別のユーザー(otherUser)を生成してデータベースに登録します。</li>
   *       <li>authorを使って初期状態の記事(articleToCreate)を作成・登録します。</li>
   *       <li>更新対象の記事(articleToUpdate)を生成しますが、authorではなくotherUserを指定して更新を試みます。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>updateメソッドを呼び出して、記事の更新処理を実行します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>データベースから記事を取得し、元の状態(articleToCreate)と一致していることを再帰的比較により検証します。</li>
   *       <li>再帰的比較では、author.passwordフィールドはセキュリティ上の理由から比較対象から除外しています。</li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("update: 著者以外のユーザーIDが指定されているとき、更新しない")
  void update_invalidAuthorId() {
    // ## Arrange ##
    // 更新後に期待されるタイトルと本文を定義
    var expectedTitle = "test_title_update";
    var expectedBody = "test_body_update";

    // 記事作成時の日時を定義（作成日時）
    var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 1, 10, 30);
    // 更新日時は作成日時の1日後とする
    var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

    // 著者ユーザーを生成（ID は自動生成されるため null で初期化）
    var author = new UserEntity(null, "test_username", "test_password", true);
    // 著者ユーザーをデータベースに登録
    userRepository.insert(author);

    // 別のユーザーを生成（更新操作を試みるユーザーとして）
    var otherUser = new UserEntity(null, "test_username1", "test_password1", true);
    // 別ユーザーをデータベースに登録
    userRepository.insert(otherUser);

    // 初期状態の記事を生成
    // 新規作成のため、createdAt と updatedAt は同じ日時です。
    var articleToCreate = new ArticleEntity(
        null,
        "test_title",
        "test_body",
        author,
        expectedCreatedAt,
        expectedCreatedAt // 記事を新規作成したため、createAt = updateAt
    );
    // 作成した記事をデータベースに登録
    cut.insert(articleToCreate);

    // 更新対象となる記事オブジェクトを生成
    // ここでは、既存の記事IDを使用しているが、更新操作において更新者としてotherUserが指定される
    var articleToUpdate = new ArticleEntity(
        articleToCreate.getId(),          // 既存の記事のIDを利用
        expectedTitle,                    // 更新後のタイトル
        expectedBody,                     // 更新後の本文
        otherUser,                        // 更新者として、元の記事作成者(author)とは異なるユーザー(otherUser)を指定
        articleToCreate.getCreatedAt(),   // 作成日時は変更なし
        expectedUpdatedAt                 // 更新日時を更新（更新処理が成功した場合に反映される値）
    );

    // ## Act ##
    // update メソッドを呼び出して記事を更新する
    cut.update(articleToUpdate);

    // ## Assert ##
    // データベースから記事を取得し、更新が行われず元の状態(articleToCreate)のままであることを検証する
    var actual = cut.selectById(articleToCreate.getId());
    assertThat(actual).hasValueSatisfying(actualArticle -> {
      // 再帰的比較により、すべてのフィールドが articleToUpdate と一致することを確認
      // ただし、セキュリティ上、author.password は無視して比較する
      assertThat(actualArticle)
          .usingRecursiveComparison()
          .ignoringFields("author.password")
          .isEqualTo(articleToCreate);
    });
  }

  /**
   * delete: 指定された ID の記事を削除することを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>ユーザーが作成した記事が、削除処理によりデータベースから完全に削除されること。</li>
   *   <li>削除後、該当の記事がデータベースに存在しないことを確認する。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>テスト用ユーザー (author) を生成し、データベースに登録します。</li>
   *       <li>author を使用して、初期状態の記事 (existingArticle) を生成し、データベースに登録します。
   *           ※ 作成時の日時は createAt と updateAt が同一です。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>サービス層の delete メソッドを呼び出して、existingArticle を削除します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>データベースから削除対象の記事を取得し、結果が空 (Optional.empty()) であることを検証します。</li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("delete: 指定された ID の記事を削除する")
  void delete_success() {
    // ## Arrange ##
    // テスト用ユーザーを生成（ID は自動生成されるため null で初期化）
    var author = new UserEntity(null, "test_username", "test_password", true);
    // ユーザーをデータベースに登録
    userRepository.insert(author);

    // 初期状態の記事を生成
    // ここでは、author を作成者とし、作成日時と更新日時は同一の値を設定しています。
    var existingArticle = new ArticleEntity(
        null,
        "test_title",
        "test_body",
        author,
        TestDateTimeUtil.of(2020, 1, 1, 10, 30),
        TestDateTimeUtil.of(2020, 1, 1, 10, 30) // 記事を新規作成したため、createAt = updateAt
    );
    // 作成した記事をデータベースに登録
    cut.insert(existingArticle);

    // ## Act ##
    // リポジトリ層の delete メソッドを呼び出して、指定された記事 (existingArticle) を削除します。
    cut.delete(existingArticle);

    // ## Assert ##
    // データベースから削除対象の記事を取得し、結果が空 (Optional.empty()) であることを検証します。
    var actual = cut.selectById(existingArticle.getId());
    assertThat(actual).isEmpty();
  }

  @Test
  @DisplayName("delete: 指定された記事IDが存在しないとき、削除しない")
  void delete_invalidArticleId() {
    // ## Arrange ##
    var author = new UserEntity(null, "test_username", "test_password", true);
    userRepository.insert(author);

    var existingArticle = new ArticleEntity(
        null,
        "test_title",
        "test_body",
        author,
        TestDateTimeUtil.of(2020, 1, 10, 10, 20),
        TestDateTimeUtil.of(2020, 1, 10, 10, 20)
    );
    cut.insert(existingArticle);

    var articleToDelete = new ArticleEntity(
        0L, // invalid article id
        existingArticle.getTitle(),
        existingArticle.getBody(),
        existingArticle.getAuthor(),
        existingArticle.getCreatedAt(),
        existingArticle.getUpdatedAt()
    );

    // ## Act ##
    cut.delete(articleToDelete);

    // ## Assert ##
    var actual = cut.selectById(existingArticle.getId());
    assertThat(actual).hasValueSatisfying(actualArticle -> {
      assertThat(actualArticle)
          .usingRecursiveComparison()
          .ignoringFields("author.password")
          .isEqualTo(existingArticle);
    });
  }
}