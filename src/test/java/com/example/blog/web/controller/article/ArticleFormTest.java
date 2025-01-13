package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.model.ArticleForm;
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
 * ArticleForm クラスのバリデーションロジックをテストするクラス。
 *
 * <p>このテストでは、{@link jakarta.validation.Validator} を使用して、
 * 入力データが {@link ArticleForm} のバリデーション要件を満たしているかを確認します。</p>
 */
class ArticleFormTest {

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
   * title フィールドに対するバリデーションの正常系テスト。
   *
   * <p>このテストでは、バリデーションルールに準拠した入力データが適切に検証され、
   * バリデーション違反が発生しないことを確認します。</p>
   *
   * <p>テストデータ:</p>
   * <ul>
   *   <li>最小文字数: 1文字</li>
   *   <li>最大文字数: 255文字</li>
   * </ul>
   *
   * @param title テスト対象のタイトル値
   */
  @ParameterizedTest
  @DisplayName("title のバリデーション：成功")
  @ValueSource(strings = {
      // 1文字
      "あ",

      // 255文字
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
  void title_success(String title) {
    // ## Arrange ##
    var cut = new ArticleForm(title, "");

    // ## Act ##
    var actual = validator.validate(cut);

    // ## Assert ##
    assertThat(actual).isEmpty();
  }

  /**
   * title フィールドに対するバリデーションの失敗ケースをテスト。
   *
   * <p>このテストでは、バリデーションルールに違反した入力データが
   * 適切に検証され、バリデーション違反が発生することを確認します。</p>
   *
   * <p>テストデータ:</p>
   * <ul>
   *   <li>null</li>
   *   <li>空文字</li>
   *   <li>256文字（最大長さ超過）</li>
   * </ul>
   *
   * @param title テスト対象のタイトル値
   */
  @ParameterizedTest
  @DisplayName("title のバリデーション：失敗")
  @NullSource
  @ValueSource(strings = {
      "",

      // 256文字
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
          + "あいうえおか"
  })
  void title_failure(String title) {
    // ## Arrange ##
    var cut = new ArticleForm(title, "");

    // ## Act ##
    var actual = validator.validate(cut);

    // ## Assert ##
    assertThat(actual).isNotEmpty();
    assertThat(actual)
        .anyMatch(violation -> violation.getPropertyPath().toString().equals("title"));
  }
}
