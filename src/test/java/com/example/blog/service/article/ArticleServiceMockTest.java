package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.repository.article.ArticleRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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
        new ArticleEntity(999L, "", "", null, null)
    ));

    // ID 999 の記事が存在することを検証
    assertThat(articleRepository.selectById(999))
        .isPresent() // Optional が空でないことを確認
        .hasValueSatisfying(article -> {
          // 記事のIDが 999 であることを確認
          assertThat(article.getId()).isEqualTo(999);
          // 失敗例：assertThat(article.title()).isEqualTo("title");
        });

    // ID 111 の記事が存在しないことを検証:Optional<null>
    assertThat(articleRepository.selectById(111)).isEmpty();
    // 失敗例：assertThat(articleRepository.selectById(111)).isPresent();
    // 期待値は、値ありだが、実際空。
    // Expecting Optional to contain a value but it was empty.
  }

  /**
   * 指定されたIDの記事が存在するとき、{@link ArticleService#findById(int)} メソッドが 該当する {@link ArticleEntity}
   * を返すことを検証するテストメソッド。
   *
   * <p>このテストでは、モックされた {@link ArticleRepository} が ID 999 の記事を返すように設定されています。
   * テストは、返された {@link Optional} が空でないことを確認し、各フィールドの値が期待通りであるかどうかを検証します。</p>
   *
   * <p>主な検証項目:
   * <ul>
   *   <li>ID 999 に対応する記事が存在すること。</li>
   *   <li>記事の各フィールド（id、title、content、createdAt、updatedAt）が期待通りの値であること。</li>
   * </ul>
   * </p>
   */
  @Test
  @DisplayName("findById: 指定されたIDの記事が存在するとき、ArticleEntityを返す")
  public void findById_returnArticleEntity() {
    // ## Arrange ## モックでID 999 の記事を返すように設定
    when(articleRepository.selectById(999)).thenReturn(Optional.of(
        new ArticleEntity(
            999L,
            "title_999",
            "body_999",
            LocalDateTime.of(2010, 10, 1, 0, 0, 0),
            LocalDateTime.of(2010, 11, 1, 0, 0, 0)
        )
    ));

    // ## Act ## ID 999 の記事を取得
    var actual = cut.findById(999);

    // ## Assert ##
    assertThat(actual)
        .isPresent()// Optional が空でないことを確認
        // 各フィールドの値が期待通りかどうかを確認
        .hasValueSatisfying(article -> {
          assertThat(article.getId()).isEqualTo(999);
          assertThat(article.getTitle()).isEqualTo("title_999");
          assertThat(article.getContent()).isEqualTo("body_999");
          assertThat(article.getCreatedAt()).isEqualTo("2010-10-01T00:00:00");
          assertThat(article.getUpdatedAt()).isEqualTo("2010-11-01T00:00:00");
        });
  }

  /**
   * 指定されたIDの記事が存在しない場合に、{@link ArticleService#findById(int)} が {@link Optional#empty()}
   * を返すことを検証するテストメソッド。
   *
   * <p>このテストでは、モックされた {@link ArticleRepository} が指定されたID 999 に対して
   * 空の {@link Optional} を返すように設定されています。</p>
   *
   * <p>主な検証項目:
   * <ul>
   *   <li>ID 999 に対応する記事が存在しない場合、{@link Optional#empty()} が返されること。</li>
   * </ul>
   * </p>
   */
  @Test
  @DisplayName("findById: 指定されたIDの記事が存在しないとき、Optional.emptyを返す")
  public void findById_returnEmpty() {
    // ## Arrange ## モックでID 999 の記事が存在しない場合を設定
    when(articleRepository.selectById(999)).thenReturn(Optional.empty());

    // ## Act ##
    var actual = cut.findById(999);

    // ## Assert ##
    assertThat(actual).isEmpty(); // 該当する記事がないため、空のOptionalを期待
  }
}
