package com.example.blog.repository.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ユーザリポジトリのテストクラス。 MyBatisを使用してデータベース操作を行うリポジトリ層のテストを実施。
 */
@MybatisDefaultDatasourceTest // MyBatisとデフォルトデータソースの設定を含むカスタムアノテーション
class UserRepositoryTest {

  @Autowired
  private UserRepository cut; // テスト対象のリポジトリ（Class Under Test）

  /**
   * リポジトリが正しくDIされていることを検証する基本的なテスト。
   */
  @Test
  void test() {
    assertThat(cut).isNotNull();
  }

}