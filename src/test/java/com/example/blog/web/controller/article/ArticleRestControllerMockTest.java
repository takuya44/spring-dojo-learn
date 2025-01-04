package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.config.ObjectMapperConfig;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.config.SecurityConfig;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.web.exception.CustomAccessDeniedHandler;
import com.example.blog.web.exception.CustomAuthenticationEntryPoint;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@link ArticleRestController} のテストクラス。
 *
 * <p>{@link WebMvcTest} を使用して、Spring MVCのコンポーネントをロードし、コントローラー層のテストを実行します。
 * このテストはコントローラー層のみに焦点を当て、サービス層やリポジトリ層はモック化され、実際のデータベースアクセスは行われません。</p>
 *
 * <p>このテストクラスでは、以下のアノテーションを使用しています:
 * <ul>
 *   <li>{@link WebMvcTest}: MVC層にフォーカスしたテストを実行し、コントローラーのテストを支援するためのアノテーション。</li>
 *   <li>{@link MockBean}: サービス層の依存関係をモックとして注入し、テスト対象のコントローラーの依存関係を注入する際に使用します。</li>
 *   <li>{@link Import}: テスト対象に必要な追加の設定クラスをインポートします。
 *   ここでは {@link ObjectMapperConfig}（ObjectMapperのカスタム設定）と {@link SecurityConfig}（セキュリティ設定）をインポートしています。
 *   このアノテーションを使用することで、テスト環境でも実際のアプリケーションと同様の設定が反映されます。</li>
 * </ul>
 * </p>
 */
@WebMvcTest(ArticleRestController.class)
@Import({ObjectMapperConfig.class, SecurityConfig.class, PasswordEncoderConfig.class,
    CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class ArticleRestControllerMockTest {

  /**
   * Spring MVCのテストを行うための {@link MockMvc} オブジェクト。
   * <p>SpringのMVC関連のテストでは、HTTPリクエストを模擬してコントローラーの動作をテストするために使用します。</p>
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * {@link ArticleService} のモック。
   * <p>このモックは、コントローラー層の依存関係として注入されるもので、実際のサービス層を使用せずにテストを行います。</p>
   */
  @MockBean
  private ArticleService mockArticleService;

  /**
   * {@link UserDetailsService} のモック。
   * <p>ユーザー認証に関連するサービス層をモック化し、実際のユーザー認証処理を行わずにテストを実行します。</p>
   */
  @MockBean
  private UserDetailsService mockUserDetailsService;

  /**
   * {@link MockMvc} が正しく初期化されていることを検証するテスト。
   * <p>このテストでは、`MockMvc` オブジェクトがnullでないことを確認し、正常にテスト環境がセットアップされていることを確認します。</p>
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();  // MockMvcがnullでないことを確認
  }

  /**
   * GET /articles/{id}: 指定されたIDの記事が存在するとき、200 OKを返すテスト。
   *
   * <p>このテストでは、指定されたIDの記事が存在する場合、ステータスコード200が返されることを検証します。
   * モックされたサービスを使用して、記事のIDに対応する {@link ArticleEntity} を返し、正常なレスポンスが返されるかどうかを確認します。</p>
   *
   * <p>また、レスポンスの内容が期待通りであることを {@code jsonPath} を使用して検証し、ID、タイトル、コンテンツ、
   * 作成日時、更新日時のフィールドが期待通りの値を持っていることを確認します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("GET /articles/{id}: 指定されたIDの記事が存在するとき、200 OK")
  public void getArticlesById_200OK() throws Exception {
    // ## Arrange ##
    // テスト用の期待されるArticleEntityオブジェクトを作成
    var expected = new ArticleEntity(
        999L,
        "title_999",
        "content_999",
        null,
        LocalDateTime.of(2022, 1, 2, 3, 4, 5),
        LocalDateTime.of(2023, 1, 2, 3, 4, 5)
    );

    // モックされたArticleServiceが指定されたIDの記事を返すように設定
    when(mockArticleService.findById(999L)).thenReturn(Optional.of(expected));

    // ## Act ##
    // GETリクエストを送信し、レスポンスを受け取る
    var actual = mockMvc.perform(get("/articles/{id}", 999));

    // ## Assert ##
    // ステータスコード200 OKを期待し、レスポンス内容の検証
    actual
        .andExpect(status().isOk())  // ステータスコード200 OK
        .andExpect(jsonPath("$.id").value(expected.getId()))  // IDの検証
        .andExpect(jsonPath("$.title").value(expected.getTitle()))  // タイトルの検証
        .andExpect(jsonPath("$.content").value(expected.getContent()))  // コンテンツの検証
        .andExpect(jsonPath("$.createdAt").value(expected.getCreatedAt().toString()))  // 作成日時の検証
        .andExpect(jsonPath("$.updatedAt").value(expected.getUpdatedAt().toString()))  // 更新日時の検証
    ;
  }

  /**
   * GET /articles/{id}: 指定されたIDの記事が存在しないとき、404 Not Foundを返すテスト。
   *
   * <p>このテストでは、指定されたIDの記事が存在しない場合に、ステータスコード404が返されることを検証します。
   * サービス層の {@link ArticleService} が {@code Optional.empty()} を返すようにモックし、
   * コントローラーが適切に404エラーレスポンスを返すことを確認します。</p>
   *
   * @throws Exception テスト実行時に例外が発生した場合
   */
  @Test
  @DisplayName("GET /articles/{id}: 指定されたIDの記事が存在しないとき、404 Not Found")
  public void getArticlesById_404NotFound() throws Exception {
    // ## Arrange ##
    var expectedId = 999;

    // モックされたArticleServiceが指定されたIDの記事を見つけられないように設定
    when(mockArticleService.findById(expectedId)).thenReturn(Optional.empty());

    // ## Act ##
    // GETリクエストを送信し、レスポンスを受け取る
    var actual = mockMvc.perform(get("/articles/{id}", expectedId));

    // ## Assert ##
    // ステータスコード404 Not Foundを期待
    actual.andExpect(status().isNotFound());
  }
}
