package com.example.blog.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.model.UserForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * UserForm クラスのバリデーションロジックをテストするクラス。
 *
 * <p>このテストでは、{@link jakarta.validation.Validator} を使用して、
 * 入力データが {@link UserForm} のバリデーション要件を満たしているかを確認します。</p>
 */
class UserFormTest {

  private ValidatorFactory factory; // バリデーションの設定を生成するファクトリ
  private Validator validator; // 実際にバリデーションを行うインスタンス


  /**
   * 各テストの実行前に、バリデーションのセットアップを行う。 - デフォルトのValidatorFactoryを作成 - バリデータを取得
   */
  @BeforeEach
  void beforeEach() {
    // テスト前にバリデーションファクトリを生成し、バリデータを初期化
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  /**
   * 各テストの実行後に、リソースを解放する。 - ValidatorFactoryをクローズ
   */
  @AfterEach
  void afterEach() {
    factory.close();// リソースを解放してメモリリークを防止
  }

  /**
   * username フィールドに対するバリデーションの正常系テスト。
   *
   * <p>このテストでは、バリデーションルールに準拠した入力データが適切に検証され、
   * バリデーション違反が発生しないことを確認します。</p>
   *
   * <p>テストデータの例:</p>
   * <ul>
   *   <li><strong>最小文字数:</strong> 3文字 ("aaa")</li>
   *   <li><strong>最大文字数:</strong> 32文字 ("aaaaaaaaaabbbbbbbbbbccccccccccdd")</li>
   *   <li><strong>使用可能な文字種:</strong></li>
   *   <ul>
   *     <li>英小文字のみ ("abcdefghqiklmnopqrstuvwxyz")</li>
   *     <li>数字のみ ("0123456789")</li>
   *     <li>特殊文字を含む ("user-._name")</li>
   *   </ul>
   * </ul>
   *
   * <p>バリデーション条件:</p>
   * <ul>
   *   <li>username の長さは 3〜32 文字であること。</li>
   *   <li>使用可能な文字は英小文字、数字、一部の特殊文字（ハイフン、アンダースコア、ドット）のみ。</li>
   *   <li>先頭および末尾に特殊文字は使用できない（別途失敗系でテスト済み）。</li>
   * </ul>
   *
   * @param username テスト対象の username 値。 パラメータ化されたテストデータにより繰り返し実行されます。
   */
  @ParameterizedTest
  @DisplayName("username のバリデーション：成功")
  @ValueSource(strings = {
      // 文字数は 3~32 文字
      "aaa", // 最小文字数
      "aaaaaaaaaabbbbbbbbbbccccccccccdd", // 最大文字数
      // 文字種
      "abcdefghqiklmnopqrstuvwxyz", // 英小文字のみ
      "0123456789", // 数字のみ
      "user-._name", // 許容される特殊文字を含む
  })
  void username_success(String username) {
    // ## Arrange ##
    // テスト対象の UserForm インスタンスを作成（正しい入力値を設定）
    var cut = new UserForm(username, "password00");

    // ## Act ##
    // バリデーションを実行し、違反内容を取得
    var violations = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反がないことを確認
    assertThat(violations).isEmpty();
  }

  /**
   * username のバリデーションテスト: 失敗ケース
   *
   * <p>このテストでは、username に対するバリデーションが正しく失敗することを確認します。</p>
   * <p>テストデータには以下を含みます:</p>
   * <ul>
   *   <li>null: username が未設定の場合</li>
   *   <li>空文字: username が空文字の場合</li>
   *   <li>短すぎる文字列: username が 3 文字未満の場合</li>
   *   <li>長すぎる文字列: username が 33 文字以上の場合</li>
   *   <li>特殊文字が含まれる場合: username に記号 (例: `!`) が含まれる</li>
   *   <li>大文字が含まれる場合: username に大文字 (例: `Username`) が含まれる</li>
   *   <li>先頭や末尾に不正な文字が含まれる場合: ハイフン（-）、アンダースコア（_）、ドット（.）</li>
   * </ul>
   *
   * <p>具体的には以下のケースをチェックします:</p>
   * <pre>{@code
   * null, "", "a", "aa", "aaaaaaaaaabbbbbbbbbbccccccccccddx", "username!",
   * "Username", ".username", "-username", "_username",
   * "username.", "username-", "username_"
   * }</pre>
   *
   * @param username テスト対象の username 値
   */
  @ParameterizedTest
  @DisplayName("username のバリデーション：失敗")
  @NullSource // null
  @ValueSource(strings = {
      // 文字数は 3~32 文字
      "", // 空文字
      "a", // 1文字
      "aa", // 2文字
      "aaaaaaaaaabbbbbbbbbbccccccccccddx", // 33文字
      "username!", // 特殊文字が含まれる
      "Username", // 大文字が含まれる
      // 先頭末尾にはハイフン・アンダースコア・ドットを使用できない
      ".username",
      "-username",
      "_username",
      "username.",
      "username-",
      "username_",
  })
  void username_failure(String username) {
    // ## Arrange ##
    // テスト対象の UserForm インスタンスを作成
    var cut = new UserForm(username, "password00");

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

  /**
   * password フィールドに対するバリデーションの正常系テスト。
   *
   * <p>このテストでは、バリデーションルールに準拠したパスワードが
   * 適切に検証され、バリデーション違反が発生しないことを確認します。</p>
   *
   * <p>テスト対象のパスワードは以下の通り:</p>
   * <ul>
   *   <li>10文字の数字のみのパスワード</li>
   *   <li>255文字の最大長を持つパスワード</li>
   *   <li>英字、数字、記号を含む複雑なパスワード</li>
   * </ul>
   *
   * @param password テスト対象のパスワード
   */
  @ParameterizedTest
  @DisplayName("password のバリデーション：成功")
  @ValueSource(strings = {
      // 10 characters
      "1234567890",
      // 255 characters
      "12345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890"
          + "12345",
      // alphabet + symbols
      "~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:ZXCVBNM<>?",
  })
  void password_success(String password) {
    // ## Arrange ##
    // テスト対象の UserForm インスタンスを作成
    var cut = new UserForm("username00", password);

    // ## Act ##
    // バリデーションを実行し、違反内容を取得
    var actual = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反がないことを確認
    assertThat(actual).isEmpty();
  }
}
