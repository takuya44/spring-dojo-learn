package com.example.blog.web.controller.user;

import com.example.blog.api.UsersApi;
import com.example.blog.model.UserDTO;
import com.example.blog.model.UserForm;
import com.example.blog.service.user.UserService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
