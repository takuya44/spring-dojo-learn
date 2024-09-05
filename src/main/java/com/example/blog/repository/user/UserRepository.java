package com.example.blog.repository.user;

import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserRepository {

  @Select("""
      SELECT
          u.username
        , u.password
        , u.enabled
      FROM users u
      WHERE u.username = #{username}
      """)
  Optional<UserRecord> selectByUsername(@Param("username") String username);

  @Insert("""
      INSERT INTO users(username, password, enabled)
      VALUES (#{username}, #{password}, #{enabled})
      """)
  void insert(
      @Param("username") String username,
      @Param("password") String password,
      @Param("enabled") boolean enabled
  );

  @Delete("""
      DELETE FROM users u
      WHERE u.username = #{username}
      """)
  void deleteByUsername(@Param("username") String username);
}
