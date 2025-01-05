package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.user.UserEntity;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 記事に関するビジネスロジックを提供するサービスクラス。
 *
 * <p>このクラスは、記事の検索や新規作成といった操作を担当します。</p>
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;

  /**
   * 指定されたIDの記事を検索します。
   *
   * <p>このメソッドは、指定された記事IDに対応する記事データをデータベースから取得します。
   * 該当する記事が見つからない場合は空の {@link Optional} を返します。</p>
   *
   * @param id 検索する記事のID
   * @return 指定されたIDの記事を含む {@link Optional} オブジェクト
   */
  public Optional<ArticleEntity> findById(long id) {
    return articleRepository.selectById(id);
  }

  /**
   * 新しい記事を作成します。
   *
   * <p>このメソッドは、指定されたユーザーID、タイトル、および本文を使用して新しい記事を作成し、データベースに保存します。
   * 作成日時と更新日時は現在のタイムスタンプが使用されます。</p>
   *
   * <p>この操作はトランザクション内で実行され、データ整合性が保証されます。</p>
   *
   * @param userId 記事を作成するユーザーのID
   * @param title  記事のタイトル
   * @param body   記事の内容
   * @return 作成された {@link ArticleEntity} オブジェクト
   */
  @Transactional
  public ArticleEntity create(long userId, String title, String body) {
    var timestamp = OffsetDateTime.now();
    var newArticle = new ArticleEntity(
        null, // 新規作成のためIDはnull
        title,
        body,
        new UserEntity(userId, null, null, true),
        timestamp,
        timestamp
    );
    articleRepository.insert(newArticle);
    return newArticle;
  }
}
