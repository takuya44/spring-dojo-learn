package com.example.blog.service.article;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ArticleService} の単体テストクラス。
 *
 * <p>{@link SpringBootTest} アノテーションを使用して、Springコンテキストをロードし、
 * Springが管理するコンポーネント（例: @Service, @Repository）をテスト環境で利用可能にします。</p>
 *
 * <p>{@link Transactional} アノテーションを使用して、テストごとにトランザクションを張り、
 * テスト終了後にロールバックすることで、データベースの状態をテスト前の状態に戻します。</p>
 */
@SpringBootTest
@Transactional
class ArticleServiceTest {

  // テスト対象のArticleServiceをSpringから注入
  @Autowired
  private ArticleService cut;

  /**
   * ArticleServiceがSpringコンテキスト内で正しく初期化され、 依存関係が注入されていることを検証するテストメソッド。
   */
  @Test
  public void cut() {
    assertThat(cut).isNotNull();
  }

  /**
   * 指定されたIDの記事が存在する場合に、{@link ArticleService#findById(int)} が正しく {@link ArticleEntity}
   * を返すことを検証するテストメソッド。
   *
   * <p>テストの準備段階で、@Sqlアノテーションを使用して、テストデータとしてID 999の記事をデータベースに挿入しています。</p>
   *
   * <p>テストの主な検証項目:
   * <ul>
   *   <li>ID 999の記事が正しくデータベースから取得されること。</li>
   *   <li>取得された記事の各フィールド（ID、タイトル、本文、作成日、更新日）が期待通りの値であること。</li>
   * </ul>
   * </p>
   */
  @Test
  @DisplayName("findById: 指定されたIDの記事が存在するとき、ArticleEntityを返す")
  @Sql(statements = {"""
      INSERT INTO articles (id, title, body, created_at, updated_at)
      VALUES (999, 'title_999', 'body_999', '2010-10-01 00:00:00', '2010-11-01 00:00:00');
      """
  })
  public void findById_returnArticleEntity() {
    // ## Arrange ##

    // ## Act ##
    var actual = cut.findById(999);

    // ## Assert ##
    assertThat(actual)
        .isPresent()// Optional が空でないことを確認
        // 各フィールドの値が期待通りかどうかを確認
        .hasValueSatisfying(article -> {
          assertThat(article.id()).isEqualTo(999);
          assertThat(article.title()).isEqualTo("title_999");
          assertThat(article.content()).isEqualTo("body_999");
          assertThat(article.createdAt()).isEqualTo("2010-10-01T00:00:00");
          assertThat(article.updatedAt()).isEqualTo("2010-11-01T00:00:00");
        });
  }
}