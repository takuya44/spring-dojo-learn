package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;

  /**
   * 指定されたIDの記事を検索します。
   *
   * @param id 検索する記事のID
   * @return 指定されたIDの記事を含むOptional<ArticleEntity>オブジェクト
   */
  public Optional<ArticleEntity> findById(long id) {
    return articleRepository.selectById(id);
  }
}
