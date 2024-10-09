package com.example.blog.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CSRFCookieRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("/csrf-cookie: GET リクエストを送ると、204 No Content を返し、Set-CookieヘッダにXSRF-TOKENが含まれている")
  void return204() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var result = mockMvc.perform(get("/csrf-cookie"));

    // ## Assert ##
    result
        .andExpect(status().isNoContent())
        .andExpect(header().string("Set-Cookie", containsString("XSRF-TOKEN=")));
  }
}