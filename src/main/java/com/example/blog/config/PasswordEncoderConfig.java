package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

  /**
   * パスワードエンコーダーを構成する。 NoOpPasswordEncoder を使用し、パスワードのエンコードは行わない。 （セキュリティ上の理由から、実運用では推奨されない）
   *
   * @return パスワードエンコーダー
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
