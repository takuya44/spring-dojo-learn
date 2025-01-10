package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleForm;
import com.example.blog.model.UserDTO;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

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
   * 記事を新規作成するエンドポイント。
   *
   * <p>このメソッドは、記事の作成リクエストを処理し、
   * 作成成功時に HTTP 201 Created レスポンスを返します。 また、レスポンスの Location ヘッダーには作成されたリソースの URI を設定します。</p>
   *
   * <p>主な処理内容:</p>
   * <ul>
   *   <li>現在ログインしているユーザーを認証情報から取得します。</li>
   *   <li>リクエストで受け取ったデータを使用して、新しい記事オブジェクトを作成します。</li>
   *   <li>作成された記事情報を DTO に変換し、レスポンスボディに含めます。</li>
   *   <li>Location ヘッダーにリソース URI を設定してレスポンスを返します。</li>
   * </ul>
   *
   * @param form 新しい記事のデータを含むリクエストボディ。
   * @return HTTP 201 Created レスポンス。
   */
  @Override
  public ResponseEntity<ArticleDTO> createArticle(ArticleForm form) {
    // ## 1. 認証情報から現在ログインしているユーザー情報を取得 ##
    var loggedInUser = (LoggedInUser) SecurityContextHolder
        .getContext() // セキュリティコンテキストを取得
        .getAuthentication() // 認証情報を取得
        .getPrincipal(); // 現在ログインしているユーザーの情報を取得

    // ## 2. 新しい記事を作成 ##
    var newArticle = articleService.create(
        loggedInUser.getUserId(),
        form.getTitle(),
        form.getBody()
    );

    // ユーザー情報をDTOオブジェクトに変換
    var userDTO = new UserDTO();
    userDTO.setId(newArticle.getAuthor().getId());
    userDTO.setUsername(newArticle.getAuthor().getUsername());

    // 記事データを DTO に変換
    var body = new ArticleDTO();
    body.setId(newArticle.getId());
    body.setTitle(newArticle.getTitle());
    body.setBody(newArticle.getBody());
    body.setAuthor(userDTO);
    body.setCreatedAt(newArticle.getCreatedAt());
    body.setUpdatedAt(newArticle.getUpdatedAt());

    // ## 3. Location ヘッダーにリソース URI を設定 ##
    var location = UriComponentsBuilder.fromPath("/articles/{id}")
        .buildAndExpand(newArticle.getId())
        .toUri();

    // ## 4. HTTP レスポンスを返す ##
    return ResponseEntity
        .created(location) // HTTP 201 Created ステータスと Location ヘッダーを設定
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }
}
