package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleCommentEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * 記事コメントのデータ操作を行うリポジトリインターフェース。
 *
 * <p>
 * MyBatis を用いて、記事コメントの挿入や取得を実装する。 結果マッピングは {@code __articleCommentResultMap()} で定義された結果マップを再利用する。
 * </p>
 */
@Mapper
public interface ArticleCommentRepository {

  String ARTICLE_COMMENT_RESULT_MAP_ID = "ARTICLE_COMMENT_RESULT_MAP_ID";

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

  /**
   * 指定された記事コメントIDに対応する記事コメントおよび関連情報を取得する。
   *
   * <p>
   * 取得対象の情報は以下の通り：
   * <ul>
   *   <li>記事コメント (ID、本文、作成日時)</li>
   *   <li>関連記事 (ID、タイトル、本文、作成日時、更新日時)</li>
   *   <li>記事著者 (ID、ユーザー名、有効状態)</li>
   *   <li>コメント著者 (ID、ユーザー名、有効状態)</li>
   * </ul>
   * </p>
   *
   * @param articleCommentId 記事コメントのID
   * @return 該当記事コメントのエンティティ（見つからなければ空）
   */
  @Select("""
              SELECT
                -- 記事コメントに関する情報
                  ac.id         AS article_comment__id
                , ac.body       AS article_comment__body
                , ac.created_at AS article_comment__created_at

                -- 関連する記事に関する情報
                , a.id          AS article__id
                , a.title       AS article__title
                , a.body        AS article__body
                , a.created_at  AS article__created_at
                , a.updated_at  AS article__updated_at

                -- 記事の著者に関する情報
                , aa.id         AS article_author__id
                , aa.username   AS article_author__username
                , aa.enabled    AS article_author__enabled

                -- コメントの著者に関する情報
                , ca.id         AS comment_author__id
                , ca.username   AS comment_author__username
                , ca.enabled    AS comment_author__enabled
              FROM article_comments ac
              JOIN articles a ON ac.article_id = a.id
              JOIN users aa ON a.user_id = aa.id
              JOIN users ca ON ac.user_id = ca.id
              WHERE ac.id = #{articleCommentId}
      """)
  @ResultMap(ARTICLE_COMMENT_RESULT_MAP_ID)
  Optional<ArticleCommentEntity> selectById(long articleCommentId);

  /**
   * 指定された記事IDに紐づく記事コメントおよび関連情報を一覧で取得する。
   *
   * <p>
   * 取得対象は以下の情報：
   * <ul>
   *   <li>記事コメント (ID、本文、作成日時)</li>
   *   <li>関連記事 (ID、タイトル、本文、作成日時、更新日時)</li>
   *   <li>記事著者 (ID、ユーザー名、有効状態)</li>
   *   <li>コメント著者 (ID、ユーザー名、有効状態)</li>
   * </ul>
   * 結果は記事コメントの作成日時およびIDの昇順でソートされる。
   * </p>
   *
   * @param articleId 対象記事のID
   * @return 対象記事に紐づく記事コメントの一覧
   */
  @Select("""
      SELECT
          ac.id         AS article_comment__id
        , ac.body       AS article_comment__body
        , ac.created_at AS article_comment__created_at

        , a.id          AS article__id
        , a.title       AS article__title
        , a.body        AS article__body
        , a.created_at  AS article__created_at
        , a.updated_at  AS article__updated_at

        , aa.id         AS article_author__id
        , aa.username   AS article_author__username
        , aa.enabled    AS article_author__enabled

        , ca.id         AS comment_author__id
        , ca.username   AS comment_author__username
        , ca.enabled    AS comment_author__enabled
        FROM article_comments ac
        JOIN articles a ON ac.article_id = a.id
        JOIN users aa ON a.user_id = aa.id
        JOIN users ca ON ac.user_id = ca.id
        WHERE a.id = #{articleId}
        ORDER BY ac.created_at, ac.id
      """)
  @ResultMap(ARTICLE_COMMENT_RESULT_MAP_ID)
  List<ArticleCommentEntity> selectByArticleId(Long articleId);

  /**
   * 統一した結果マッピング（ARTICLE_COMMENT_RESULT_MAP_ID）の定義用ダミーメソッド。
   *
   * <p>
   * このメソッドは実際のクエリ処理を行わず、@Results アノテーションにより、記事コメントに関する
   * 各種情報（記事コメント自体、関連する記事、記事の著者、コメントの著者）のフィールドマッピングを定義する。 定義した結果マッピングは、他のクエリメソッド（selectById,
   * selectByArticleId）で再利用される。
   * </p>
   *
   * @return ダミーの ArticleCommentEntity（返却値は利用されない）
   */
  @Select("SELECT '1'")
  @Results(id = ARTICLE_COMMENT_RESULT_MAP_ID, value = {
      // 記事コメントの情報
      @Result(column = "article_comment__id", property = "id"),
      @Result(column = "article_comment__body", property = "body"),
      @Result(column = "article_comment__created_at", property = "createdAt"),

      // 関連する記事の情報
      @Result(column = "article__id", property = "article.id"),
      @Result(column = "article__title", property = "article.title"),
      @Result(column = "article__body", property = "article.body"),
      @Result(column = "article__created_at", property = "article.createdAt"),
      @Result(column = "article__updated_at", property = "article.updatedAt"),

      // 記事の著者情報
      @Result(column = "article_author__id", property = "article.author.id"),
      @Result(column = "article_author__username", property = "article.author.username"),
      @Result(column = "article_author__enabled", property = "article.author.enabled"),

      // コメントの著者情報
      @Result(column = "comment_author__id", property = "author.id"),
      @Result(column = "comment_author__username", property = "author.username"),
      @Result(column = "comment_author__enabled", property = "author.enabled"),
  })
  ArticleCommentEntity __articleCommentResultMap();

}
