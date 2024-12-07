package com.example.blog.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
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
 * UserRestController のエラー処理に関するテストクラス。
 *
 * <p>このテストクラスでは、サービス層で例外が発生した場合に
 * /users エンドポイントが適切にエラーレスポンスを返すことを確認します。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRestController500InternalServerErrorTest {

  /**
   * MockMvc オブジェクト。
   * <p>HTTP リクエストをモックしてコントローラーをテストするために使用します。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * UserService をモック化した Bean。
   * <p>サービス層の振る舞いをカスタマイズしてテストを実行します。</p>
   */
  @MockBean
  private UserService userService;

  /**
   * MockMvc が正しく初期化されていることを確認する基本テスト。
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();
  }

  /**
   * POST /users: サーバー内部エラー発生時のレスポンスをテスト。
   *
   * <p>このテストでは、サービス層で例外がスローされた場合に、
   * スタックトレースがレスポンスに露出せず、適切に HTTP 500 ステータスが返されることを確認します。</p>
   */
  @Test
  @DisplayName("POST /users: 500 InternalServerError のとき、スタックトレースがレスポンスに露出しない")
  void method_success() throws Exception {
    // ## Arrange ##
    // テスト用のユーザー名とパスワードを定義
    var username = "username123";
    var password = "password123";

    // UserService の register メソッドが例外をスローするようモックを設定
    when(userService.register(username, password)).thenThrow(
        new RuntimeException("サーバーエラー"));

    // テスト用のリクエストボディを JSON 形式で作成
    var newUserJson = """
        {
          "username": "%s",
          "password": "%s"
        }
        """.formatted(username, password);

    // ## Act ##
    // MockMvc を使用して POST リクエストを送信
    var actual = mockMvc.perform(post("/users")
        // CSRF トークンを含める（403 エラー防止）
        .contentType(MediaType.APPLICATION_JSON) // リクエストの Content-Type を設定
        .content(newUserJson)); // JSON データをリクエストボディとして送信

    // ## Assert ##
    // サーバーエラーのステータスコードを確認
    actual
        .andExpect(status().isInternalServerError());
  }
}
