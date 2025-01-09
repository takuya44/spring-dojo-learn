package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

/**
 * {@link ArticleService} のテストクラス。
 *
 * <p>このクラスでは、記事作成サービスの動作を検証します。具体的には、articlesテーブルへのレコード挿入が
 * 正常に行われるかを確認します。</p>
 *
 * <p>使用するアノテーション:</p>
 * <ul>
 *   <li>{@link MybatisDefaultDatasourceTest}: MyBatisとデフォルトデータソース設定を使用したテスト環境を構築。</li>
 *   <li>{@link Import}: {@link ArticleService} をテスト環境にインポートしてテスト対象に指定。</li>
 * </ul>
 */
@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceTest {

  @Autowired
  private ArticleService cut;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * {@link ArticleService} と {@link UserRepository} が正しく初期化されていることを確認します。
   * <p>このテストはテスト環境のセットアップ確認用です。</p>
   */
  @Test
  void setup() {
    // テスト対象のインスタンスがnullでないことを検証
    assertThat(cut).isNotNull();
    assertThat(userRepository).isNotNull();
  }

  /**
   * 記事作成処理の動作を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>articlesテーブルに新しいレコードが挿入されること。</li>
   *   <li>挿入された記事データが正しい値を持つこと。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>テスト用のユーザーを準備してデータベースに挿入。</li>
   *   <li>作成する記事のタイトルと本文を準備。</li>
   *   <li>記事作成サービスを呼び出し、新しい記事を作成。</li>
   *   <li>作成された記事が期待通りのデータを持っていることを検証。</li>
   * </ol>
   */
  @Test
  @DisplayName("create: articles テーブルにレコードが insert される")
  void create_success() {
    // ## Arrange ##
    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user1"); // ユーザー名を設定
    expectedUser.setPassword("test_password1"); // パスワードを設定
    expectedUser.setEnabled(true); // 有効なユーザーであることを示す
    // ユーザーをデータベースに挿入
    userRepository.insert(expectedUser);

    // 日付を固定：この値がDBに登録される
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // 記事のタイトルと本文を準備
    var expectedTitle = "test_article_title";
    var expectedBody = "test_article_body";

    // ## Act ##
    // 記事作成サービスを呼び出し、新しい記事を作成
    var actual = cut.create(expectedUser.getId(), expectedTitle, expectedBody);

    // ## Assert ##
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getTitle()).isEqualTo(expectedTitle);
    assertThat(actual.getBody()).isEqualTo(expectedBody);
    assertThat(actual.getAuthor()).satisfies(user -> {
      assertThat(user.getId()).isEqualTo(expectedUser.getId());
      assertThat(user.getUsername()).isEqualTo(expectedUser.getUsername());
      assertThat(user.getPassword()).isNull(); // パスワードはnullであるべき（セキュリティ対策）
      assertThat(user.isEnabled()).isEqualTo(expectedUser.isEnabled());
    });
    assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
    assertThat(actual.getUpdatedAt()).isEqualTo(actual.getCreatedAt()); // 作成日時と更新日時が一致していることを確認
  }

}