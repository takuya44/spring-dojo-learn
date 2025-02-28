package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

/**
 * {@link ArticleCommentService} のユニットテストクラス。
 *
 * <p>このクラスでは、記事のコメント機能が正しく動作することを検証する。</p>
 */
@MybatisDefaultDatasourceTest
@Import({
    ArticleCommentService.class,
    ArticleService.class,
    UserService.class,
    PasswordEncoderConfig.class
})
class ArticleCommentServiceTest {

  /**
   * モック化した日時サービス
   */
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * ユーザー管理サービス
   */
  @Autowired
  private UserService userService;

  /**
   * 記事管理サービス
   */
  @Autowired
  private ArticleService articleService;

  /**
   * テスト対象のコメント管理サービス
   */
  @Autowired
  private ArticleCommentService cut;

  /**
   * 依存するサービスのインスタンスが正しく注入されていることを検証するセットアップメソッド。
   */
  @Test
  void setup() {
    // テスト対象のインスタンスがnullでないことを検証
    assertThat(cut).isNotNull();
    assertThat(articleService).isNotNull();
    assertThat(userService).isNotNull();
  }

  /**
   * コメント作成処理の正常系テスト。
   *
   * <p>以下の条件を満たすことを確認する:</p>
   * <ul>
   *   <li>コメントがデータベースに正常に登録されること</li>
   *   <li>作成されたコメントの内容が正しいこと</li>
   *   <li>コメントの作成者情報が適切に設定されていること</li>
   *   <li>作成日時が正しく設定されること</li>
   * </ul>
   */
  @Test
  @DisplayName("create: articles テーブルにレコードが insert される")
  void create_success() {
    // ## Arrange ##
    // 固定された日時をモックで設定
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // テスト用のユーザーと記事を作成
    var articleAuthor = userService.register("test_username1", "test_password");
    var commentAuthor = userService.register("test_username2", "test_password");
    var article = articleService.create(articleAuthor.getId(), "test_title", "test_body");
    var expectedComment = "コメントしました";

    // ## Act ##
    // コメントを作成
    var actual = cut.create(commentAuthor.getId(), article.getId(), expectedComment);

    // ## Assert ##
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getBody()).isEqualTo(expectedComment);

    // 作成者の情報が適切に設定されていること（パスワードを除外）
    assertThat(actual.getAuthor())
        .usingRecursiveComparison()
        .ignoringFields("password")
        .isEqualTo(commentAuthor);
    
    assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
  }
}