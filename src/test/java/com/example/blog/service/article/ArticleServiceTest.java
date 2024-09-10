package com.example.blog.service.article;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}