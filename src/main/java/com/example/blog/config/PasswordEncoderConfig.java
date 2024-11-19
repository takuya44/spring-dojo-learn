package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * パスワードエンコーディングに関する設定を提供するクラス。
 * <p>
 * このクラスでは、Spring Security が提供する {@link BCryptPasswordEncoder} を使用して パスワードをハッシュ化するためのエンコーダーを定義します。
 * </p>
 * <p>
 * BCrypt はハッシュ化アルゴリズムの一種で、ハッシュ生成時にランダムなソルトを使用するため、 同じ入力でも異なるハッシュを生成します。セキュリティ上、安全な選択肢とされています。
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

  /**
   * パスワードエンコーダーを定義する。
   * <p>
   * {@link BCryptPasswordEncoder} を使用して、パスワードをハッシュ化します。 このハッシュ化エンコーダーは、セキュリティ上の理由から運用環境で推奨されます。
   * </p>
   *
   * @return {@link PasswordEncoder} インスタンス
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
