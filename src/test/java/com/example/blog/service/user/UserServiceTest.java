package com.example.blog.service.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

  @Autowired
  private UserService cut;

  @Autowired
  private UserRepository userRepository;

  @Test
  void successAutowired() {
    assertThat(cut).isNotNull();
    assertThat(userRepository).isNotNull();
  }

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

    assertThat(actual).hasValueSatisfying(actualEntity -> {
      assertThat(actualEntity.getPassword())
          .describedAs("入力された生のパスワードがハッシュ化されていること")
          .isNotEmpty()
          .isNotEqualTo(password);
      assertThat(actualEntity.isEnabled())
          .describedAs("ユーザー新規登録時には、 有効なアカウントとして登録する")
          .isTrue();
    });
  }
}