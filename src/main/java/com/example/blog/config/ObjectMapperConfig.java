package com.example.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jacksonの {@link ObjectMapper} をカスタマイズするための設定クラス。
 *
 * <p>このクラスでは、日時のフォーマットをISO 8601形式でシリアライズするために、
 * {@link JavaTimeModule} を登録し、{@link LocalDateTime} のシリアライザをカスタマイズします。</p>
 */
@Configuration
public class ObjectMapperConfig {

  /**
   * {@link ObjectMapper} のカスタム設定を定義するメソッド。
   *
   * <p>このメソッドでは、{@link JavaTimeModule} に対して {@link LocalDateTime} のシリアライザを追加し、
   * 日時をISO 8601形式（例: "2024-09-12T15:30:00"）でシリアライズする設定を行います。</p>
   *
   * @return カスタマイズされた {@link ObjectMapper} のインスタンス
   */
  @Bean
  public ObjectMapper objectMapper() {
    // JavaのLocalDateTimeクラスをISO 8601形式でシリアライズするためのシリアライザを追加
    var timeModule = new JavaTimeModule();
    timeModule.addSerializer(
        LocalDateTime.class,
        new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME)
    );

    // カスタマイズされたJavaTimeModuleをObjectMapperに登録して返す
    return new ObjectMapper()
        .registerModule(timeModule);
  }
}
