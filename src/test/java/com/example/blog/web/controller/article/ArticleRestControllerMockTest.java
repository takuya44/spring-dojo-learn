package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.service.article.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 * </ul>
 * </p>
 */
@WebMvcTest(ArticleRestController.class)
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
   * {@link MockMvc} が正しく初期化されていることを検証するテスト。
   * <p>このテストでは、`MockMvc` オブジェクトがnullでないことを確認し、正常にテスト環境がセットアップされていることを確認します。</p>
   */
  @Test
  public void mockMvc() {
    assertThat(mockMvc).isNotNull();  // MockMvcがnullでないことを確認
  }
}
