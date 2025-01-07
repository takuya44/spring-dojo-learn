package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleRepository {

  /**
   * IDに基づいて記事を取得するクエリ。
   * <p>SQLのカラム名とエンティティのフィールド名をマッピングしています。</p>
   *
   * @param articleId 検索対象の記事ID
   * @return 該当する記事のOptionalオブジェクト
   */
  @Select("""
      SELECT
          a.id         AS article__id
        , a.title      AS article__title
        , a.body       AS article__body
        , a.created_at AS article__created_at
        , a.updated_at AS article__updated_at
        , u.id         AS user__id
        , u.username   AS user__username
        , u.enabled    AS user__enabled
      FROM articles a
      JOIN users u ON a.user_id = u.id
      WHERE a.id = #{articleId}
      """)
  @Results(value = {
      @Result(column = "article__id", property = "id"),
      @Result(column = "article__title", property = "title"),
      @Result(column = "article__body", property = "body"),
      @Result(column = "article__created_at", property = "createdAt"),
      @Result(column = "article__updated_at", property = "updatedAt"),

      @Result(column = "user__id", property = "author.id"),
      @Result(column = "user__username", property = "author.username"),
      @Result(column = "user__enabled", property = "author.enabled"),
  })
  Optional<ArticleEntity> selectById(@Param("articleId") long articleId);

  /**
   * 新しい記事をデータベースに挿入するクエリ。
   * <p>作成者ID（user_id）を含むフィールドを挿入し、挿入後に生成されたIDを取得します。</p>
   *
   * @param newArticle 挿入する記事オブジェクト
   */
  @Insert("""
      INSERT INTO articles (title, body, user_id, created_at, updated_at)
      VALUES (#{title}, #{body}, #{author.id}, #{createdAt}, #{updatedAt})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  void insert(ArticleEntity newArticle);
}
