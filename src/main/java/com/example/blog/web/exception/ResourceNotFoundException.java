package com.example.blog.web.exception;

/**
 * カスタム例外クラス {@code ResourceNotFoundException}。
 *
 * <p>このクラスは、リソースが見つからなかった場合にスローされる例外です。
 * 例えば、データベースに存在しないリソースや無効なリソースIDを要求した場合に、 コントローラやサービス層でこの例外を発生させることができます。</p>
 *
 * <p>{@link RuntimeException} を継承しているため、チェックされない例外（非チェック例外）として機能し、
 * キャッチする必要がない場合でも処理を強制されません。</p>
 */
public class ResourceNotFoundException extends RuntimeException {

  // デフォルトコンストラクタ
  public ResourceNotFoundException() {
    super();
  }

  // エラーメッセージを指定できるコンストラクタ
  public ResourceNotFoundException(String message) {
    super(message);
  }

  // エラーメッセージと例外の原因を指定できるコンストラクタ
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  // 例外の原因を指定できるコンストラクタ
  public ResourceNotFoundException(Throwable cause) {
    super(cause);
  }
}
