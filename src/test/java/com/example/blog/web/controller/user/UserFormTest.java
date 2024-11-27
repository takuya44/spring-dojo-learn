package com.example.blog.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.model.UserForm;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UserForm クラスのバリデーションロジックをテストするクラス。
 *
 * <p>このテストでは、{@link jakarta.validation.Validator} を使用して、
 * 入力データが {@link UserForm} のバリデーション要件を満たしているかを確認します。</p>
 */
class UserFormTest {

  /**
   * username フィールドに対するバリデーションの正常系テスト。
   *
   * <p>このテストでは、バリデーションルールに準拠した入力データが
   * 適切に検証され、バリデーション違反が発生しないことを確認します。</p>
   *
   * <p>具体例:</p>
   * <ul>
   *   <li>username = "username00"</li>
   *   <li>password = "password00"</li>
   * </ul>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("username のバリデーション：成功")
  void username_success() {
    // ## Arrange ##
    // バリデーションファクトリを使用して Validator インスタンスを生成
    var factory = Validation.buildDefaultValidatorFactory();
    var validator = factory.getValidator();

    // テスト対象の UserForm インスタンスを作成（正しい入力値を設定）
    var cut = new UserForm("username00", "password00");

    // ## Act ##
    // バリデーションを実行し、違反内容を取得
    var violations = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反がないことを確認
    assertThat(violations).isEmpty();
  }

  /**
   * username フィールドに対するバリデーションの異常系テスト。
   *
   * <p>このテストでは、バリデーションルールに違反した入力データが
   * 適切に検証され、バリデーション違反が発生することを確認します。</p>
   *
   * <p>具体例:</p>
   * <ul>
   *   <li>username = null</li>
   *   <li>password = "password00"</li>
   * </ul>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("username のバリデーション：失敗")
  void username_failure() {
    // ## Arrange ##
    // バリデーションファクトリを使用して Validator インスタンスを生成
    var factory = Validation.buildDefaultValidatorFactory();
    var validator = factory.getValidator();

    // テスト対象の UserForm インスタンスを作成（username に null を設定）
    var cut = new UserForm(null, "password00");

    // ## Act ##
    // バリデーションを実行し、違反内容を取得
    var violations = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反が発生していることを確認
    assertThat(violations).isNotEmpty();

    // バリデーション違反の対象が "username" フィールドであることを検証
    assertThat(violations)
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("username"));
  }
}
