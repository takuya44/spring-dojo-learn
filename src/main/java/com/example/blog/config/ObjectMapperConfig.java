package com.example.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson の {@link ObjectMapper} をカスタマイズする設定クラス。
 *
 * <p>このクラスでは、Spring アプリケーション全体で使用される Jackson の設定をカスタマイズします。</p>
 *
 * <p>主な設定内容:</p>
 * <ul>
 *   <li>Java 8 の日時 API をサポートするモジュール（{@link JavaTimeModule}）の登録</li>
 *   <li>日時をタイムスタンプ（エポック秒）ではなく ISO-8601 形式でシリアライズ</li>
 * </ul>
 */
@Configuration
public class ObjectMapperConfig {

  /**
   * アプリケーション全体で使用されるカスタマイズ済みの {@link ObjectMapper} を定義します。
   *
   * <p>設定内容:</p>
   * <ul>
   *   <li>{@link JavaTimeModule} を登録して、Java 8 の日時型をサポート</li>
   *   <li>{@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} を無効化し、\n
   *   日時を ISO-8601 形式で出力</li>
   * </ul>
   *
   * @return カスタマイズ済みの {@link ObjectMapper}
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule()) // Java 8 の日時 API 用モジュールを登録
        .disable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 日時をタイムスタンプでなく ISO-8601 形式でシリアライズ
  }
}
