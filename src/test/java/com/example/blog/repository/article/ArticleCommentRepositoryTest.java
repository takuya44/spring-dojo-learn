package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.article.ArticleCommentEntity;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ArticleCommentRepositoryTestは、記事コメントに関するリポジトリの操作が正しく行われるかを検証するテストクラスです。
 * このテストは、MyBatisを利用したデフォルトのデータソース設定で実行されます。
 */
@MybatisDefaultDatasourceTest
class ArticleCommentRepositoryTest {

  // テスト対象という意味（cut: class under test）
  @Autowired
  private ArticleCommentRepository cut;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ArticleRepository articleRepository;

  // テスト期待値フィールド（検索対象とダミーデータ用）
  private ArticleEntity article1;
  private ArticleCommentEntity article1Comment1;
  private ArticleCommentEntity article1Comment2;
  private ArticleCommentEntity article2Comment1;

  @BeforeEach
  void beforeEach() {
    // 【方針】
    // - article1 に対するコメント (article1Comment1, article1Comment2) は検索対象
    // - article2 に対するコメント (article2Comment1) はダミーデータ

    // ---- article1 用データ生成 ----
    // 記事作成者生成・登録 (ID: 自動生成)
    var articleAuthor1 = new UserEntity(
        null, // IDは自動生成のためnull
        "test_username1", // ユーザー名
        "test_password1", // パスワード（テスト用）
        true              // ユーザーが有効であることを示すフラグ
    );
    userRepository.insert(articleAuthor1);

    // article1 生成・登録
    article1 = new ArticleEntity(
        null,                                  // IDは自動生成
        "test_title",                             // 記事のタイトル
        "test_body",                              // 記事の本文
        articleAuthor1,                            // 記事の著者
        TestDateTimeUtil.of(2020, 1, 1, 10, 30), // 記事の作成日時
        TestDateTimeUtil.of(2021, 1, 1, 10, 30) // 記事の更新日時
    );
    articleRepository.insert(article1);

    // article1 用コメント投稿者生成・登録
    var commentAuthor11 = new UserEntity(
        null,             // IDは自動生成
        "test_username11",   // ユーザー名
        "test_password",     // パスワード
        true                 // ユーザーが有効であることを示すフラグ
    );
    userRepository.insert(commentAuthor11);
    var commentAuthor12 = new UserEntity(
        null,             // IDは自動生成
        "test_username12",   // ユーザー名
        "test_password",     // パスワード
        true                 // ユーザーが有効であることを示すフラグ
    );
    userRepository.insert(commentAuthor12);

    // article1 のコメント生成（検索対象）
    article1Comment1 = new ArticleCommentEntity(
        null,                                 // コメントIDはDB自動生成
        "test_body",                             // コメントの本文
        article1,                                 // コメント対象の記事
        commentAuthor11,                           // コメント投稿者
        TestDateTimeUtil.of(2022, 1, 1, 10, 31) // コメントの投稿日時
    );
    article1Comment2 = new ArticleCommentEntity(
        null,                                 // コメントIDはDB自動生成
        "test_body",                             // コメントの本文
        article1,                                 // コメント対象の記事
        commentAuthor12,                           // コメント投稿者
        TestDateTimeUtil.of(2022, 1, 1, 10, 31) // コメントの投稿日時
    );

    // ---- article2 用ダミーデータ生成 ----
    // 記事作成者生成・登録
    var articleAuthor2 = new UserEntity(
        null, // IDは自動生成のためnull
        "test_username2", // ユーザー名
        "test_password2", // パスワード（テスト用）
        true              // ユーザーが有効であることを示すフラグ
    );
    userRepository.insert(articleAuthor2);

    // article2 生成・登録
    var article2 = new ArticleEntity(
        null,                                  // IDは自動生成
        "test_title",                             // 記事のタイトル
        "test_body",                              // 記事の本文
        articleAuthor2,                            // 記事の著者
        TestDateTimeUtil.of(2020, 1, 1, 10, 30), // 記事の作成日時
        TestDateTimeUtil.of(2021, 1, 1, 10, 30) // 記事の更新日時
    );
    articleRepository.insert(article2);

    // article2 用コメント投稿者生成・登録
    var commentAuthor21 = new UserEntity(
        null,             // IDは自動生成
        "test_username21",   // ユーザー名
        "test_password",     // パスワード
        true                 // ユーザーが有効であることを示すフラグ
    );
    userRepository.insert(commentAuthor21);

    // article2 のコメント生成（ダミーデータ）
    article2Comment1 = new ArticleCommentEntity(
        null,                                 // コメントIDはDB自動生成
        "test_body",                             // コメントの本文
        article2,                                 // コメント対象の記事
        commentAuthor21,                           // コメント投稿者
        TestDateTimeUtil.of(2022, 1, 1, 10, 31) // コメントの投稿日時
    );
  }

