package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.service.article.ArticleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@MybatisDefaultDatasourceTest
class ArticleRepositoryTest {

  // テスト対象（cut: class under test）のArticleRepositoryを自動的に注入
  @Autowired
  private ArticleRepository cut;

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
   * データベースに挿入しています。この場合、ID 999の記事が 'title_999', 'content_999' という内容で 挿入されています。</p>
   *
   * <p>テストの主な検証点は次の通りです:
   * <ul>
   *   <li>IDが999である記事が正しく返されること。</li>
   *   <li>返された記事のタイトル、本文、作成日、更新日が正しいこと。</li>
   * </ul>
   * </p>
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
}