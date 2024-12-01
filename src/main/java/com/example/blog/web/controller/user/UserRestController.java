package com.example.blog.web.controller.user;

import com.example.blog.api.UsersApi;
import com.example.blog.model.BadRequest;
import com.example.blog.model.ErrorDetail;
import com.example.blog.model.UserDTO;
import com.example.blog.model.UserForm;
import com.example.blog.service.user.UserService;
import java.security.Principal;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * ユーザーに関するAPIエンドポイントを提供するRESTコントローラー。
 *
 * <p>このクラスは {@link UsersApi} インターフェースを実装し、以下のエンドポイントを提供します:</p>
 * <ul>
 *   <li>/users/me - 現在ログインしているユーザー情報を取得</li>
 *   <li>/users - 新しいユーザーを作成</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class UserRestController implements UsersApi {

  private final UserService userService;
  private final DuplicateUsernameValidator duplicateUsernameValidator;

  /**
   * DataBinder にカスタムバリデータを登録するためのメソッド。
   *
   * <p>Spring MVC において、リクエストデータをバインドする際にこのメソッドが呼び出され、
   * カスタムバリデーションロジックを追加することができます。 この場合、{@link DuplicateUsernameValidator}
   * を登録し、ユーザー名の重複チェックを行います。</p>
   *
   * <p>具体的な動作:</p>
   * <ul>
   *   <li>POST リクエストで送信された {@link UserForm} のデータを検証する際に、
   *       ユーザー名が既に存在していないかチェック。</li>
   *   <li>重複している場合、`username` フィールドに対してバリデーションエラーを登録。</li>
   * </ul>
   *
   * @param dataBinder リクエストデータをバインドするための {@link DataBinder}
   */
  @InitBinder
  public void initBinder(DataBinder dataBinder) {
    // DuplicateUsernameValidator を登録
    dataBinder.addValidators(duplicateUsernameValidator);
  }

  /**
   * 現在認証されているユーザーの情報を取得する。
   *
   * <p>このエンドポイントは、現在ログインしているユーザーの名前を返す。
   *
   * <p>具体例:
   * <pre>{@code
   * GET /users/me
   * レスポンス: "username"
   * }</pre>
   * </p>
   *
   * @param principal 現在認証されているユーザーの情報を含むPrincipalオブジェクト
   * @return 認証されたユーザーの名前を含むレスポンス
   */
  @GetMapping("/users/me") // GETリクエストを /users/me にマッピング
  public ResponseEntity<String> me(Principal principal) {
    // Principalオブジェクトから認証されたユーザー名を取得して返す
    return ResponseEntity.ok(principal.getName());
  }

  /**
   * 新しいユーザーを作成します。
   *
   * <p>受け取ったユーザー情報をもとに新しいユーザーを作成し、そのユーザーIDをLocationヘッダーに含むレスポンスを返します。</p>
   *
   * <p>具体例:</p>
   * <pre>{@code
   * POST /users
   * リクエストボディ:
   * {
   *   "username": "newuser",
   *   "password": "password123"
   * }
   * レスポンス: HTTP 201 Created, Location: /users/1
   * }</pre>
   *
   * @param userForm 新しいユーザーのデータを含むリクエストボディ
   * @return HTTP 201 Created と Locationヘッダーを含むレスポンス
   */
  @Override
  public ResponseEntity<UserDTO> createUser(UserForm userForm) {
    // サービス層を呼び出して新しいユーザーを登録
    var newUser = userService.register(userForm.getUsername(), userForm.getPassword());

    // 作成されたユーザーのIDを含むLocationヘッダーを構築
    var location = UriComponentsBuilder.fromPath("/users/{id}")
        .buildAndExpand(newUser.getId())
        .toUri();

    // ユーザー情報をDTOにマッピング（UserEntity から UserDTO へ変換）
    // UserEntity はデータベース層から取得されるエンティティクラスで、
    // UserDTO はプレゼンテーション層（APIレスポンス）で使用されるデータ転送オブジェクト。
    // 必要なフィールドのみを抽出し、プレゼンテーション層に渡す形式に変換する。PWは渡さない。
    var dto = new UserDTO();
    dto.setId(newUser.getId());
    dto.setUsername(newUser.getUsername());

    // HTTP 201 Created ステータスとレスポンスボディを返す
    return ResponseEntity
        .created(location) // LocationヘッダーにユーザーのURLを設定
        .body(dto);        // 作成したユーザー情報をレスポンスに含む
  }

  /**
   * バリデーション失敗時に 400 Bad Request を返すための例外ハンドラ。
   *
   * <p>このメソッドは {@link MethodArgumentNotValidException} をキャッチし、リクエストデータのバリデーションエラーを
   * クライアントに返すためのレスポンスを生成します。</p>
   *
   * <p>具体的な動作:</p>
   * <ul>
   *   <li>例外オブジェクト {@link MethodArgumentNotValidException} からエラーメッセージを取得。</li>
   *   <li>エラー詳細リストを構築し、カスタムレスポンスオブジェクト {@link BadRequest} に設定。</li>
   *   <li>HTTP 400 Bad Request のレスポンスを返す。</li>
   * </ul>
   *
   * @param e バリデーションエラーを含む例外オブジェクト
   * @return バリデーションエラーの詳細を含む HTTP 400 Bad Request レスポンス
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BadRequest> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e
  ) {
    // バリデーションエラー情報を格納するカスタムオブジェクトを作成
    var body = new BadRequest();

    // 例外オブジェクトからレスポンス用のプロパティをコピー
    BeanUtils.copyProperties(e.getBody(), body);

    // バリデーションエラーの詳細をリスト形式で収集
    var errorDetailList = new ArrayList<ErrorDetail>();

    for (final FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      // フィールドエラー情報を収集
      var pointer = "#/" + fieldError.getField(); // エラー発生箇所を JSON Pointer 形式で表現
      var detail = fieldError.getCode(); // バリデーションエラーメッセージを取得

      // ErrorDetail オブジェクトを生成
      var errorDetail = new ErrorDetail();
      errorDetail.setPointer(pointer);
      errorDetail.setDetail(detail);

      // エラーディテールリストに追加
      errorDetailList.add(errorDetail);
    }

    // エラーディテールリストをレスポンスオブジェクトに設定
    body.setErrors(errorDetailList);

    // HTTP 400 Bad Request レスポンスを生成
    return ResponseEntity
        .badRequest() // ステータスコード 400 を設定
        .body(body); // レスポンスボディにエラー詳細を含むオブジェクトを設定
  }
}
