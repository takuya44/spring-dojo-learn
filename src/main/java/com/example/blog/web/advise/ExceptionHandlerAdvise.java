package com.example.blog.web.advise;

import com.example.blog.model.InternalServerError;
import com.example.blog.web.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
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

    // 500ステータスコードと、application/problem+json のコンテンツタイプを設定してレスポンスを返す
    return ResponseEntity
        .internalServerError() // HTTP 500 ステータスコードを返す
        .contentType(MediaType.APPLICATION_PROBLEM_JSON) // application/problem+json コンテンツタイプを設定
        .body(error); // エラーオブジェクトをレスポンスのボディとして返す
  }

  /**
   * カスタム例外 {@link ResourceNotFoundException} が発生した場合のハンドリングを行うメソッド。
   *
   * <p>このメソッドは、指定されたリソースが存在しない場合にスローされる {@link ResourceNotFoundException} をキャッチし、
   * クライアントに 404 Not Found ステータスコードを返します。</p>
   *
   * <p>{@link ExceptionHandler} アノテーションを使用して、例外発生時にこのメソッドが呼び出されます。</p>
   *
   * @param e 発生した {@link ResourceNotFoundException} のインスタンス
   * @return 404 Not Found のレスポンスを返します
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
    // リソースが見つからない場合に404ステータスコードを返す
    return ResponseEntity.notFound().build();
  }

}
