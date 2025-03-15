package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.model.ArticleCommentForm;
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
 * ArticleCommentFormTestは、ArticleCommentFormクラスのバリデーションルールが正しく動作するかを検証するテストクラスです。
 *
 * <p>
 * 本テストでは、ArticleCommentFormの「body」フィールドに対して、正常な入力と異常な入力の両方でバリデーションが期待通りに動作するかをパラメータ化テストで検証します。
 * </p>
 */
class ArticleCommentFormTest {

  private ValidatorFactory factory; // バリデーションの設定を生成するファクトリ
  private Validator validator; // 実際にバリデーションを行うインスタンス


  /**
   * 各テストの実行前に、バリデーションのセットアップを行う。
   * <ul>
   *   <li>デフォルトの ValidatorFactory を作成</li>
   *   <li>Validator インスタンスを取得</li>
   * </ul>
   */
  @BeforeEach
  void beforeEach() {
    // テスト前にバリデーションファクトリを生成し、バリデータを初期化
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  /**
   * 各テストの実行後に、リソースを解放する。
   * <ul>
   *   <li>ValidatorFactory をクローズしてリソースを解放</li>
   * </ul>
   */
  @AfterEach
  void afterEach() {
    factory.close(); // リソースを解放してメモリリークを防止
  }

  /**
   * bodyフィールドのバリデーションが正常に成功するケースを検証するテストです。
   *
   * <p>
   * テスト対象であるArticleCommentFormに、1文字の入力や長い文字列など有効な値を設定し、 バリデーションエラーが発生しないことを確認します。
   * </p>
   *
   * @param body テスト対象のbodyフィールドに設定する値
   */
  @ParameterizedTest
  @DisplayName("body のバリデーション：成功")
  @ValueSource(strings = {
      // 1文字
      "あ",

      // 長い文字列
      "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
          + "あいうえお"
  })
  void body_success(String body) {
    // ## Arrange ##
    // テスト対象のArticleCommentFormインスタンスを生成（有効なbody値を使用）
    var cut = new ArticleCommentForm(body);

    // ## Act ##
    // 生成したインスタンスに対してバリデーションを実行し、違反がないことを確認する
    var actual = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反が存在しないことを検証（有効な入力の場合、エラーは発生しないはず）
    assertThat(actual).isEmpty();
  }

  /**
   * bodyフィールドのバリデーションが失敗するケースを検証するテストです。
   *
   * <p>
   * テスト対象であるArticleCommentFormに対して、nullや空文字といった無効な値を設定し、 バリデーションエラーが発生することを確認します。
   * </p>
   *
   * @param body テスト対象のbodyフィールドに設定する無効な値（nullまたは空文字）
   */
  @ParameterizedTest
  @DisplayName("body のバリデーション：失敗")
  @NullSource
  @ValueSource(strings = {
      ""
  })
  void body_failure(String body) {
    // ## Arrange ##
    // テスト対象のArticleCommentFormインスタンスを生成（無効なbody値を使用）
    var cut = new ArticleCommentForm(body);

    // ## Act ##
    // 生成したインスタンスに対してバリデーションを実行し、違反が発生することを確認する
    var actual = validator.validate(cut);

    // ## Assert ##
    // バリデーション違反が発生していることを検証
    assertThat(actual).isNotEmpty();
    // さらに、違反リストの中に"body"フィールドに関連するエラーが含まれているかを確認
    assertThat(actual)
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("body"));
  }
}
