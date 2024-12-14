package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    );

    // ## Assert ##
    actual.andExpect(status().isCreated());
  }
}