package com.example.blog.service.article;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 記事を表すエンティティクラス。
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
@AllArgsConstructor
@Data
public class ArticleEntity {

  private Long id;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
