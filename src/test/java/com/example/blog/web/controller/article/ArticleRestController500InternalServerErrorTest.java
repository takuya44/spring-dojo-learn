package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
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
 * ArticleRestController の 500 Internal Server Error に関するテストクラス。
 *
 * <p>このクラスでは、コントローラーが予期せぬ例外を適切に処理し、スタックトレースなどの内部情報を露出しないことを検証します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ArticleRestController500InternalServerErrorTest {

  /**
   * MockMvc オブジェクト。
   * <p>HTTP リクエストをモックしてコントローラーをテストするために使用します。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * モック化された ArticleService。
   * <p>サービス層の動作をシミュレートしてコントローラーの挙動をテストします。</p>
   */
  @MockBean
  private ArticleService articleService;

  /**
   * MockMvc およびモックサービスが正しく初期化されていることを確認するテスト。
   */
  @Test
  void setUp_success() {
    // ## Arrange ##

    // ## Act ##

    // ## Assert ##
    assertThat(mockMvc).isNotNull();
    assertThat(articleService).isNotNull();
  }

  /**
   * POST /articles: 500 Internal Server Error の処理をテストします。
   *
   * <p>このテストでは、予期しない例外が発生した場合に、エンドポイントがスタックトレースを露出せず、適切なエラーレスポンスを返すことを確認します。</p>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("POST /articles: 500 InternalServerError で stacktrace が露出しない")
  void createArticle_500() throws Exception {
    // ## Arrange ##
    var userId = 999L;
    var title = "test_title";
    var body = "test_body";
    when(articleService.create(userId, title, body)).thenThrow(RuntimeException.class);

    // テスト用のリクエストボディを JSON 形式で作成
    var bodyJson = """
        {
          "title": "%s",
          "body": "%s"
        }
        """.formatted(title, body);

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf())
            .with(user(new LoggedInUser(userId, "test_username", "", true)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles"))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }

  /**
   * GET /articles: 500 Internal Server Error の処理をテストします。
   *
   * <p>このテストでは、予期しない例外が発生した場合に、エンドポイントがスタックトレースを露出せず、適切なエラーレスポンスを返すことを確認します。</p>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("GET /articles: 500 InternalServerError で stacktrace が露出しない")
  void listArticles_500() throws Exception {
    // ## Arrange ##
    when(articleService.findAll()).thenThrow(RuntimeException.class);

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles")
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles"))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }

  /**
   * GET /articles/{articleId}: 予期しないエラーが発生した場合の挙動をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>サービス層で予期しない例外がスローされた場合、エンドポイントが 500 Internal Server Error を返すこと。</li>
   *   <li>レスポンスヘッダーに正しい Content-Type が設定されていること。</li>
   *   <li>レスポンスボディにスタックトレースなどの内部情報が露出していないこと。</li>
   *   <li>エラーレスポンスの形式が適切であること（RFC 7807 に準拠）。</li>
   * </ul>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>モックされたサービスで {@link RuntimeException} をスローするように設定。</li>
   *   <li>エンドポイントを呼び出してレスポンスを取得。</li>
   *   <li>レスポンスのステータスコード、ヘッダー、ボディを検証。</li>
   * </ol>
   *
   * @throws Exception テスト実行中の例外
   */
  @Test
  @DisplayName("GET /articles/{articleId}: 500 InternalServerError で stacktrace が露出しない")
  void getArticle_500() throws Exception {
    // ## Arrange ##
    var articleId = 999;
    when(articleService.findById(articleId)).thenThrow(RuntimeException.class);

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles/{articleId}", articleId)
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }
}
