package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.repository.article.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ArticleService} のモックテストクラス。
 *
 * <p>{@link MockitoExtension} を使用して、依存関係をモック化しながらテストを実行します。
 * このテストでは、{@link ArticleRepository} をモック化して、{@link ArticleService} の テストを行います。</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceMockTest {

  /**
   * {@link ArticleRepository} のモック。
   * <p>このモックは、{@link ArticleService} 内で使われる依存関係として注入されます。
   * テスト中に実際のデータベースには接続せず、モックが返す値で動作確認を行います。</p>
   */
  @Mock
  private ArticleRepository articleRepository;

  /**
   * テスト対象となる {@link ArticleService}。
   * <p>モックされた {@link ArticleRepository} が依存関係として注入されます。
   * このテストでは {@link ArticleService} のメソッドが正しく初期化されるかを検証します。</p>
   */
  @InjectMocks
  private ArticleService cut;

  /**
   * {@link ArticleService} が正しく初期化され、モックが注入されていることを確認するテスト。
   * <p>{@link assertThat} を使って、{@link ArticleService} が null ではないことを確認します。</p>
   */
  @Test
  public void cut() {
    assertThat(cut).isNotNull();
  }

}
