package com.example.blog.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationAndLoginIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void integrationTest() {
    // ## Arrange ##

    // ## Act ##
    var responseSpec = webTestClient.get().uri("/").exchange();

    // ## Action ##
    var response = responseSpec.returnResult(String.class);
    var xsrfTokenOpt = Optional.ofNullable(response.getResponseCookies().getFirst("XSRF-TOKEN"));

    // レスポンスのステータスコードが 204 No Content であることを検証
    responseSpec.expectStatus().isNoContent();

    // Cookie ヘッダーにXSRF-TOKENがある+空文字かどうかをチェック。
    assertThat(xsrfTokenOpt)
        .isPresent() // Optional<null>かOptional<xsrfTokenCookie = "aaa">の検証
        .hasValueSatisfying(xsrfTokenCookie ->
                assertThat(xsrfTokenCookie.getValue()).isNotBlank()
            // Optional<xsrfTokenCookie = "aaa">の値が空文字かどうかチェック。
        );
  }
}
