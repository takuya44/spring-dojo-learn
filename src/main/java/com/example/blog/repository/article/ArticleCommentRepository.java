package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleCommentEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

/**
 * 記事のコメントをデータベースに保存するリポジトリインターフェース。
 *
 * <p>このインターフェースでは、MyBatis を使用して記事コメントのデータを操作する。</p>
 */
public interface ArticleCommentRepository {

  /**
   * 新しいコメントをデータベースに挿入する。
   *
   * <p>コメントは `article_comments` テーブルに保存され、ID はデータベースで自動生成される。</p>
   *
   * @param entity 挿入するコメントのエンティティ
   */
  @Insert("""
      INSERT INTO article_comments(body, user_id, article_id, created_at)
      VALUES (#{body}, #{author.id}, #{article.id}, #{createdAt});
      """)
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insert(ArticleCommentEntity entity);
}
