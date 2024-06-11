package com.example.blog.service.article;

import java.time.LocalDateTime;

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
public record ArticleEntity(
    long id,
    String title,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
