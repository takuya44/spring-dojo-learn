package com.example.blog.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link UserRestController} の統合テストクラス。
 *
 * <p>このテストクラスでは、Spring MVC の {@link MockMvc} を使用してユーザー関連のエンドポイントをテストします。
 * 具体的には、認証済みユーザーや未認証ユーザーがエンドポイントにアクセスした場合の応答を検証します。</p>
 *
 * <p>使用される主要なアノテーション:</p>
 * <ul>
 *   <li>{@link SpringBootTest} - Spring Boot のアプリケーションコンテキスト全体を使用したテストを実行する。</li>
 *   <li>{@link AutoConfigureMockMvc} - {@link MockMvc} オブジェクトを自動設定する。</li>
 *   <li>{@link Transactional} - テストメソッドごとにトランザクションを開始し、終了後にロールバックする。</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRestControllerTest {

  private static final String MOCK_USERNAME = "user1";

  /**
   * Spring MVCのテストを行うための {@link MockMvc} オブジェクト。
   * <p>MockMvcを使用して、コントローラーの統合テストを行います。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * {@link UserService} の依存性を注入。
   *
   * <p>テスト中に必要となるユーザーサービスのメソッドを呼び出すために利用します。
   * たとえば、事前条件としてデータベースにユーザーを登録する処理などで使用されます。</p>
   */
  @Autowired
  private UserService userService;

  /**
   * MockMvcが正しく初期化されていることを検証するテスト。
   * <p>MockMvcオブジェクトがnullでないことを確認し、テスト環境が正常にセットアップされていることを確認します。</p>
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();
  }

  /**
   * /users/me: ログイン済みユーザーがアクセスしたとき、200 OK でユーザー名を返すテスト。
   *
   * <p>このテストでは、@WithMockUser アノテーションを使用して、仮のユーザー名を設定した状態でエンドポイントにアクセスします。
   * レスポンスとして、200 OKが返され、ユーザー名が含まれていることを検証します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("/users/me: ログイン済みユーザーがアクセスすると、200 OK でユーザー名を返す")
  @WithMockUser(username = MOCK_USERNAME)
  public void usersMe_return200() throws Exception {
    // ## Arrange ##

    // ## Act ##
    // GETリクエストを送信してレスポンスを取得
    var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

    // ## Assert ##
    // ステータスコード200 OKが返され、レスポンスの内容にユーザー名が含まれることを検証
    actual
        .andExpect(status().isOk())
        .andExpect(content().bytes(MOCK_USERNAME.getBytes()));

  }

  /**
   * /users/me: 未ログインユーザーがアクセスしたとき、403 Forbidden を返すテスト。
   *
   * <p>このテストでは、認証されていないユーザーが /users/me エンドポイントにアクセスした際に、
   * ステータスコード403 Forbiddenが返されることを確認します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("/users/me: 未ログインユーザーがアクセスすると、403 Forbidden を返す")
  public void usersMe_return403() throws Exception {
    // ## Arrange ##
    // 未ログイン状態でGETリクエストを作成：@WithMockUser(username = MOCK_USERNAME)なし

    // ## Act ##
    var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

    // ## Assert ##
    // ステータスコード403 Forbiddenが返されることを検証
    actual.andExpect(status().isForbidden());

  }

  /**
   * POST /users: ユーザー作成成功時の動作をテストする。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>ステータスコード 201 (Created) が返されること</li>
   *   <li>ユーザー作成に必要なJSON形式のデータをリクエストボディとして送信する</li>
   *   <li>リクエストにはCSRFトークンが含まれている</li>
   * </ul>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("POST /users：ユーザー作成に成功すると、レスポンスの Location ヘッダー、ボディが設定される")
  public void createUser_success() throws Exception {
    // ## Arrange ##
    // テスト用のユーザーデータをJSON形式で作成
    String newUserJson = """
        {
            "username": "username123",
            "password": "password123"
        }
        """;

    // ## Act ##
    // MockMvcを使用してPOSTリクエストを作成し、エンドポイントに送信
    var actual = mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定: 415エラー対策
        .with(csrf()) // CSRFトークンを含める（セキュリティ設定で必須）:403エラー対策
        .content(newUserJson)); // リクエストボディとしてユーザーデータを送信

    // ## Assert ##
    // ステータスコードが201 Createdであることを確認
    actual
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", matchesPattern("/users/\\d+")))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.username").value("username123"))
        .andExpect(jsonPath("$.password").doesNotExist()) // パスワードが含まれないことを確認
        .andDo(print());// 結果みたいから追記した
  }

  /**
   * POST /users エンドポイントのバリデーションをテストする。
   *
   * <p>このテストでは、リクエストボディに username が含まれていない場合に、
   * サーバーが 400 Bad Request を返すことを検証します。</p>
   *
   * <p>検証項目:</p>
   * <ul>
   *   <li>CSRF トークンが設定されていること</li>
   *   <li>Content-Type が JSON に設定されていること</li>
   *   <li>ステータスコードが 400 Bad Request であること</li>
   * </ul>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("POST /users：400 Bad Request > リクエストに username がないとき")
  public void createUser_badRequest() throws Exception {
    // ## Arrange ##
    // username を含まない不正なリクエストボディを準備
    String newUserJson = """
        {
            "password": "password123"
        }
        """;

    // ## Act ##
    // MockMvc を使用して POST リクエストを送信
    var actual = mockMvc.perform(post("/users")
        .with(csrf()) // CSRFトークンを含める（セキュリティ設定で必須）:403エラー対策
        .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定: 415エラー対策
        .content(newUserJson)); // リクエストボディとしてユーザーデータを送信

    // ## Assert ##
    // サーバーが 400 Bad Request を返すことを確認
    actual
        .andDo(print()) // レスポンスの内容を標準出力に表示（デバッグ目的）
        .andExpect(status().isBadRequest());
  }

  /**
   * POST /users: ユーザー登録のバリデーション（重複したユーザー名）をテストします。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>既に登録されているユーザー名を指定してリクエストを送信した場合、サーバーが 400 Bad Request を返すこと。</li>
   *   <li>CSRFトークンが正しく処理されること。</li>
   *   <li>リクエストの Content-Type が適切であること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>サービス層を利用して、重複するユーザー名を事前に登録。</li>
   *   <li>MockMvc を使用して同じユーザー名でリクエストを送信。</li>
   *   <li>サーバーが 400 Bad Request を返すことを検証。</li>
   * </ol>
   *
   * @throws Exception テスト実行中に予期しない例外が発生した場合
   */
  @Test
  @DisplayName("POST /users：400 Bad Request > すでに登録されているユーザーを指定したとき")
  public void createUser_badRequest_duplicatedUsername() throws Exception {
    // ## Arrange ##
    // 重複するユーザー名を定義し、事前に登録
    var duplicatedUsername = "username00";
    userService.register(duplicatedUsername, "password00");

    // リクエストボディとして送信するJSONデータを作成
    var newUserJson = """
        {
          "username": "%s",
          "password": "password00"
        }
        """.formatted(duplicatedUsername);

    // ## Act ##
    // MockMvc を使用して POST リクエストを送信
    var actual = mockMvc.perform(post("/users")
        .with(csrf()) // CSRFトークンを含める（セキュリティ設定で必須）:403エラー対策
        .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-TypeをJSONに設定: 415エラー対策
        .content(newUserJson)); // リクエストボディとしてユーザーデータを送信

    // ## Assert ##
    // サーバーが 400 Bad Request を返すことを確認
    actual.andExpect(status().isBadRequest());
  }
}