package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleCommentService;
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

  @MockBean
  private ArticleCommentService articleCommentService;

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

  /**
   * PUT /articles/{articleId}: 500 InternalServerError で stacktrace が露出しないことを検証するテスト。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>記事更新時に内部エラーが発生した場合、サーバーが 500 Internal Server Error を返すこと。</li>
   *   <li>レスポンスの Content-Type が RFC 7807 に準拠した <code>application/problem+json</code> であること。</li>
   *   <li>レスポンスボディにスタックトレースや内部エラーの詳細情報が含まれず、必要最小限のエラー情報のみが返されること。</li>
   *   <li>具体的には、<code>detail</code> が空であり、レスポンス全体が4つのキー（title, status, detail, instance）のみで構成されていること。</li>
   *   <li>レスポンスの <code>instance</code> プロパティにリクエストURIが正しく設定されていること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>テスト用の articleId、userId、title、body を定義し、更新処理が RuntimeException をスローするようにスタブする。</li>
   *   <li>JSON 形式のリクエストボディを作成する。</li>
   *   <li>認証情報と CSRF トークンを含む PUT リクエストを送信する。</li>
   *   <li>受信したレスポンスに対し、HTTP ステータス、Content-Type、各フィールドの値を検証する。</li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("PUT /articles/{articleId}: 500 InternalServerError で stacktrace が露出しない")
  void putArticle_500() throws Exception {
    // ## Arrange ##
    // テスト用のパラメータを定義: mockBeanなので実際の動きができないなで必須の入力値のみ用意
    var articleId = 9999L;    // テスト対象の記事ID
    var userId = 999L;        // テスト用のユーザーID
    var title = "test_title"; // 更新に使用するタイトル
    var body = "test_body";   // 更新に使用する本文

    // articleService.update() を呼び出すと RuntimeException をスローするようにスタブ設定
    when(articleService.update(articleId, userId, title, body))
        .thenThrow(RuntimeException.class);

    // テスト用のリクエストボディを JSON 形式で作成
    var bodyJson = """
        {
          "title": "%s",
          "body": "%s"
        }
        """.formatted(title, body);

    // ## Act ##
    // 認証情報と CSRF トークンを含む PUT リクエストを送信
    var actual = mockMvc.perform(
        put("/articles/{articleId}", articleId)
            .with(csrf())
            .with(user(new LoggedInUser(userId, "test_username", "", true)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bodyJson)
    );

    // ## Assert ##
    // レスポンスが 500 Internal Server Error であること、およびレスポンスボディの内容を検証する
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }

  /**
   * DELETE /articles: 500 InternalServerError で stacktrace が露出しないことを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>
   *     サービス層で RuntimeException が発生した場合、コントローラーが 500 Internal Server Error を返すこと。
   *   </li>
   *   <li>
   *     レスポンスの Content-Type が RFC7807 に準拠した <code>application/problem+json</code> であること。
   *   </li>
   *   <li>
   *     レスポンスボディ内に、スタックトレースなど内部情報が露出していない（detail が空で、返されるキーが4つのみ）こと。
   *   </li>
   *   <li>
   *     レスポンスの instance プロパティがリクエスト URI を正しく示していること。
   *   </li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>テスト用のパラメータ（articleId と userId）を定義します。</li>
   *       <li>
   *         mockBean の articleService の delete メソッド呼び出し時に RuntimeException をスローするように設定し、
   *         内部エラー発生時の挙動をシミュレートします。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>
   *         CSRF トークンと認証情報（LoggedInUser）を付与した DELETE リクエストを送信します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>
   *         サーバーが 500 Internal Server Error を返し、レスポンスの Content-Type が <code>application/problem+json</code> であることを検証します。
   *       </li>
   *       <li>
   *         レスポンスボディのエラー情報において、title が "Internal Server Error"、status が 500、detail が空、
   *         instance が正しいリクエストURI になっており、返されるキーが4つのみであることを確認します。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("DELETE /articles: 500 InternalServerError で stacktrace が露出しない")
  void deleteArticle_500() throws Exception {
    // ## Arrange ##
    // テスト用のパラメータを定義: mockBeanなので実際の動きができないなで必須の入力値のみ用意
    var articleId = 9999L;    // テスト対象の記事ID
    var userId = 999L;        // テスト用のユーザーID

    // articleService.delete が呼ばれた際、RuntimeException をスローするように設定します。
    // これにより、内部エラーが発生した場合のレスポンスをシミュレートします。
    doThrow(RuntimeException.class)
        .when(articleService).delete(userId, articleId);

    // ## Act ##
    var actual = mockMvc.perform(
        delete("/articles/{articleId}", articleId)
            .with(csrf())
            .with(user(new LoggedInUser(userId, "test_username", "", true)))
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // レスポンスが 500 Internal Server Error であり、レスポンスボディの内容が期待通り（内部情報が漏れていない）であることを検証します。
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }

  @Test
  @DisplayName("POST /articles/{articleId}/comments: 500 InternalServerError で stacktrace が露出しない")
  void createArticleComment_500() throws Exception {
    // ## Arrange ##
    var userId = 999L;
    var articleId = 9999L;
    var body = "test_body";

    doThrow(RuntimeException.class).when(articleCommentService).create(userId, articleId, body);

    // テスト用のリクエストボディを JSON 形式で作成
    var bodyJson = """
        {
          "body": "%s"
        }
        """.formatted(body);

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles/{articleId}/comments", articleId)
            .with(csrf())
            .with(user(new LoggedInUser(userId, "test_username", "", true)))
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    actual
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").isEmpty())
        .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(articleId)))
        .andExpect(jsonPath("$", aMapWithSize(4)));
  }
}
