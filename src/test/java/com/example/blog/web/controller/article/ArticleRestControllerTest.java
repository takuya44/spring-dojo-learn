package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;

  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
  }

  @Test
  @DisplayName("POST /articles: 記事の新規作成に成功する")
  void createArticle_success() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf())
            .with(user("user1"))
    );

    // ## Assert ##
    actual.andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /articles: 未ログインのとき、401 Unauthorized を返す")
  void createArticle_401Unauthorized() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var actual = mockMvc.perform(
        post("/articles")
            .with(csrf())
        // .with(user("user1")) // 未ログイン状態でテストするため、コメントアウトしている
    );

    // ## Assert ##
    actual
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
        .andExpect(jsonPath("$.instance").value("/articles"))
    ;
  }
}