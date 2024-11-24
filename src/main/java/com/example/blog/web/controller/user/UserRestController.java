package com.example.blog.web.controller.user;

import com.example.blog.service.user.UserService;
import java.net.URI;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @GetMapping("/me")
  public ResponseEntity<String> me(Principal principal) {
    return ResponseEntity.ok(principal.getName());
  }

  @PostMapping
  public ResponseEntity<Void> create(@RequestBody UserForm userForm) {
    userService.register(userForm.username(), userForm.password());
    return ResponseEntity
        .created(URI.create("/users/123"))
        .build();
  }

}
