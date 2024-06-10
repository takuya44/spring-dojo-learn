package com.example.blog.web.controller.article;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * RESTコントローラークラス。このクラスは、記事に関するREST APIエンドポイントを提供します。
 */
@RestController
public class ArticleRestController {

  /**
   * 指定されたIDの記事を表示します。
   *
   * @param id 表示する記事のID
   * @return 指定されたIDの記事を表す文字列
   */
  @GetMapping("/articles/{id}")
  public String showArticle(@PathVariable("id") long id) {
    return "This is article: id = " + id;
  }
}
