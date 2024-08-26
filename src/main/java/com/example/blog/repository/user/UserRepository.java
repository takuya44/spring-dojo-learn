package com.example.blog.repository.user;

import java.util.Optional;
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
}
