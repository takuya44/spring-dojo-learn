package com.example.blog.web.controller.user;

import com.example.blog.model.UserForm;
import com.example.blog.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * ユーザー名の重複をチェックするカスタムバリデータ。
 *
 * <p>このバリデータは、{@link UserForm} に対して、username フィールドの値が
 * 既にデータベースに存在するかをチェックします。</p>
 *
 * <p>検証対象のオブジェクトがサポートされているかを {@link #supports(Class)} で確認し、
 * 実際のバリデーションロジックは {@link #validate(Object, Errors)} に実装されています。</p>
 */
@Component
@RequiredArgsConstructor
public class DuplicateUsernameValidator implements Validator {

  // ユーザー名の存在確認に使用するサービス
  private final UserService userService;

  /**
   * このバリデータがサポートするクラスを指定します。
   *
   * @param clazz バリデーション対象のクラス
   * @return {@code true} サポートされる場合、{@code false} それ以外の場合
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return UserForm.class.isAssignableFrom(clazz);
  }

  /**
   * username フィールドの値が既にデータベースに存在するかをチェックします。
   *
   * <p>もし存在する場合、Errors オブジェクトにエラーを追加します。</p>
   *
   * @param target 検証対象のオブジェクト
   * @param errors 検証エラーを格納するオブジェクト
   */
  @Override
  public void validate(Object target, Errors errors) {
    // 検証対象のオブジェクトを UserForm 型にキャスト
    var form = (UserForm) target;

    // ユーザー名が既に存在する場合、エラーを追加
    if (userService.existsUsername(form.getUsername())) {
      // "username" フィールドに重複エラーを追加
      errors.rejectValue("username", "duplicate.userForm.username");
    }
  }
}
