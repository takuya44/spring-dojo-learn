package com.example.blog.service.user;

import com.example.blog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー管理に関するビジネスロジックを提供するサービスクラス。
 *
 * <p>このクラスは以下の主要機能を提供します:</p>
 * <ul>
 *   <li>ユーザーの登録</li>
 *   <li>ユーザーの削除</li>
 *   <li>ユーザー名の存在確認</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor // 必須フィールドをコンストラクタにより自動注入
public class UserService {

  private final UserRepository userRepository;// ユーザーデータベース操作のためのリポジトリ
  private final PasswordEncoder passwordEncoder; // パスワードのエンコード処理

  /**
   * 新しいユーザーを登録します。
   *
   * <p>パスワードはエンコードされてデータベースに保存されます。</p>
   *
   * @param username    ユーザー名
   * @param rawPassword 平文のパスワード
   * @return 登録されたユーザーエンティティ
   */
  @Transactional
  public UserEntity register(String username, String rawPassword) {
    // パスワードをエンコード
    var encodedPassword = passwordEncoder.encode(rawPassword);

    // 新しいユーザーエンティティを作成
    var newUser = new UserEntity(null, username, encodedPassword, true);

    // ユーザーをデータベースに挿入
    userRepository.insert(newUser);

    // 登録されたユーザーを返す
    return newUser;
  }

  /**
   * 指定されたユーザー名のユーザーを削除します。
   *
   * @param username 削除対象のユーザー名
   */
  @Transactional
  public void delete(String username) {
    userRepository.deleteByUsername(username);
  }

  /**
   * 指定されたユーザー名が既に存在するかを確認します。
   *
   * @param username 確認対象のユーザー名
   * @return 存在する場合は true、存在しない場合は false
   */
  @Transactional(readOnly = true) // データベースの読み取り専用トランザクション: パフォーマンスの最適化
  public boolean existsUsername(String username) {
    return userRepository.selectByUsername(username).isPresent();
  }
}
