package com.example.blog.security;

import java.util.List;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

/**
 * Spring Security の認証済みユーザーを表すクラス。
 *
 * <p>このクラスは、Spring Security の {@link User} クラスを拡張し、
 * 認証済みのユーザーに対して一意のユーザー ID（userId）を追加します。</p>
 */
@Getter
public class LoggedInUser extends User {

  // データベースなどで管理されている一意のユーザー ID
  private final long userId;

  /**
   * {@code LoggedInUser} クラスのコンストラクタ。
   *
   * <p>Spring Security のユーザー情報（ユーザー名、パスワード、アカウントの有効性など）に加え、
   * アプリケーション独自の属性（userId）を追加します。</p>
   *
   * @param userId   データベースなどで管理される一意のユーザー ID
   * @param username ユーザー名（ログインに使用される）
   * @param password 暗号化されたパスワード
   * @param enabled  アカウントが有効であるかどうか（{@code true}: 有効、{@code false}: 無効）
   */
  public LoggedInUser(
      long userId,
      String username,
      String password,
      boolean enabled
  ) {
    // 親クラス（Spring Security の User クラス）のコンストラクタを呼び出す
    // List.of() は、ユーザーの権限（ロール）を空リストとして渡しています
    super(username, password, enabled, true, true, true, List.of());
    this.userId = userId; // このクラス特有の userId を設定

    // Lombok の @Getter アノテーションにより、以下のようなゲッターメソッドが自動生成されます：
    // public long getUserId() {
    //   return userId;
    // }
  }
}
