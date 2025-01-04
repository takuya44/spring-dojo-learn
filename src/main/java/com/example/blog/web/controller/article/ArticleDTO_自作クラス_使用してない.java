package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleEntity;
import java.time.LocalDateTime;

/**
 * 記事を表すデータ転送オブジェクト（DTO）。
 * <p>
 * このレコードは、記事のID、タイトル、内容、作成日時、および更新日時を保持します。
 * </p>
 *
 * @param id        記事のID
 * @param title     記事のタイトル
 * @param content   記事の内容
 * @param createdAt 記事の作成日時
 * @param updatedAt 記事の更新日時
 */
public record ArticleDTO_自作クラス_使用してない(
    long id,
    String title,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  /**
   * ArticleEntityからArticleDTOを作成します。
   *
   * @param entity ArticleEntityオブジェクト
   * @return ArticleDTOオブジェクト
   */
  public static ArticleDTO_自作クラス_使用してない from(ArticleEntity entity) {
    return new ArticleDTO_自作クラス_使用してない(
        entity.getId(),
        entity.getTitle(),
        entity.getBody(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
