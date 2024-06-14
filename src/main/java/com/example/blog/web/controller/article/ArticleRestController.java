package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * RESTコントローラークラス。このクラスは、記事に関するREST APIエンドポイントを提供します。
 */
@RestController
@RequiredArgsConstructor
public class ArticleRestController {

  private final ArticleService articleService;

  /**
   * 指定されたIDの記事を表示します。
   *
   * <p>具体例:
   * <pre>{@code
   * // URL: http://localhost:8080/articles/1
   * // レスポンス:
   * // {
   * //   "id": 1,
   * //   "title": "This is title: id = 1",
   * //   "content": "This is content",
   * //   "createdAt": "2024-06-08T12:34:56.789",
   * //   "updatedAt": "2024-06-08T12:34:56.789"
   * // }
   * }</pre>
   * </p>
   *
   * @param id 表示する記事のID
   * @return 指定されたIDの記事を表すArticleDTOオブジェクト
   */
  @GetMapping("/articles/{id}")
  public ArticleDTO showArticle(@PathVariable("id") long id) {
    return articleService.findById(id)
        .map(ArticleDTO::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
