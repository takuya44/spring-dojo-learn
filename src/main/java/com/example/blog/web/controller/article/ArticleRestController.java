package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleForm;
import com.example.blog.model.UserDTO;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
import java.net.URI;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
   * <p>具体的な動作:</p>
   * <ul>
   *   <li>認証情報を使用して、現在ログインしているユーザーを特定します。</li>
   *   <li>リクエストで受け取った記事情報をもとに、記事オブジェクトを作成します。</li>
   *   <li>作成された記事の情報をレスポンスボディに含めます。</li>
   *   <li>Locationヘッダーには作成されたリソースのURIを設定します。</li>
   * </ul>
   *
   * @param form 新しい記事のデータを含むリクエストボディ
   * @return HTTP 201 Createdレスポンス
   */
  @Override
  public ResponseEntity<ArticleDTO> createArticle(ArticleForm form) {
    // ## 1. 認証情報から現在ログインしているユーザー情報を取得 ##
    var loggedInUser = (LoggedInUser) SecurityContextHolder
        .getContext() // セキュリティコンテキストを取得
        .getAuthentication() // 認証情報を取得
        .getPrincipal(); // 現在ログインしているユーザーの情報を取得

    // ユーザー情報をDTOオブジェクトに変換
    var userDTO = new UserDTO();
    userDTO.setId(loggedInUser.getUserId());
    userDTO.setUsername(loggedInUser.getUsername());

    // ## 2. 新しい記事データを作成 ##
    var body = new ArticleDTO();
    body.setId(123L);
    body.setTitle(form.getTitle());
    body.setBody(form.getBody());
    body.setAuthor(userDTO);
    body.setCreatedAt(OffsetDateTime.now());
    body.setUpdatedAt(OffsetDateTime.now());

    // TODO: 実際の作成処理（データベース登録など）を実装する
    // 現在はモック（仮）実装で固定値を返しています。

    // ## 3. レスポンスを作成して返す ##
    return ResponseEntity
        .created(URI.create("/articles/123"))
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }
}
