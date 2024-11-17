package com.example.blog.service.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ユーザー情報を表現するエンティティクラス。
 * <p>
 * このクラスは、ユーザーの基本情報を保持し、データベースとやり取りする際のモデルとして使用されます。
 * </p>
 *
 * <p>主なフィールド:</p>
 * <ul>
 *   <li>{@code id}: ユーザーの一意な識別子（自動採番）。</li>
 *   <li>{@code username}: ユーザー名。</li>
 *   <li>{@code password}: 暗号化されたユーザーのパスワード。</li>
 *   <li>{@code enabled}: ユーザーが有効かどうかを示すフラグ。</li>
 * </ul>
 *
 * <p>このクラスには以下の機能が付加されています:</p>
 * <ul>
 *   <li>Lombok を使用して、全てのフィールドを引数に取るコンストラクタを自動生成。</li>
 *   <li>Getter と Setter メソッドを自動生成。</li>
 * </ul>
 */
@AllArgsConstructor // 全てのフィールドを引数に取るコンストラクタを自動生成
@Data // Getter、Setter、toString、equals、hashCode を自動生成
public class UserEntity {

  private Long id; // Insert後、自動採番される
  private String username;
  private String password;
  private boolean enabled;
}
