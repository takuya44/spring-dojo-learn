package com.example.blog.repository.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.service.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

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

  /**
   * 指定されたユーザー名のユーザーがデータベースに存在することを検証するメソッド {@link UserRepository#selectByUsername(String)}メソッドが正しく
   * {@link java.util.Optional< UserEntity >} を返すことを検証します。
   *
   * <p>テストの準備段階として、@Sqlアノテーションを使用して、必要なテストデータをデータベースに挿入。
   * この場合、2つのユーザー (test_user1, test_user2) が事前に挿入されています。</p>
   *
   * <p>主な検証点は次の通りです:
   * <ul>
   *   <li>指定されたユーザー名 ('test_user1') に一致するユーザーエンティティがデータベースから取得されること。</li>
   *   <li>取得されたエンティティの各フィールド (username, password, enabled) の値が期待通りであること。</li>
   * </ul>
   * </p>
   *
   * @throws Exception テスト中にエラーが発生した場合
   */
  @Test
  @DisplayName("selectByUsername: 指定されたユーザー名のユーザーが存在するとき、Optional<UserEntity>を返す")
  @Sql(statements = {
      "INSERT INTO users (id, username, password, enabled) VALUES (998, 'test_user1', 'test_password', true);",
      "INSERT INTO users (id, username, password, enabled) VALUES (999, 'test_user2', 'test_password', true);"
  })
  void selectByUsername_returnNotEmptyOptional() {
    // ## Arrange ##
    // ## テストデータは@Sqlアノテーションにより事前に挿入されている

    // ## Act ##
    // ユーザー名 "test_user1" に一致するユーザーエンティティを取得
    var actual = cut.selectByUsername("test_user1");

    // ## Assert ##
    // Optional に値が含まれていること、かつ取得したエンティティの内容が期待通りであることを確認
    assertThat(actual)
        .hasValueSatisfying(actualEntity -> {
          assertThat(actualEntity.id()).isEqualTo(998);
          assertThat(actualEntity.username()).isEqualTo("test_user1");
          assertThat(actualEntity.password()).isEqualTo("test_password");
          assertThat(actualEntity.enabled()).isTrue();
        });
  }

  /**
   * 存在しないユーザー名を指定して selectByUsername を実行したときのテスト {@link UserRepository#selectByUsername(String)}
   *
   * <p>
   * このテストは、データベースに存在しないユーザー名が指定された場合に、 メソッドが正しく空の {@link Optional} を返すことを確認します。
   * </p>
   */
  @Test
  @DisplayName("selectByUsername: 指定されたユーザー名のユーザーが存在しないとき、Optional.empty を返す")
  @Sql(statements = {
      "INSERT INTO users (id, username, password, enabled) VALUES (998, 'test_user1', 'test_password', true);",
  })
  void selectByUsername_returnEmpty() {
    // ## Arrange ##
    // テストデータとして 'test_user1' ユーザーをデータベースに挿入。

    // ## Act ##
    // 存在しないユーザー名 'invalid_user' を指定してメソッドを呼び出し
    var actual = cut.selectByUsername("invalid_user");

    // ## Assert ##
    // 結果が空の Optional であることを検証
    assertThat(actual).isEmpty();
  }

  /**
   * null が指定されたとき、全件検索されないことを確認
   * <p>
   * このテストでは、username に null が指定された場合、メソッドが全件検索を実行せず、 正しく空の {@link Optional} を返すことを確認します。
   * さらに、データベース内に username がリテラル文字列 'null' が存在しても、 これが null として扱われないことを検証します。
   * </p>
   */
  @Test
  @DisplayName("selectByUsername: username に null が指定されたとき、Optional.Empty を返す（全件検索しない）")
  @Sql(statements = {
      "INSERT INTO users (id, username, password, enabled) VALUES (998, 'null', 'test_password', true);"
  })
  void selectByUsername_returnNullWhenGivenUsernameIsNull() {
    // ## Arrange ##

    // ## Act ##
    // 存在しないユーザー名 'null' を指定してメソッドを呼び出し
    var actual = cut.selectByUsername(null);

    // ## Assert ##
    assertThat(actual).isEmpty();
  }
}