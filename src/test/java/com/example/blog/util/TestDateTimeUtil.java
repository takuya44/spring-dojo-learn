package com.example.blog.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * テスト用日時ユーティリティクラス。
 *
 * <p>このクラスは、指定された年月日・時刻に基づいて、<br>
 * 日本標準時（Asia/Tokyo）の {@link OffsetDateTime} を生成します。</p>
 *
 * <p>主にテストケースで使用されます。</p>
 */
public final class TestDateTimeUtil {

  /**
   * 指定された年月日・時刻に基づいて {@link OffsetDateTime} を生成します。
   *
   * @param year       年（例: 2025）
   * @param month      月（例: {@link Month#JANUARY}）
   * @param dayOfMonth 日（1~31）
   * @param hour       時間（0~23）
   * @param minute     分（0~59）
   * @return 指定された日時に基づく日本標準時の {@link OffsetDateTime}
   */
  public static OffsetDateTime of(
      int year,
      int month,
      int dayOfMonth,
      int hour,
      int minute
  ) {
    // ローカル日時を生成
    var ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    // タイムゾーンを設定
    var zoneId = ZoneId.of("Asia/Tokyo");
    // ZonedDateTimeからOffsetDateTimeに変換して返す
    return ZonedDateTime.of(ldt, zoneId).toOffsetDateTime();
  }
}
