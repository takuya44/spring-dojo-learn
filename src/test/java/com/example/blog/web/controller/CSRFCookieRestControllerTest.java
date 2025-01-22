package com.example.blog.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * テストクラス {@code CSRFCookieRestControllerTest} は、 {@link CSRFCookieRestController} のエンドポイント
 * <p>
 * `/csrf-cookie` に対するテストを行います。 このテストでは、GET リクエストが正しく処理され、 204 No Content のステータスコードと XSRF
 * トークンが、Set-Cookie ヘッダに含まれているかを検証します。
 *
 * <p>このクラスは {@link SpringBootTest} および {@link AutoConfigureMockMvc} アノテーションを使用して、
 * MockMvc を自動構成し、実際の HTTP リクエストのシミュレーションを行います。
 * </p>
 *
 * <p>Spring Boot コンテキストをロードし、エンドポイントの統合テストを実行することで、
 * アプリケーションの動作が期待通りかどうかを確認します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class CSRFCookieRestControllerTest {

  @Nested
  class Return204Test {

    /**
     * HTTP リクエストのモックを作成するための {@link MockMvc} インスタンス。
     * このオブジェクトを使用して、コントローラーに対するリクエストをシミュレートし、レスポンスを検証します。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * テストメソッド {@code return204} は、 `/csrf-cookie` エンドポイントに対して GET リクエストを送信し、 レスポンスに 204 No
     * Contentのステータスコードと XSRF トークンが含まれていることを検証します。
     *
     * <p>このテストでは、以下を検証します:</p>
     * <ul>
     *   <li>ステータスコードが 204 No Content であること</li>
     *   <li>レスポンスヘッダに Set-Cookie が含まれ、その中に XSRF-TOKEN が設定されていること</li>
     * </ul>
     *
     * @throws Exception テスト中にリクエストやレスポンスでエラーが発生した場合
     */
    @Test
    @DisplayName("/csrf-cookie: GET リクエストを送ると、204 No Content を返し、Set-CookieヘッダにXSRF-TOKENが含まれている")
    void return204() throws Exception {
      // ## Arrange ##
      // 特別な準備は不要です

      // ## Act ##
      // GETリクエストを /csrf-cookie に送信して結果を取得
      var result = mockMvc.perform(get("/csrf-cookie"));

      // ## Assert ##
      // ステータスコードが 204 No Content であることを確認
      // "Set-Cookie" ヘッダーに "XSRF-TOKEN=" が含まれていることを確認
      result
          .andExpect(status().isNoContent())
          .andExpect(header().string("Set-Cookie", containsString("XSRF-TOKEN=")));
    }
  }

  @Nested
  class Return500Test {

    /**
     * HTTP リクエストのモックを作成するための {@link MockMvc} インスタンス。
     * このオブジェクトを使用して、コントローラーに対するリクエストをシミュレートし、レスポンスを検証します。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * モックされた {@link CSRFCookieRestController} のインスタンス。 このモックを使用して、意図的に例外をスローさせ、500 Internal Server
     * Error のレスポンスをテストします。
     */
    @MockBean
    private CSRFCookieRestController mockCsrfCookieRestController;


    /**
     * テストメソッド {@code return500} は、 `/csrf-cookie` エンドポイントに対して GET リクエストを送信し、 処理中に例外が発生した場合、500
     * Internal Server Error のステータスコードが返されることを検証します。
     *
     * <p>このテストでは、以下を検証します:</p>
     * <ul>
     *   <li>ステータスコードが 500 Internal Server Error であること</li>
     *   <li>レスポンスのJSONフィールドにエラーメッセージが含まれていること</li>
     * </ul>
     *
     * @throws Exception テスト中にリクエストやレスポンスでエラーが発生した場合
     */
    @Test
    @DisplayName("/csrf-cookie: GET リクエストの処理で Exception が発生すると、 500 Internal Server Error が返る")
    void return500() throws Exception {
      // ## Arrange ##
      // mockCsrfCookieRestController が例外をスローするように設定
      doThrow(new RuntimeException("exception")).when(mockCsrfCookieRestController).getCsrfCookie();

      // ## Act ##
      // GETリクエストを /csrf-cookie に送信して結果を取得
      var result = mockMvc.perform(get("/csrf-cookie"));

      // ## Assert ##
      // ステータスコードが 500 Internal Server Error であることを確認
      result
          .andExpect(status().isInternalServerError())
          .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.title").value("Internal Server Error"))
          .andExpect(jsonPath("$.status").value(500))
          .andExpect(jsonPath("$.detail").isEmpty())
          .andExpect(jsonPath("$.instance").value("/csrf-cookie"));
      ;
    }
  }
}