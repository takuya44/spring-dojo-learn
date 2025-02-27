package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.ArticleCommentDTO;
import com.example.blog.model.ArticleCommentForm;
import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleForm;
import com.example.blog.model.ArticleListDTO;
import com.example.blog.model.ArticleListItemDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleCommentService;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.exception.ResourceNotFoundException;
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
  private final ArticleCommentService articleCommentService;

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

    // 記事データを DTO に変換
    var body = ArticleMapper.toArticleDTO(newArticle);

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

  /**
   * 記事一覧を取得するエンドポイントの実装。
   *
   * <p>このメソッドは記事の一覧を取得し、各記事を DTO に変換してクライアントに返します。
   * 記事ごとに著者情報も DTO に変換して含めます。</p>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>サービス層から記事データを取得します。</li>
   *   <li>各記事データを {@link ArticleListItemDTO} に変換します。</li>
   *   <li>著者情報を {@link UserDTO} に変換し、記事DTOに設定します。</li>
   *   <li>変換後の DTO を {@link ArticleListDTO} に格納して返却します。</li>
   * </ol>
   *
   * @return 記事一覧を含む HTTP レスポンス
   */
  @Override
  public ResponseEntity<ArticleListDTO> getArticleList() {
    // 記事データを取得し、DTO に変換
    var items = articleService.findAll()
        .stream()
        .map(ArticleMapper::toArticleListItemDTO)
        .toList();

    // レスポンスボディを作成
    var body = new ArticleListDTO();
    body.setItems(items);

    // HTTP レスポンスを返却
    return ResponseEntity
        .ok(body);
  }

  /**
   * 指定された記事IDに基づいて記事の詳細を取得するエンドポイント。
   *
   * <p>このメソッドでは、記事データをサービス層から取得し、DTO に変換してクライアントに返却します。</p>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>サービス層の {@code findById} メソッドを呼び出し、指定された記事IDで記事を検索。</li>
   *   <li>記事が見つかった場合:
   *     <ul>
   *       <li>著者情報を {@link UserDTO} に変換。</li>
   *       <li>記事情報を {@link ArticleDTO} に変換。</li>
   *       <li>変換した DTO を HTTP 200 OK レスポンスとして返却。</li>
   *     </ul>
   *   </li>
   *   <li>記事が見つからなかった場合、{@link ResourceNotFoundException} をスロー。</li>
   * </ol>
   *
   * @param articleId 取得する記事のID
   * @return 指定された記事の詳細を含む HTTP レスポンス
   * @throws ResourceNotFoundException 指定された記事IDに対応する記事が存在しない場合
   */
  @Override
  public ResponseEntity<ArticleDTO> getArticle(Long articleId) {
    return articleService.findById(articleId)
        .map(ArticleMapper::toArticleDTO)
        .map(ResponseEntity::ok)
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * 指定された記事を更新するエンドポイント。
   *
   * <p>このメソッドは、認証されたユーザーの情報を基に、指定された記事を更新します。
   * 更新された記事データを DTO に変換し、HTTP レスポンスとして返します。</p>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>セキュリティコンテキストから現在ログイン中のユーザー情報を取得。</li>
   *   <li>サービス層の {@code update} メソッドを呼び出して記事を更新。</li>
   *   <li>更新が成功した場合:
   *     <ul>
   *       <li>更新された記事データを DTO に変換。</li>
   *       <li>HTTP ステータスコード 200 OK のレスポンスとして返却。</li>
   *     </ul>
   *   </li>
   *   <li>更新対象の記事が存在しない場合:
   *     <ul>
   *       <li>{@link ResourceNotFoundException} をスローし、HTTP ステータスコード 404 Not Found を返す。</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @param articleId 更新する記事のID
   * @param form      更新内容を含むリクエストボディ
   * @return 更新された記事データを含む HTTP レスポンス
   * @throws ResourceNotFoundException 指定された記事が見つからない場合
   */
  @Override
  public ResponseEntity<ArticleDTO> updateArticle(
      Long articleId,
      ArticleForm form
  ) {
    // ## 1. 認証情報から現在ログインしているユーザー情報を取得 ##
    var loggedInUser = (LoggedInUser) SecurityContextHolder
        .getContext() // セキュリティコンテキストを取得
        .getAuthentication() // 認証情報を取得
        .getPrincipal(); // 現在ログインしているユーザーの情報を取得

    // ## 2. 指定された記事を更新 ##
    var entity = articleService.update(
        articleId,
        loggedInUser.getUserId(),
        form.getTitle(),
        form.getBody()
    );

    return ResponseEntity
        .ok(ArticleMapper.toArticleDTO(entity));
  }

  /**
   * 指定された記事IDに対応する記事を削除し、削除成功時は HTTP 204 No Content のレスポンスを返します。
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>
   *     セキュリティコンテキストから現在ログインしているユーザー情報（LoggedInUser）を取得します。
   *     これにより、リクエストを送信したユーザーが誰であるかを判定します。
   *   </li>
   *   <li>
   *     取得したユーザーのIDと指定された記事IDを基に、記事削除処理を実行します。
   *     この際、記事が実際にそのユーザーによって作成されたものであることを内部で確認することが前提です。
   *   </li>
   *   <li>
   *     削除処理が成功した場合、HTTP 204 No Content のレスポンスを返します。
   *   </li>
   * </ol>
   *
   * @param articleId 削除対象の文章のID
   * @return 削除成功時に内容が空の HTTP 204 No Content レスポンス
   */
  @Override
  public ResponseEntity<Void> deleteArticle(Long articleId) {
    // ## 1. 認証情報から現在ログインしているユーザー情報を取得 ##
    // セキュリティコンテキストから認証情報を取り出し、現在ログインしているユーザーの詳細（LoggedInUser）を取得します。
    var loggedInUser = (LoggedInUser) SecurityContextHolder
        .getContext() // セキュリティコンテキストを取得
        .getAuthentication() // 認証情報を取得
        .getPrincipal(); // 現在ログインしているユーザーの情報を取得

    // ## 2. 指定された記事を削除 ##
    // 取得したユーザーIDとリクエストで指定された記事IDを元に、記事削除処理を実行します。
    articleService.delete(
        loggedInUser.getUserId(),
        articleId
    );

    // 削除処理が完了したら、HTTP 204 No Content レスポンスを返却します。
    return ResponseEntity
        .noContent()
        .build();
  }

  /**
   * 指定された記事に新しいコメントを作成する。
   *
   * <p>このメソッドは、認証済みのユーザーによるコメント投稿を処理し、
   * 作成されたコメントの詳細情報を返します。</p>
   *
   * <p>処理の流れ:</p>
   * <ul>
   *   <li>認証情報を取得し、現在のユーザーを特定する。</li>
   *   <li>記事に紐づく新しいコメントを作成する。</li>
   *   <li>作成されたコメントのデータを DTO に変換する。</li>
   *   <li>レスポンスの Location ヘッダーに作成されたコメントの URI を設定する。</li>
   *   <li>HTTP 201 Created ステータスと共にレスポンスを返す。</li>
   * </ul>
   *
   * @param articleId コメントを追加する記事の ID
   * @param form      コメントの内容を含むリクエストデータ
   * @return 作成されたコメントの詳細情報を含むレスポンス
   */
  @Override
  public ResponseEntity<ArticleCommentDTO> createComment(
      Long articleId,
      ArticleCommentForm form
  ) {
    // ## 1. 認証情報から現在ログインしているユーザー情報を取得 ##
    var loggedInUser = (LoggedInUser) SecurityContextHolder
        .getContext() // セキュリティコンテキストを取得
        .getAuthentication() // 認証情報を取得
        .getPrincipal(); // 現在ログインしているユーザーの情報を取得

    // ## 2. 記事に対する新しいコメントを作成 ##
    var newComment = articleCommentService.create(
        loggedInUser.getUserId(), // コメントの投稿者 ID
        articleId,                // コメントを追加する記事の ID
        form.getBody()            // コメントの本文
    );

    // ## 3. 作成されたコメントのデータを DTO に変換 ##
    var body = ArticleCommentMapper.toArticleDTO(newComment);

    // ## 3. Location ヘッダーにリソース URI を設定 ##
    var location = UriComponentsBuilder
        .fromPath("/articles/{articleId}/comments/{commentId}")
        .buildAndExpand(articleId, newComment.getId())
        .toUri();

    // ## 5. HTTP 201 Created レスポンスを返す ##
    return ResponseEntity
        .created(location) // HTTP 201 Created ステータスと Location ヘッダーを設定
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }
}
