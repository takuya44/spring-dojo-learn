package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.article.ArticleCommentEntity;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
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
    // 記事の著者となるユーザーエンティティを生成（IDはDB自動生成）
    var articleAuthor = new UserEntity(
        null, // IDは自動生成のためnull
        "test_username1", // ユーザー名
        "test_password1", // パスワード（テスト用）
        true              // ユーザーが有効であることを示すフラグ
    );
    // ユーザー情報をデータベースに登録
    userRepository.insert(articleAuthor);

    // テスト用の記事エンティティを生成
    var article = new ArticleEntity(
        null,                                  // IDは自動生成
        "test_title",                             // 記事のタイトル
        "test_body",                              // 記事の本文
        articleAuthor,                            // 記事の著者
        TestDateTimeUtil.of(2020, 1, 1, 10, 30), // 記事の作成日時
        TestDateTimeUtil.of(2021, 1, 1, 10, 30) // 記事の更新日時
    );
    // 記事情報をデータベースに登録
    articleRepository.insert(article);

    // コメント投稿者となる別のユーザーエンティティを生成
    var commentAuthor = new UserEntity(
        null,             // IDは自動生成
        "test_username2",    // ユーザー名
        "test_password2",    // パスワード
        true                 // ユーザーが有効であることを示すフラグ
    );
    // コメント投稿者のユーザー情報をデータベースに登録
    userRepository.insert(commentAuthor);

    // 記事コメントエンティティを生成（IDは自動生成のためnull）
    var expectedComment = new ArticleCommentEntity(
        null,                                 // コメントIDはDB自動生成
        "test_body",                             // コメントの本文
        article,                                 // コメント対象の記事
        commentAuthor,                           // コメント投稿者
        TestDateTimeUtil.of(2022, 1, 1, 10, 31) // コメントの投稿日時
    );

    // ## Act ##
    // テスト対象メソッドを使用して、記事コメントエンティティをデータベースに挿入する
    cut.insert(expectedComment);

    // ## Assert ##
    // 挿入した記事コメントエンティティをIDで取得し、期待値と一致するかを検証する
    var actualOpt = cut.selectById(expectedComment.getId());
    assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
      // 再帰的な比較を行い、パスワード情報は比較対象から除外して一致していることを確認
      assertThat(actualEntity)
          .usingRecursiveComparison()
          .ignoringFields(
              "author.password", // コメント投稿者のパスワードは比較対象外
              "article.author.password"// 記事著者のパスワードも比較対象外
          )
          .isEqualTo(expectedComment);
    });
  }
}