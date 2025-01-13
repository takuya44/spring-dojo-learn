package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

  @MockBean
  private ArticleService articleService;

  @Test
  void setUp_success() {
    // ## Arrange ##

    // ## Act ##

    // ## Assert ##
    assertThat(mockMvc).isNotNull();
    assertThat(articleService).isNotNull();
  }

  @Test
  @DisplayName("POST /articles: 500 InternalServerError で stacktrace が露出しない")
  void createUser_internalServerError() throws Exception {
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
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.instance").isEmpty())
        .andExpect(jsonPath("$", aMapWithSize(5)));
  }
}
