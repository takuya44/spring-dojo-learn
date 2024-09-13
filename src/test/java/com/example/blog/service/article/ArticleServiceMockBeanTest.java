package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.repository.article.ArticleRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
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
        new ArticleEntity(999, "", "", null, null)
    ));

    // ID 999 の記事が存在することを検証
    assertThat(articleRepository.selectById(999))
        .isPresent() // Optional が空でないことを確認
        .hasValueSatisfying(article ->
            assertThat(article.id()).isEqualTo(999)
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
            999,
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
          assertThat(article.id()).isEqualTo(999);
          assertThat(article.title()).isEqualTo("title_999");
          assertThat(article.content()).isEqualTo("body_999");
          assertThat(article.createdAt()).isEqualTo("2010-10-01T00:00:00");
          assertThat(article.updatedAt()).isEqualTo("2010-11-01T00:00:00");
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
