package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.repository.article.ArticleRepository;
import java.util.Optional;
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

  /**
   * {@link ArticleRepository#selectById(int)} のMockに慣れるためのテスト
   * <p>このテストでは、ID 999 に対応する記事が存在する場合と、ID 111 に対応する記事が存在しない場合の動作を検証します。</p>
   *
   * <p>主な検証項目:
   * <ul>
   *   <li>ID 999 に対応する記事が存在する場合、{@link Optional} が {@link Optional#isPresent()} を返すこと。</li>
   *   <li>ID 999 に対応する記事のIDが 999 であること。</li>
   *   <li>ID 111 に対応する記事が存在しない場合、{@link Optional#isEmpty()} を返すこと。</li>
   * </ul>
   * </p>
   */
  @Test
  public void mock() {
    // ID 999 に対応する記事が存在する場合のモックを設定
    when(articleRepository.selectById(999)).thenReturn(Optional.of(
        new ArticleEntity(999, "", "", null, null)
    ));

    // ID 999 の記事が存在することを検証
    assertThat(articleRepository.selectById(999))
        .isPresent() // Optional が空でないことを確認
        .hasValueSatisfying(article -> {
          // 記事のIDが 999 であることを確認
          assertThat(article.id()).isEqualTo(999);
          // 失敗例：assertThat(article.title()).isEqualTo("title");
        });

    // ID 111 の記事が存在しないことを検証:Optional<null>
    assertThat(articleRepository.selectById(111)).isEmpty();
    // 失敗例：assertThat(articleRepository.selectById(111)).isPresent();
    // 期待値は、値ありだが、実際空。
    // Expecting Optional to contain a value but it was empty.
  }
}
