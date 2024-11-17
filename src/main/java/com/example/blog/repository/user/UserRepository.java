package com.example.blog.repository.user;

import com.example.blog.service.user.UserEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserRepository {

  /**
   * 指定された username に基づいてユーザー情報を取得します。
   * <p>
   * このデフォルトメソッドは、入力された username が null の場合に SQL クエリを実行せずに空の {@link Optional} を返します。 username が非
   * null の場合は、{@link #selectByUsernameInternal(String)} メソッドを使用して 実際のデータベースクエリを実行します。
   * </p>
   *
   * @param username 検索対象のユーザー名（null の場合はクエリを実行しません）
   * @return ユーザー情報が存在する場合は {@link Optional<UserEntity>} を返し、 存在しない場合は空の {@link Optional} を返します。
   */
  default Optional<UserEntity> selectByUsername(String username) {
    return Optional.ofNullable(username) // → Optiona<string> = "test_user"
        .flatMap(this::selectByUsernameInternal); // ネストを防止 → Optiona<Optional<UserEntity>> = {{}};
  }

  @Select("""
      SELECT
          id
        , u.username
        , u.password
        , u.enabled
      FROM users u
      WHERE u.username = #{username}
      """)
  Optional<UserEntity> selectByUsernameInternal(@Param("username") String username);

  /**
   * データベースに新しいユーザーを登録するメソッド。
   * <p>
   * このメソッドは、MyBatis を使用して `users` テーブルに新しいユーザーを挿入します。 挿入時に `id` 列は自動採番され、その値が指定されたエンティティの `id`
   * フィールドに反映されます。
   * </p>
   *
   * @param entity 登録するユーザー情報を保持する {@link UserEntity} オブジェクト。 - `username` ユーザー名 - `password`
   *               パスワード（暗号化されたもの） - `enabled` ユーザーが有効かどうかを示すフラグ
   */
  @Insert("""
      INSERT INTO users(username, password, enabled)
      VALUES (#{username}, #{password}, #{enabled})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  void insert(UserEntity entity);

  @Delete("""
      DELETE FROM users u
      WHERE u.username = #{username}
      """)
  void deleteByUsername(@Param("username") String username);
}
