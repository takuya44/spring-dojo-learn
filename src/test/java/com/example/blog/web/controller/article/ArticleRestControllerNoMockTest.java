package com.example.blog.web.controller.article;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ArticleRestController} の単体テストクラス。
 *
 * <p>このテストクラスでは、Spring MVCのMockMvcを使用して、コントローラーレイヤのエンドポイントをテストします。
 * 実際のデータベースアクセスを行い、Mockを使用せずに記事の取得エンドポイントをテストします。</p>
 *
 * <p>テストは {@link SpringBootTest} と {@link AutoConfigureMockMvc} アノテーションを使用して構成され、
 * MockMvcを通じてリクエストを模擬し、エンドポイントの応答を検証します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerNoMockTest {


  /**
   * Spring MVCのテストを行うための {@link MockMvc} オブジェクト。
   * <p>MockMvcを使用して、コントローラーの統合テストを行います。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * MockMvcが正しく初期化されていることを検証するテスト。
   * <p>MockMvcオブジェクトがnullでないことを確認し、テスト環境が正常にセットアップされていることを確認します。</p>
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();
  }

  /**
   * GET /articles/{id}: 指定されたIDの記事が存在するとき、200 OK で記事データを返すテスト。
   *
   * <p>このテストは、指定されたIDの記事がデータベースに存在する場合に、ステータスコード200 OKが返され、
   * 正しい記事データがJSON形式でレスポンスされることを検証します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("GET /articles/{id}: 指定されたIDの記事が存在するとき、200 OK で記事データを返す")
  @Sql(statements = {
      """
          INSERT INTO articles (id, title, body, created_at, updated_at)
          VALUE (999, 'title_999', 'body_999', '2022-01-01 10:00:00', '2022-02-01 11:00:00');
          """
  })
  public void getArticle_return200() throws Exception {
    // ## Arrange ##
    // データベースにテストデータを挿入

    // ## Act ##
    // 指定されたIDの記事を取得するリクエストを送信
    var actual = mockMvc.perform(get("/articles/{id}", 999));

    // ## Assert ##
    // レスポンスのステータスコードが200 OKであり、期待された記事データが返されることを検証
    actual
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(999))
        .andExpect(jsonPath("$.title").value("title_999"))
        .andExpect(jsonPath("$.content").value("body_999"))
        .andExpect(jsonPath("$.createdAt").value("2022-01-01T10:00:00"))
        .andExpect(jsonPath("$.updatedAt").value("2022-02-01T11:00:00"));
  }
}