package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.exception.UnauthorizedResourceAccessException;
import com.example.blog.service.user.UserEntity;
import java.util.List;
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
  private final DateTimeService dateTimeService;

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
   * 新しい記事を作成するメソッド。
   *
   * <p>このメソッドは以下の処理を行います:</p>
   * <ul>
   *   <li>現在のタイムスタンプを取得し、作成日時と更新日時として設定。</li>
   *   <li>記事データ（タイトル、本文、作成者ID）を基に新しい {@link ArticleEntity} を作成。</li>
   *   <li>記事データをデータベースに挿入。</li>
   *   <li>挿入後、データベースから作成した記事データを再取得し、返却。</li>
   * </ul>
   *
   * <p>トランザクション処理が適用されており、メソッドの処理が正常に完了しない場合は、すべての変更がロールバックされます。</p>
   *
   * <p>主な例外:</p>
   * <ul>
   *   <li>{@link IllegalStateException}: 記事データがデータベースから取得できない場合にスローされます。</li>
   * </ul>
   *
   * @param userId 作成者のユーザーID
   * @param title  記事のタイトル
   * @param body   記事の本文
   * @return 作成された記事の {@link ArticleEntity} オブジェクト
   */
  @Transactional
  public ArticleEntity create(long userId, String title, String body) {
    // 現在のタイムスタンプを取得
    var timestamp = dateTimeService.now();

    // 新しい記事エンティティを作成
    var newArticle = new ArticleEntity(
        null, // 新規作成のためIDはnull
        title,
        body,
        new UserEntity(userId, null, null, true),
        timestamp,
        timestamp // 更新日時（初期値は作成日時と同じ）
    );
    articleRepository.insert(newArticle);

    // 挿入された記事データを再取得して返却
    return articleRepository.selectById(newArticle.getId())
        .orElseThrow(() -> new IllegalStateException("never reached"));
  }

  public List<ArticleEntity> findAll() {
    return articleRepository.selectAll();
  }

  /**
   * 記事を更新するメソッド。
   *
   * <p>このメソッドは、指定された記事IDに対応する記事データを更新します。
   * 更新対象のリソースが存在しない場合、またはリソースの所有者ではないユーザーが 更新を試みた場合は、それぞれ例外をスローします。</p>
   *
   * <p>処理の流れ:</p>
   * <ol>
   *   <li>記事IDに対応する記事を取得。存在しない場合は {@link ResourceNotFoundException} をスロー。</li>
   *   <li>記事の所有者がログイン中のユーザーでない場合は {@link UnauthorizedResourceAccessException} をスロー。</li>
   *   <li>記事のタイトルと本文を更新し、現在日時を更新日時として設定。</li>
   *   <li>更新した内容をリポジトリを通じて保存。</li>
   *   <li>更新後のエンティティを返却。</li>
   * </ol>
   *
   * @param articleId      更新対象の記事のID
   * @param loggedInUserId 現在ログインしているユーザーのID
   * @param updatedTitle   更新後のタイトル
   * @param updatedBody    更新後の本文
   * @return 更新後の {@link ArticleEntity}
   * @throws ResourceNotFoundException           指定された記事が存在しない場合
   * @throws UnauthorizedResourceAccessException 記事の所有者でないユーザーが更新を試みた場合
   */
  @Transactional
  public ArticleEntity update(
      long articleId,
      long loggedInUserId,
      String updatedTitle,
      String updatedBody
  ) {
    // 指定された記事を取得、存在しない場合は例外をスロー
    var entity = findById(articleId)
        .orElseThrow(ResourceNotFoundException::new);

    // ログイン中のユーザーが記事の所有者でない場合は例外をスロー
    if (entity.getAuthor().getId() != loggedInUserId) {
      throw new UnauthorizedResourceAccessException();
    }

    // 記事データを更新
    entity.setTitle(updatedTitle);
    entity.setBody(updatedBody);
    entity.setUpdatedAt(dateTimeService.now()); // 現在日時を設定

    // 更新内容をリポジトリを通じて保存
    articleRepository.update(entity);

    // 更新後のエンティティを返却
    return entity;
  }
}
