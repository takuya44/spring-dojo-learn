package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleCommentRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.user.UserEntity;
import jakarta.validation.constraints.NotNull;
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

  private final ArticleCommentRepository articleCommentRepository;
  private final DateTimeService dateTimeService;

  /**
   * ユーザーが指定した記事にコメントを投稿するメソッドです。
   *
   * <p>
   * このメソッドは、ユーザーID、記事ID、及び必須のコメント本文を受け取り、 新規コメントエンティティを生成してデータベースに保存します。<br>
   * 保存後、DB側で自動生成されたIDやその他の付加情報を反映したエンティティを再取得し返却します。
   * </p>
   *
   * @param userId    コメント投稿者のユーザーID
   * @param articleId 対象記事のID
   * @param body      投稿するコメントの本文（null不可）
   * @return データベースに保存されたコメントエンティティ
   * @throws jakarta.validation.ConstraintViolationException {@code body} が null の場合に発生します
   */
  public ArticleCommentEntity create(
      long userId,
      long articleId,
      @NotNull String body
  ) {
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
}
