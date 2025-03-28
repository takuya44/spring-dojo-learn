package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleCommentRepository;
import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 記事のコメントを管理するサービスクラス。
 *
 * <p>このクラスでは、記事のコメントの作成処理を提供する。</p>
 */
@Service
@RequiredArgsConstructor
public class ArticleCommentService {

  private final ArticleRepository articleRepository;
  private final ArticleCommentRepository articleCommentRepository;
  private final DateTimeService dateTimeService;

  /**
   * 指定された記事IDに対して、ユーザーがコメントを投稿する処理を実施します。
   *
   * <p>
   * このメソッドでは、まず指定された記事IDに紐付く記事が存在するかを確認します。 存在しない場合は早期に {@link ResourceNotFoundException}
   * をスローすることで、 後続のinsert処理でデータベースの外部キー制約違反を発生させる前にエラーパターンを検出します。
   * これにより、エラーハンドリングが容易になり、問題の原因が明確になります。
   * </p>
   *
   * @param userId    コメント投稿者のユーザーID
   * @param articleId コメントを投稿する対象の記事ID
   * @param body      コメントの内容（null不可）
   * @return 作成された記事コメントエンティティ
   * @throws ResourceNotFoundException 指定された記事IDに対応する記事が存在しない場合にスローされる
   */
  public ArticleCommentEntity create(
      long userId,
      long articleId,
      @NotNull String body
  ) {
    // 早期に親リソースである記事の存在を確認し、存在しない場合はResourceNotFoundExceptionをスロー
    articleRepository.selectById(articleId)
        .orElseThrow(ResourceNotFoundException::new);

    // --- 新規コメントエンティティの生成 ---
    // 関連するArticleEntityおよびUserEntityは、IDのみを設定してインスタンス化
    // これにより、リレーションを表現するための最小限の情報のみを渡す設計
    var newComment = new ArticleCommentEntity(
        null, // IDはDBで自動生成されるため、ここではnull
        body, // コメント本文（ユーザーからの入力）
        new ArticleEntity(articleId, "", "", null, null, null),
        new UserEntity(userId, "", "", true),
        dateTimeService.now()
    );

    // コメントをデータベースに挿入
    articleCommentRepository.insert(newComment);

    // 作成したコメントを返却
    return articleCommentRepository
        .selectById(newComment.getId())
        .orElseThrow(() -> new IllegalStateException("never reached"));
  }

  /**
   * 指定された記事IDに紐づく記事コメントの一覧を取得する。
   *
   * <p>
   * このメソッドは、articleCommentRepository.selectByArticleId(articleId) を呼び出し、
   * 対象記事に関連する全てのコメントエンティティを返す。取得結果は、クエリ側で昇順にソートされる。
   * </p>
   *
   * @param articleId 対象記事のID
   * @return 対象記事に紐づく記事コメントのリスト
   */
  public List<ArticleCommentEntity> findByArticleId(Long articleId) {
    return articleCommentRepository.selectByArticleId(articleId);
  }
}
