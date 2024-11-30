package com.example.blog.service.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * ユーザーサービス（{@link UserService}）のテストクラス。
 * <p>
 * このクラスでは、ユーザー登録機能やその他のサービス層のロジックが正しく動作することを検証します。
 * </p>
 */
@MybatisDefaultDatasourceTest
@Import({UserService.class, PasswordEncoderConfig.class})
class UserServiceTest {

  @Autowired
  private UserService cut; // テスト対象のクラス（class under test: cut）

  @Autowired
  private UserRepository userRepository; // ユーザーデータの永続化操作を行うリポジトリ

  @Autowired
  private ApplicationContext ctx; // アプリケーション全体のBean管理状況を確認するためのコンテキスト

  /**
   * DI（依存性注入）が成功していることを確認するテスト。
   * <p>
   * このテストは、SpringのDIコンテナが正しく動作し、テスト対象のBeanが正しく初期化されていることを検証します。
   * </p>
   */
  @Test
  void successAutowired() {
    // UserService と UserRepository がDIコンテナから取得できることを確認
    assertThat(cut).isNotNull();
    assertThat(userRepository).isNotNull();

    // アプリケーションコンテキストに登録されているBeanの数を出力
    System.out.println("bean.length = " + ctx.getBeanDefinitionNames().length);
  }

  /**
   * register メソッドのテスト。
   * <p>
   * 指定されたユーザー名とパスワードで、新しいユーザーをデータベースに登録できることを検証します。
   * </p>
   *
   * <p>主な検証点：</p>
   * <ul>
   *   <li>パスワードがハッシュ化されて保存されること。</li>
   *   <li>新規登録されたアカウントが有効（enabled）状態であること。</li>
   * </ul>
   */
  @Test
  @DisplayName("register: ユーザーがデータベースに登録される")
  void register_success() {
    // ## Arrange ##
    var username = "test_username";
    var password = "test_password";

    // ## Act ##
    cut.register(username, password);

    // ## Assert ##
    // passwordとenabled以外の値は、UserRepositoryのテストで検証済みのため、省略。
    var actual = userRepository.selectByUsername(username);

    // 取得したユーザーのフィールドを検証
    assertThat(actual).hasValueSatisfying(actualEntity -> {
      // パスワードがハッシュ化されていることを確認
      assertThat(actualEntity.getPassword())
          .describedAs("入力された生のパスワードがハッシュ化されていること")
          .isNotEmpty()
          .isNotEqualTo(password);
      // enabled フィールドが true であることを確認
      assertThat(actualEntity.isEnabled())
          .describedAs("ユーザー新規登録時には、 有効なアカウントとして登録する")
          .isTrue();
    });
  }

  /**
   * ユーザー名存在確認機能（existsUsername）のテスト。
   *
   * <p>ユーザー名がデータベースに既に存在している場合、true を返すことを確認します。</p>
   */
  @Test
  @DisplayName("existsUsername: ユーザー名が存在するとき true")
  void method_success() {
    // ## Arrange ##
    // 登録済みユーザー情報を設定
    var username = "test_username";
    var alreadyExistUser = new UserEntity(null, username, "test_password", true);

    // ユーザーをデータベースに登録
    userRepository.insert(alreadyExistUser);

    // ## Act ##
    // ユーザー名存在確認メソッドを呼び出し
    var actual = cut.existsUsername(username);

    // ## Assert ##
    // ユーザー名が存在する場合、true が返ることを確認
    assertThat(actual).isTrue();
  }

  /**
   * existsUsername メソッドのテスト。
   *
   * <p>このテストは、指定されたユーザー名がデータベースに存在しない場合に
   * false を返すことを検証します。</p>
   *
   * <p>主な検証点：</p>
   * <ul>
   *   <li>データベースに存在しないユーザー名を確認した場合、メソッドが false を返すこと。</li>
   * </ul>
   */
  @Test
  @DisplayName("existsUsername: ユーザー名が存在しないとき false")
  void existsUsername_returnFalse() {
    // ## Arrange ##
    // データベースに登録されるテスト用ユーザーを作成
    var user = new UserEntity(null, "test_username", "test_password", true);

    // ユーザーをデータベースに登録
    userRepository.insert(user);

    // ## Act ##
    // 指定したユーザー名が存在するか確認
    var actual = cut.existsUsername("new_username");

    // ## Assert ##
    // 指定したユーザー名が存在しないため、false が返されることを検証
    assertThat(actual).isFalse();
  }
}