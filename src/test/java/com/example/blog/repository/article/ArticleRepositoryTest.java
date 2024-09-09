package com.example.blog.repository.article;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

// MyBatisのテストを行うためのテストクラス
// @MybatisTestアノテーションは、MyBatis関連のコンポーネントのみをロードし、テストを行います。
@MybatisTest
// 実際のデータベースを使用する設定を行うアノテーション。
// replace = AutoConfigureTestDatabase.Replace.NONEにより、Springがデフォルトで組み込みデータベースを使用するのを防ぎ、
// application.ymlやapplication.propertiesで定義された実際のデータベース設定を使用します。
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

  // テスト対象（cut: class under test）のArticleRepositoryを自動的に注入
  @Autowired
  private ArticleRepository cut;

  // 単純なテストメソッド。cut（ArticleRepository）がnullではないことを検証。
  // これにより、Springのコンテナが正常に動作し、ArticleRepositoryが正しく注入されているかどうかを確認します。
  @Test
  public void test() {
    assertThat(cut).isNotNull();
  }
}