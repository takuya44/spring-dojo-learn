package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 記事データを管理する MyBatis のリポジトリインターフェース。
 *
 * <p>このインターフェースは、データベース操作（記事の取得や挿入）を提供します。</p>
 *
 * <p>主な機能:</p>
 * <ul>
 *   <li>記事の一覧取得</li>
 *   <li>記事の詳細取得</li>
 *   <li>新規記事の挿入</li>
 * </ul>
 */
@Mapper
public interface ArticleRepository {

  /**
   * 記事のデータを取得するクエリ。
   *
   * <p>このクエリは、`articles` テーブルと `users` テーブルを結合し、記事の詳細とその作成者の情報を取得します。
   * また、指定された記事 ID がある場合は、その ID に基づくフィルタリングを行います。</p>
   *
   * @param articleId フィルタリングに使用する記事 ID（null の場合はすべての記事を取得）
   * @return 記事データのリスト
   */
  @Select("""
            <script>
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
              <where>
                <if test="articleId != null">
                  AND a.id = #{articleId}
                </if>
              </where>
              ORDER BY a.created_at DESC
            </script>
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
  List<ArticleEntity> __select(@Param("articleId") Long articleId);

  /**
   * 指定された記事 ID を基に、単一の記事を取得します。
   *
   * @param articleId 取得対象の記事 ID
   * @return 指定された ID に一致する記事データ（存在しない場合は空の {@link Optional} を返す）
   */
  default Optional<ArticleEntity> selectById(long articleId) {
    return __select(articleId).stream().findFirst();
  }

  /**
   * すべての記事を取得します。
   *
   * <p>記事は作成日時の降順で並び替えられます。</p>
   *
   * @return すべての記事のリスト
   */
  default List<ArticleEntity> selectAll() {
    return __select(null);
  }

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


  /**
   * 指定された記事を更新するための SQL クエリ。
   *
   * <p>このメソッドは、記事IDと作成者のユーザーIDに基づいて記事を検索し、
   * 該当する記事のタイトル、本文、および更新日時を変更します。</p>
   *
   * <p>処理の流れ:</p>
   * <ul>
   *   <li>指定された記事ID（`id`）と作成者のユーザーID（`author.id`）に一致するレコードを検索。</li>
   *   <li>該当するレコードが存在する場合、そのタイトル（`title`）、本文（`body`）、および更新日時（`updatedAt`）を変更。</li>
   *   <li>該当するレコードが存在しない場合は何も更新されない。</li>
   * </ul>
   *
   * @param entity 更新する記事データを格納した {@link ArticleEntity} オブジェクト
   */
  @Update("""
      UPDATE articles
      SET
          title      = #{title}
        , body       = #{body}
        , updated_at = #{updatedAt}
      WHERE id       = #{id}
        AND user_id  = #{author.id}
      """)
  void update(ArticleEntity entity);

  /**
   * 指定された ArticleEntity に基づいて、記事を削除する SQL DELETE ステートメントです。
   *
   * <p>
   * このメソッドは MyBatis のマッパーで使用され、次の条件を満たす記事を削除します:
   * </p>
   * <ul>
   *   <li>
   *     {@code id = #{id}} - 文章のIDが、ArticleEntity の {@code id} フィールドと一致する。
   *   </li>
   *   <li>
   *     {@code user_id = #{author.id}} - 文章の所有者（ユーザーID）が、ArticleEntity の {@code author.id} と一致する。
   *   </li>
   * </ul>
   *
   * <p>
   * これにより、指定された記事IDと所有者情報が正しい場合にのみ、記事が削除されることが保証されます。
   * 例えば、誤って他のユーザーの記事が削除されるのを防ぐために、この条件が利用されます。
   * </p>
   *
   * @param entity 削除対象の記事を表す ArticleEntity オブジェクト。 このオブジェクトの {@code id} と {@code author.id}
   *               が、削除の条件として利用されます。
   */
  @Delete("""
      DELETE FROM articles
      WHERE id = #{id}
        AND user_id = #{author.id}
      """)
  void delete(ArticleEntity entity);
}
