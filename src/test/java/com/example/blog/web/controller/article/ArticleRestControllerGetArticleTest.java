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
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArticleRestController の GET /articles/{articleId} エンドポイントに関するテストクラス。
 *
 * <p>このクラスでは、記事の詳細取得機能が正しく動作するかを検証します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerGetArticleTest {

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
   * GET /articles/{articleId}: 正常系のテスト。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>エンドポイントが正しいステータスコード（200 OK）を返すこと。</li>
   *   <li>レスポンスヘッダーに正しい Content-Type が含まれること。</li>
   *   <li>レスポンスボディに期待される記事の詳細が含まれること。</li>
   *   <li>余計なデータ（例: パスワード）がレスポンスに含まれないこと。</li>
   * </ul>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("GET /articles/{articleId}: 記事の詳細を取得できる")
  @Sql(statements = {"""
      DELETE FROM articles;
      """
  })
  void getArticle_success() throws Exception {
    // ## Arrange ##
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // 日付を固定：この値がDBに登録される
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20));

    var expectedUser1 = userService.register("test_username1", "test_password1");
    var expectedArticle1 = articleService.create(expectedUser1.getId(), "test_title1",
        "test_body1");

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles/{articleId}", expectedArticle1.getId())
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // response header
    actual
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    // response body: item
    actual
        .andExpect(jsonPath("$.id").value(expectedArticle1.getId()))
        .andExpect(jsonPath("$.title").value(expectedArticle1.getTitle()))
        .andExpect(jsonPath("$.body").value(expectedArticle1.getBody()))
        .andExpect(jsonPath("$.author.id").value(expectedArticle1.getAuthor().getId()))
        .andExpect(jsonPath("$.author.username").value(
            expectedArticle1.getAuthor().getUsername()))
        .andExpect(jsonPath("$.author.password").doesNotExist())
        .andExpect(
            jsonPath("$.createdAt").value(
                expectedArticle1.getCreatedAt().format(formatter)))
        .andExpect(
            jsonPath("$.updatedAt").value(
                expectedArticle1.getUpdatedAt().format(formatter)))
    ;

  }

  /**
   * GET /articles/{articleId}: 指定された記事IDが存在しない場合の動作をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>存在しない記事IDを指定した場合、エンドポイントが 404 Not Found を返すこと。</li>
   *   <li>レスポンスヘッダーに正しい Content-Type が含まれること。</li>
   *   <li>レスポンスボディに適切なエラーメッセージが含まれること。</li>
   *   <li>レスポンスにリクエストパスがインスタンス情報として含まれること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>データベースに 3 件の記事データが存在する。</li>
   *   <li>指定する記事ID（{@code 0}）は有効な記事IDではない。</li>
   * </ul>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("GET /articles/{articleId}: 指定された記事のIDが存在しないとき 404 を返す")
  void getArticle_404() throws Exception {
    // ## Arrange ## 前提：DBに３件データある
    var invalidArticleId = 0;

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles/{articleId}", invalidArticleId)
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    actual
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("NotFound"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("リソースが見つかりません"))
        .andExpect(jsonPath("$.instance").value("/articles/" + invalidArticleId))
    ;

  }
}