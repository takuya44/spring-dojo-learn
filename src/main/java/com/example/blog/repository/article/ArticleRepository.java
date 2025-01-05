package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleRepository {

  @Select("""
      SELECT
          id
        , title
        , body
        , created_at
        , updated_at
      FROM articles
      WHERE id = #{id}
      """)
  Optional<ArticleEntity> selectById(@Param("id") long id);

  @Insert("""
      INSERT INTO articles (title, body, user_id, created_at, updated_at)
      VALUES (#{title}, #{body}, #{author.id}, #{createdAt}, #{updatedAt})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  void insert(ArticleEntity newArticle);
}
