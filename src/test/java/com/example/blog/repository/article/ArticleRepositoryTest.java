package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest
class ArticleRepositoryTest {

  @Autowired
  private ArticleRepository cut;

  @Test
  public void test() {
    assertThat(cut).isNotNull();
  }
}