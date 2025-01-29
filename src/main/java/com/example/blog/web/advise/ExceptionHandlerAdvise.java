package com.example.blog.web.advise;

import com.example.blog.model.BadRequest;
import com.example.blog.model.ErrorDetail;
import com.example.blog.model.Forbidden;
import com.example.blog.model.InternalServerError;
import com.example.blog.model.NotFound;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.exception.UnauthorizedResourceAccessException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
@RequiredArgsConstructor
public class ExceptionHandlerAdvise {

  private final MessageSource messageSource;

  /**
   * バリデーションエラー時に 400 Bad Request を返す例外ハンドラ。
   *
   * <p>このメソッドは {@link MethodArgumentNotValidException} をキャッチし、リクエストデータに含まれる
   * バリデーションエラー情報をクライアントに返すレスポンスを生成します。</p>
   *
   * <p>処理の概要:</p>
   * <ul>
   *   <li>例外オブジェクトからバリデーションエラー情報を抽出。</li>
   *   <li>エラー詳細をリスト形式で整形し、カスタムレスポンスオブジェクト {@link BadRequest} に設定。</li>
   *   <li>HTTP 400 Bad Request のレスポンスを生成して返却。</li>
   * </ul>
   *
   * <p>この例外ハンドラは、ユーザー入力のバリデーション失敗を通知し、詳細なエラー情報を含むレスポンスを提供します。</p>
   *
   * @param e バリデーションエラーを含む例外オブジェクト
   * @return バリデーションエラーの詳細を含む HTTP 400 Bad Request レスポンス
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BadRequest> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e
  ) {
    // バリデーションエラー情報を格納するカスタムオブジェクトを作成
    var body = new BadRequest();

    // 例外オブジェクトからレスポンス用のプロパティをコピー
    // ここでは、例外から取得したエラー情報をカスタムオブジェクトに転写
    BeanUtils.copyProperties(e.getBody(), body);

    // 現在のロケールを取得（多言語対応）
    var locale = LocaleContextHolder.getLocale();

    // バリデーションエラーの詳細をリスト形式で収集
    var errorDetailList = new ArrayList<ErrorDetail>();

    for (final FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      // フィールドエラー情報を収集
      var pointer = "#/" + fieldError.getField(); // エラー発生箇所を JSON Pointer 形式で表現
      var detail = messageSource.getMessage(fieldError, locale); // ロケールに応じたバリデーションエラーメッセージを取得

      // ErrorDetail オブジェクトを生成
      var errorDetail = new ErrorDetail();
      errorDetail.setPointer(pointer);
      errorDetail.setDetail(detail);

      // エラーディテールリストに追加
      errorDetailList.add(errorDetail);
    }

    // エラーディテールリストをレスポンスオブジェクトに設定
    body.setErrors(errorDetailList);

    // HTTP 400 Bad Request レスポンスを生成
    return ResponseEntity
        .badRequest() // ステータスコード 400 を設定
        .contentType(MediaType.APPLICATION_PROBLEM_JSON) // application/problem+json コンテンツタイプを設定
        .body(body); // レスポンスボディにエラー詳細を含むオブジェクトを設定
  }

  /**
   * RuntimeException をキャッチし、500 Internal Server Error のレスポンスを返す例外ハンドラ。
   *
   * <p>このメソッドは、コントローラー内で発生した予期しない {@link RuntimeException} を処理し、
   * クライアントに適切なエラーレスポンスを提供します。</p>
   *
   * <p>処理の流れ:</p>
   * <ul>
   *   <li>{@link ExceptionHandler} アノテーションにより、{@link RuntimeException} 発生時にこのメソッドが呼び出されます。</li>
   *   <li>HTTP ステータスコード 500 (Internal Server Error) を設定します。</li>
   *   <li>レスポンスの Content-Type を {@code application/problem+json} に設定します。</li>
   *   <li>{@link InternalServerError} オブジェクトを使用して、エラーの詳細情報をレスポンスに含めます。</li>
   *   <li>エラーの発生元 URI をレスポンスに設定します。</li>
   * </ul>
   *
   * <p>この例外ハンドラにより、システム内部の詳細（スタックトレースなど）がクライアントに露出しないようにします。</p>
   *
   * @param e       捕捉されたランタイム例外
   * @param request 現在の HTTP リクエスト情報
   * @return 500 Internal Server Error のレスポンスと共に、カスタムエラーメッセージを返す
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<InternalServerError> handleRuntimeException(
      RuntimeException e,
      HttpServletRequest request
  ) {
    // 500ステータスコードと、application/problem+json のコンテンツタイプを設定してレスポンスを返す
    return ResponseEntity
        .internalServerError() // HTTP 500 ステータスコードを返す
        .contentType(MediaType.APPLICATION_PROBLEM_JSON) // application/problem+json コンテンツタイプを設定
        .body(new InternalServerError()
            .instance(URI.create(request.getRequestURI()))// 発生元のリクエスト URI
        ); // エラーオブジェクトをレスポンスのボディとして返す
  }

  /**
   * カスタム例外 {@link ResourceNotFoundException} が発生した場合のハンドリングを行うメソッド。
   *
   * <p>このメソッドは、指定されたリソースが存在しない場合にスローされる {@link ResourceNotFoundException} をキャッチし、
   * クライアントに 404 Not Found ステータスコードを適切なエラーレスポンスと共に返します。</p>
   *
   * <p>処理の概要:</p>
   * <ul>
   *   <li>{@link ExceptionHandler} アノテーションにより、このメソッドが例外発生時に自動的に呼び出されます。</li>
   *   <li>HTTP ステータスコード 404 (Not Found) を設定します。</li>
   *   <li>レスポンスの Content-Type を {@code application/problem+json} に設定します。</li>
   *   <li>{@link NotFound} クラスを使用して、エラーメッセージと発生元のリクエスト URI をレスポンスに含めます。</li>
   * </ul>
   *
   * @param e       発生した {@link ResourceNotFoundException} のインスタンス
   * @param request 現在の HTTP リクエスト情報
   * @return 404 Not Found のエラーレスポンス
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<NotFound> handleResourceNotFoundException(
      ResourceNotFoundException e,
      HttpServletRequest request
  ) {
    // リソースが見つからない場合に404ステータスコードを返す
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(new NotFound()
            .detail("リソースが見つかりません") // エラーメッセージ
            .instance(URI.create(request.getRequestURI())) // 発生元のリクエスト URI
        );
  }

  /**
   * カスタム例外 {@link UnauthorizedResourceAccessException} が発生した場合のハンドリングを行うメソッド。
   *
   * <p>このメソッドは、認可されていないリソースへのアクセスが試みられた際にスローされる
   * {@link UnauthorizedResourceAccessException} をキャッチし、クライアントに HTTP 403 Forbidden
   * ステータスコードを返します。</p>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>403 Forbidden ステータスコードを設定。</li>
   *   <li>エラー詳細として、エラーメッセージとリクエスト元のURIを含むレスポンスボディを作成。</li>
   *   <li>レスポンスをクライアントに返却。</li>
   * </ol>
   *
   * <p>レスポンスは RFC 7807 に準拠したフォーマット（application/problem+json）で返されます。</p>
   *
   * @param e       捕捉された {@link UnauthorizedResourceAccessException}
   * @param request 発生元の HTTP リクエスト情報
   * @return HTTP 403 Forbidden のレスポンスとエラーメッセージ
   */
  @ExceptionHandler(UnauthorizedResourceAccessException.class)
  public ResponseEntity<Forbidden> handleUnauthorizedResourceAccessException(
      UnauthorizedResourceAccessException e,
      HttpServletRequest request
  ) {
    // リソースへのアクセスが拒否された場合に 403 ステータスコードを返す
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN) // HTTP 403 ステータスコード
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(new Forbidden()
            .detail("リソースへのアクセスが拒否されました") // エラーメッセージ
            .instance(URI.create(request.getRequestURI())) // 発生元のリクエスト URI
        );
  }

}