  /**
   * DI（依存性注入）が正しく機能しているか確認するためのシンプルなテスト。 ArticleCommentRepositoryがSpringコンテナから正常に注入されていることを検証します。
   */
  @Test
  public void test() {
    assertThat(cut).isNotNull();
  }

  /**
   * insert_successは、記事コメントの挿入処理が正しく動作することを検証するテストです。
   *
   * <p>テストは以下の手順で実行されます。</p>
   * <ul>
   *   <li><b>Arrange:</b> 記事の著者、記事、コメント投稿者、及び記事コメントの各エンティティを生成し、データベースに保存する。</li>
   *   <li><b>Act:</b> 作成した記事コメントエンティティを対象リポジトリに挿入する。</li>
   *   <li><b>Assert:</b> 挿入後、記事コメントが正しくデータベースから取得でき、期待した内容と一致していることを検証する。</li>
   * </ul>
   */
  @Test
  @DisplayName("insert：記事コメントの insert に成功する")
  void insert_success() {
    // ## Arrange ##

    // ## Act ##
    // テスト対象メソッドを使用して、記事コメントエンティティをデータベースに挿入する
    cut.insert(article1Comment1);

    // ## Assert ##
    // 挿入した記事コメントエンティティをIDで取得し、期待値と一致するかを検証する
    var actualOpt = cut.selectById(article1Comment1.getId());
    assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
      // 再帰的な比較を行い、パスワード情報は比較対象から除外して一致していることを確認
      assertThat(actualEntity)
          .usingRecursiveComparison()
          .ignoringFields(
              "author.password", // コメント投稿者のパスワードは比較対象外
              "article.author.password"// 記事著者のパスワードも比較対象外
          )
          .isEqualTo(article1Comment1);
    });
  }

  /**
   * 指定したIDの記事コメントが存在する場合に、selectByIdメソッドが正しい記事コメントエンティティを返すことを検証するテストです。
   *
   * <p>
   * このテストでは以下の手順で処理を行います:
   * </p>
   * <ol>
   *   <li>
   *     <b>Arrange:</b> 事前に {@code expectedComment} をデータベースに挿入し、対象データが存在する状態を作ります。
   *   </li>
   *   <li>
   *     <b>Act:</b> 挿入された記事コメントのIDを用いて {@code selectById} メソッドを呼び出し、記事コメントエンティティを取得します。
   *   </li>
   *   <li>
   *     <b>Assert:</b> 取得したエンティティと、事前に登録した {@code expectedComment} が再帰的な比較により一致することを検証します。<br>
   *     ※ 比較時には、セキュリティ上の理由からコメント投稿者および記事著者のパスワードフィールドは除外しています。
   *   </li>
   * </ol>
   *
   * @see ArticleCommentRepository#selectById(long)
   */
  @Test
  @DisplayName("selectById：指定した ID の記事コメントが存在するとき、記事コメントを返す")
  void selectById_success() {
    // ## Arrange ##
    // テスト対象の環境を整えるため、事前に期待値として定義した記事コメントエンティティをデータベースに登録
    cut.insert(article1Comment1);

    // ## Act ##
    // 登録済みの記事コメントのIDを用いてselectByIdメソッドを呼び出し、データベースから記事コメントエンティティを取得
    var actualOpt = cut.selectById(article1Comment1.getId());

    // ## Assert ##
    // 取得したOptionalに値が存在することを前提に、実際に取得された記事コメントエンティティと期待値(expectedComment)が一致するかを検証
    assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
      // 再帰的な比較を行い、パスワード情報は比較対象から除外して一致していることを確認
      assertThat(actualEntity)
          .usingRecursiveComparison()
          .ignoringFields(
              "author.password", // コメント投稿者のパスワードは比較対象外
              "article.author.password"// 記事著者のパスワードも比較対象外
          )
          .isEqualTo(article1Comment1);
    });
  }

  /**
   * 指定したIDの記事コメントが存在しない場合、selectByIdメソッドがOptional.emptyを返すことを検証するテストです。
   *
   * <p>
   * このテストでは以下の手順で処理を行います:
   * </p>
   * <ol>
   *   <li>
   *     <b>Arrange:</b> {@code expectedComment} をデータベースに挿入して、存在する記事コメントとして登録します。
   *   </li>
   *   <li>
   *     <b>Act:</b> 存在しない記事コメントID（ここでは0）を指定して {@code selectById} を呼び出し、該当エンティティが取得されないことを確認します。
   *   </li>
   *   <li>
   *     <b>Assert:</b> 取得結果が {@code Optional.empty} であることを検証します。
   *   </li>
   * </ol>
   *
   * @see ArticleCommentRepository#selectById(long)
   */
  @Test
  @DisplayName("selectById：指定した ID の記事コメントが存在しないとき、Optional.empty を返す")
  void selectById_returnEmpty() {
    // ## Arrange ##
    cut.insert(article1Comment1);// dummy Record
    var notInsertedId = 0;

    // ## Act ##
    // 存在しない記事コメントIDを指定してselectByIdメソッドを呼び出します。
    var actualOpt = cut.selectById(notInsertedId);

    // ## Assert ##
    assertThat(actualOpt).isEmpty();
  }

  /**
   * selectByArticleId_success: 指定した記事IDに対して、記事コメントが存在する場合、正しい記事コメントのリストが返されることを検証するテスト。
   *
   * <p>
   * 【テストの流れ】
   * <ol>
   *   <li><b>Arrange:</b>
   *     <ul>
   *       <li>記事1に属するコメント（article1Comment1、article1Comment2）をデータベースに挿入。</li>
   *       <li>記事2に属するコメント（article2Comment1）も挿入し、対象記事のコメントのみ取得されることを確認。</li>
   *     </ul>
   *   </li>
   *   <li><b>Act:</b>
   *     <ul>
   *       <li>対象記事（article1）のIDを指定して、selectByArticleId を実行。</li>
   *     </ul>
   *   </li>
   *   <li><b>Assert:</b>
   *     <ul>
   *       <li>返却されるコメントリストのサイズが2件であることを検証。</li>
   *       <li>各コメントの内容が、予め挿入した記事1のコメントと一致することを、再帰的比較（パスワードフィールドは除外）で確認。</li>
   *     </ul>
   *   </li>
   * </ol>
   * </p>
   */
  @Test
  @DisplayName("selectByArticleId：指定した記事IDにコメントが存在するとき、記事コメントのリストを返す")
  void selectByArticleId_success() {
    // ## Arrange ##
    cut.insert(article1Comment1);
    cut.insert(article1Comment2);
    cut.insert(article2Comment1);

    // ## Act ##
    // 記事1に属するコメントを取得
    var actual = cut.selectByArticleId(article1.getId());

    // ## Assert ##
    // 取得したコメントリストのサイズと内容を検証
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .ignoringFields(
            "author.password",
            "article.author.password")
        .isEqualTo(article1Comment1);
    assertThat(actual.get(1))
        .usingRecursiveComparison()
        .ignoringFields(
            "author.password",
            "article.author.password")
        .isEqualTo(article1Comment2);
  }
}