package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.DateTimeService;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 記事一覧を取得する REST コントローラーのテストクラス。
 *
 * <p>このクラスでは、{@link MockMvc} を使用して REST API のエンドポイント
 * {@code GET /articles} をテストします。記事一覧取得時の正常系動作を確認します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerListArticlesTest {

  /**
   * MockMvc オブジェクト。
   * <p>HTTP リクエストをモックしてコントローラーをテストするために使用します。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * ユーザー管理サービス。
   * <p>記事作成時にユーザー情報を提供するために使用します。</p>
   */
  @Autowired
  private UserService userService;

  /**
   * 記事管理サービス。
   * <p>テストデータとして記事を登録する際に使用します。</p>
   */
  @Autowired
  private ArticleService articleService;

  /**
   * 日付時刻管理サービスのモック。
   * <p>テスト用に固定された日時を提供します。</p>
   */
  @MockBean
  private DateTimeService mockDateTimeService;

  /**
   * MockMvc とサービスの初期化確認。
   * <p>依存関係が正しくセットアップされていることを確認します。</p>
   */
  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
    assertThat(articleService).isNotNull();
  }

  /**
   * 記事一覧取得エンドポイントの正常系テスト。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>エンドポイント {@code GET /articles} が正常に動作すること。</li>
   *   <li>正しいレスポンスヘッダー（ステータスコードおよび Content-Type）が返されること。</li>
   *   <li>レスポンスボディに期待される記事データが含まれること。</li>
   *   <li>レスポンスに余計なデータ（例: パスワードフィールド）が含まれないこと。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("GET /articles: 記事の一覧を取得できる")
  void listArticles_success() throws Exception {
    // ## Arrange ##
    // 日付を固定：この値がDBに登録される
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2022, 1, 1, 10, 10))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 20, 20));

    var user1 = userService.register("test_username1", "test_password1");
    var expectedArticle1 = articleService.create(user1.getId(), "test_title1", "test_body1");
    var expectedArticle2 = articleService.create(user1.getId(), "test_title2", "test_body2");

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles")
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // response header
    actual
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    // response body: item[0]
    actual
        .andExpect(jsonPath("$.items[0].id").value(expectedArticle2.getId()))
        .andExpect(jsonPath("$.items[0].title").value(expectedArticle2.getTitle()))
        .andExpect(jsonPath("$.items[0].body").doesNotExist())
        .andExpect(jsonPath("$.items[0].author.id").value(expectedArticle2.getAuthor().getId()))
        .andExpect(jsonPath("$.items[0].author.username").value(
            expectedArticle2.getAuthor().getUsername()))
        .andExpect(jsonPath("$.items[0].author.password").doesNotExist())
        .andExpect(
            jsonPath("$.items[0].createdAt").value(expectedArticle2.getCreatedAt().toString()))
        .andExpect(
            jsonPath("$.items[0].updatedAt").value(expectedArticle2.getUpdatedAt().toString()))
    ;

    // response body: item[1]
    actual
        .andExpect(jsonPath("$.items[1].id").value(expectedArticle1.getId()))
        .andExpect(jsonPath("$.items[1].title").value(expectedArticle1.getTitle()))
        .andExpect(jsonPath("$.items[1].body").doesNotExist())
        .andExpect(jsonPath("$.items[1].author.id").value(expectedArticle1.getAuthor().getId()))
        .andExpect(jsonPath("$.items[1].author.username").value(
            expectedArticle1.getAuthor().getUsername()))
        .andExpect(jsonPath("$.items[1].author.password").doesNotExist())
        .andExpect(
            jsonPath("$.items[1].createdAt").value(expectedArticle1.getCreatedAt().toString()))
        .andExpect(
            jsonPath("$.items[1].updatedAt").value(expectedArticle1.getUpdatedAt().toString()))
    ;
  }
}