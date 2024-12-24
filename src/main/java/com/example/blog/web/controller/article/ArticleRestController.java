package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleForm;
import com.example.blog.model.UserDTO;
import com.example.blog.service.article.ArticleService;
import java.net.URI;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 記事に関するREST APIエンドポイントを提供するコントローラークラス。
 *
 * <p>このクラスは {@link ArticlesApi} インターフェースを実装し、記事に関する操作を提供します。</p>
 * <ul>
 *   <li>記事の詳細を取得</li>
 *   <li>記事を新規作成</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class ArticleRestController implements ArticlesApi {

  private final ArticleService articleService;

//  /** 練習用で作成したので、一度削除した。自作したArticleDTOクラスも削除すること
//   * 指定されたIDの記事を表示します。
//   *
//   * <p>具体例:
//   * <pre>{@code
//   * // URL: http://localhost:8080/articles/1
//   * // レスポンス:
//   * // {
//   * //   "id": 1,
//   * //   "title": "This is title: id = 1",
//   * //   "content": "This is content",
//   * //   "createdAt": "2024-06-08T12:34:56.789",
//   * //   "updatedAt": "2024-06-08T12:34:56.789"
//   * // }
//   * }</pre>
//   * </p>
//   *
//   * @param id 表示する記事のID
//   * @return 指定されたIDの記事を表す {@link ArticleDTO_自作クラス_使用してない} オブジェクト
//   * @throws ResourceNotFoundException 指定されたIDの記事が存在しない場合
//   */
//  @GetMapping("/articles/{id}")
//  public ArticleDTO_自作クラス_使用してない showArticle(@PathVariable("id") long id) {
//    return articleService.findById(id)
//        .map(ArticleDTO_自作クラス_使用してない::from)
//        .orElseThrow(ResourceNotFoundException::new);
//  }

  /**
   * 新しい記事を作成します。
   *
   * <p>このエンドポイントは、記事の作成リクエストを受け付け、作成成功時に201 Createdレスポンスを返します。
   * Locationヘッダーには作成されたリソースのURIを設定します。</p>
   *
   * @return HTTP 201 Createdレスポンス
   */
  @Override
  public ResponseEntity<ArticleDTO> createArticle(ArticleForm form) {
    var userDTO = new UserDTO();
    userDTO.setId(99L);
    userDTO.setUsername("user1");

    var body = new ArticleDTO();
    body.setId(123L);
    body.setTitle(form.getTitle());
    body.setBody(form.getBody());
    body.setAuthor(userDTO);
    body.setCreatedAt(OffsetDateTime.now());
    body.setUpdatedAt(OffsetDateTime.now());

    // TODO: 実際の作成処理を実装
    return ResponseEntity
        .created(URI.create("/articles/123")) // TODO 最終自動再版されたIDを使う
        .contentType(MediaType.APPLICATION_JSON)
        .body(body); // TODO mock実装中。最終DBに登録された物を渡す
  }
}
