package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.service.article.ArticleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

// MyBatisのテストを行うためのテストクラス
// @MybatisTestアノテーションは、MyBatis関連のコンポーネントのみをロードし、テストを行います。
@MybatisTest
// 実際のデータベースを使用する設定を行うアノテーション。
// replace = AutoConfigureTestDatabase.Replace.NONEにより、Springがデフォルトで組み込みデータベースを使用するのを防ぎ、
// application.ymlやapplication.propertiesで定義された実際のデータベース設定を使用します。
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
      INSERT INTO articles (id, title, body, created_at, updated_at)
      VALUES (999, 'title_999', 'content_999', '2010-10-01 00:00:00', '2010-11-01 00:00:00');
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
          assertThat(article.id()).isEqualTo(999);
          assertThat(article.title()).isEqualTo("title_999");
          assertThat(article.content()).isEqualTo("content_999");
          assertThat(article.createdAt()).isEqualTo("2010-10-01T00:00:00");
          assertThat(article.updatedAt()).isEqualTo("2010-11-01T00:00:00");
        });
  }
}