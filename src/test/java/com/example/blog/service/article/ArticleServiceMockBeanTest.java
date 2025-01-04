package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.article.ArticleRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

/**
 * {@link ArticleService} のテストクラスで、MyBatisとSpringのコンテキストを使用して、
 * 実際のデータベースではなく、モックや設定されたデータソースを利用してテストを行う。
 *
 * <p>このクラスでは、主に次のアノテーションが使用されています:
 * <ul>
 *   <li>{@link MybatisTest}: MyBatisに特化したテストをサポートするアノテーションで、MyBatisのコンポーネントをロードしてテストを実行します。
 *   通常、リポジトリ層（Mapperインターフェース）やデータベースとの接続に関連するテストに使用します。
 *   このアノテーションを使用すると、自動的にインメモリデータベース（H2など）が設定されます。</li>
 *
 *   <li>{@link AutoConfigureTestDatabase}: テスト用データベースを自動設定するためのアノテーションです。
 *   このクラスでは `replace = AutoConfigureTestDatabase.Replace.NONE` が指定されているため、Springが自動的にインメモリデータベースを使用するのを無効化し、
 *   代わりに、`application.yml`や`application.properties`に定義されたデータベース設定を使用します。</li>
 *
 *   <li>{@link Import}: Springコンテキストに特定のクラス（この場合は {@link ArticleService}）を追加して、
 *   そのクラスがテスト対象として利用できるようにします。
 *   ここでは、テスト対象として {@link ArticleService} を明示的にSpringコンテキストに登録しています。</li>
 * </ul>
 * </p>
 */
@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceMockBeanTest {

  @MockBean
  private ArticleRepository articleRepository;

  @Autowired
  private ArticleService cut;

  @Test
  public void cut() {
    assertThat(cut).isNotNull();
  }

  @Test
  public void mock() {
    // ID 999 に対応する記事が存在する場合のモックを設定
    when(articleRepository.selectById(999)).thenReturn(Optional.of(
        new ArticleEntity(999L, "", "", null, null, null)
    ));

    // ID 999 の記事が存在することを検証
    assertThat(articleRepository.selectById(999))
        .isPresent() // Optional が空でないことを確認
        .hasValueSatisfying(article ->
            assertThat(article.getId()).isEqualTo(999)
        );

    // ID 111 の記事が存在しないことを検証:Optional<null>
    assertThat(articleRepository.selectById(111)).isEmpty();
  }

  @Test
  @DisplayName("findById: 指定されたIDの記事が存在するとき、ArticleEntityを返す")
  public void findById_returnArticleEntity() {
    // ## Arrange ## モックでID 999 の記事を返すように設定
    when(articleRepository.selectById(999)).thenReturn(Optional.of(
        new ArticleEntity(
            999L,
            "title_999",
            "body_999",
            null,
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
          assertThat(article.getBody()).isEqualTo("body_999");
          assertThat(article.getCreatedAt()).isEqualTo("2010-10-01T00:00:00");
          assertThat(article.getUpdatedAt()).isEqualTo("2010-11-01T00:00:00");
        });
  }

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
