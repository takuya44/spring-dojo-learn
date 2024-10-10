package com.example.blog.web.advise;

import com.example.blog.model.InternalServerError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全てのコントローラーに対する例外処理を提供するアドバイスクラス。
 * <p>
 * このクラスは、コントローラーの外で発生した例外に対して、適切なエラーレスポンスを返します。 主に {@link RuntimeException} をキャッチして、標準的な 500
 * Internal Server Error レスポンスを返します。
 * </p>
 */
@RestControllerAdvice
public class ExceptionHandlerAdvise {

  /**
   * RuntimeException をキャッチし、500 Internal Server Error のレスポンスを返す。
   *
   * <p>
   * このメソッドは、コントローラーで発生した {@link RuntimeException} を処理し、 クライアントにカスタマイズされたエラーメッセージを返します。
   * </p>
   *
   * @param e 捕捉されたランタイム例外
   * @return 500 Internal Server Error のレスポンスと共に、カスタムエラーメッセージを返す
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<InternalServerError> handleRuntimeException(RuntimeException e) {
    // InternalServerError オブジェクトを作成し、エラーの詳細情報を設定
    var error = new InternalServerError();
    error.setType(null);
    error.setTitle("Internal Server Error");
    error.setStatus(500);
    error.setDetail(null);
    error.setInstance(null);

    // 500ステータスコードと設定したレスポンスを返す
    return ResponseEntity.status(500).body(error);
  }
}
