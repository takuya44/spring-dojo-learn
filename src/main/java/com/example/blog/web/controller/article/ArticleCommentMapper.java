package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleCommentDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.service.article.ArticleCommentEntity;
import org.springframework.beans.BeanUtils;

/**
 * 記事コメントのエンティティを DTO に変換するマッパークラス。
 *
 * <p>このクラスは、`ArticleCommentEntity` を `ArticleCommentDTO` に変換し、
 * クライアントに適切なデータを提供する役割を担います。</p>
 *
 * <p>この変換処理により、エンティティ層（DB）と API レスポンスの分離を実現し、
 * セキュリティや設計の観点で適切なデータ構造を保持できます。</p>
 */
public class ArticleCommentMapper {

  /**
   * `ArticleCommentEntity` を `ArticleCommentDTO` に変換する。
   *
   * <p>このメソッドは以下の変換を行います:</p>
   * <ul>
   *   <li>コメントの基本情報をコピー（ID, 本文, 作成日時など）</li>
   *   <li>コメントの投稿者情報（`UserEntity`）を `UserDTO` に変換</li>
   * </ul>
   *
   * <p>`BeanUtils.copyProperties` を使用してプロパティをコピーすることで、
   * ボイラープレートコードを削減し、メンテナンス性を向上させています。</p>
   *
   * @param entity 変換対象の `ArticleCommentEntity`
   * @return 変換後の `ArticleCommentDTO`
   */
  public static ArticleCommentDTO toArticleDTO(ArticleCommentEntity entity) {
    // ユーザー情報を DTO に変換
    var userDTO = new UserDTO();
    BeanUtils.copyProperties(entity.getAuthor(), userDTO);

    // コメントデータを DTO に変換
    var commentDTO = new ArticleCommentDTO();
    BeanUtils.copyProperties(entity, commentDTO);
    commentDTO.setAuthor(userDTO); // 投稿者情報をセット

    return commentDTO;
  }
}
