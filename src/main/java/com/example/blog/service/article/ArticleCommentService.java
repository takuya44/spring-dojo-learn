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
   * 記事にコメントを追加する。
   *
   * <p>指定された記事 ID に対して、指定されたユーザーがコメントを投稿する処理を行う。</p>
   *
   * @param userId    コメントを投稿するユーザーのID
   * @param articleId コメントを投稿する対象の記事ID
   * @param body      コメントの内容（必須）
   * @return 作成されたコメントのエンティティ
   * @throws jakarta.validation.ConstraintViolationException body が null の場合にスローされる
   */
  public ArticleCommentEntity create(
      long userId,
      long articleId,
      @NotNull String body
  ) {
    // 新しいコメントのエンティティを作成
    var newComment = new ArticleCommentEntity(
        null, // ID はデータベースで自動生成
        body, // コメント本文
        new ArticleEntity(articleId, "", "", null, null, null),
        new UserEntity(userId, "", "", true),
        dateTimeService.now()
    );

    // コメントをデータベースに挿入
    articleCommentRepository.insert(newComment);

    // 作成したコメントを返却
    return newComment;
  }
}
