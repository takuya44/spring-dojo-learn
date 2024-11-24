package com.example.blog.web.controller.user;

import com.example.blog.service.user.UserService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * RESTコントローラークラス。ユーザーに関するAPIエンドポイントを提供する。
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestController {

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
  @GetMapping("/me") // GETリクエストを /users/me にマッピング
  public ResponseEntity<String> me(Principal principal) {
    // Principalオブジェクトから認証されたユーザー名を取得して返す
    return ResponseEntity.ok(principal.getName());
  }

  /**
   * 新しいユーザーを作成する。
   *
   * <p>このエンドポイントは、新しいユーザーを作成し、作成されたユーザーのIDを含むLocationヘッダーを設定します。</p>
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
  @PostMapping
  public ResponseEntity<Void> create(@RequestBody UserForm userForm) {
    // サービス層を呼び出して新しいユーザーを登録
    var newUser = userService.register(userForm.username(), userForm.password());

    // 作成されたユーザーのIDを含むLocationヘッダーを構築
    var location = UriComponentsBuilder.fromPath("/users/{id}")
        .buildAndExpand(newUser.getId())
        .toUri();

    // HTTP 201 Created を返す
    return ResponseEntity
        .created(location) // LocationヘッダーにユーザーのURLを設定
        .build();
  }

}
