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
   * @param id 検索対象の記事ID
   * @return 該当する記事のOptionalオブジェクト
   */
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
  @Results(value = {
      @Result(column = "id", property = "id"),
      @Result(column = "title", property = "title"),
      @Result(column = "body", property = "body"),
      @Result(column = "created_at", property = "createdAt"),
      @Result(column = "updated_at", property = "updatedAt")
  })
  Optional<ArticleEntity> selectById(@Param("id") long id);

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
  // 自動生成されたIDを取得し、エンティティの "id" フィールドに設定
  void insert(ArticleEntity newArticle);
}
