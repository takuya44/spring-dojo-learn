package com.example.blog.service.article;

import java.time.LocalDateTime;
import java.util.Optional;

public class ArticleService {

  /**
   * 指定されたIDの記事を検索します。
   *
   * @param id 検索する記事のID
   * @return 指定されたIDの記事を含むOptional<ArticleEntity>オブジェクト
   */
  public Optional<ArticleEntity> findById(long id) {
    // TODO 最終的にはDBから取得する

    return Optional.of(new ArticleEntity(
        id,
        "title",
        "content",
        LocalDateTime.now(),
        LocalDateTime.now()
    ));
  }
}
