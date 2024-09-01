package com.example.blog;

// 可読性のために、static import して使うことが多い

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.web.controller.user.UserRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class BlogApplicationTests {

  @Autowired
  private UserRestController userRestController;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void contextLoads() {
    // ## Arrange ##
    var bean = applicationContext.getBean("userRestController");

    // ## Act ##

    // ## Assert ##
    assertThat(bean).isNotNull();
  }

}
