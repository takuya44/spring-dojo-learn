package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 記事を表すエンティティクラス。
 * <p>
 * このクラスは、記事の基本情報を保持します。
 * </p>
 *
 * <ul>
 *   <li>記事のID</li>
 *   <li>記事のタイトル</li>
 *   <li>記事の内容</li>
 *   <li>作成者（著者）</li>
 *   <li>作成日時</li>
 *   <li>更新日時</li>
 * </ul>
 *
 * <p>このエンティティは、記事に関連するデータを操作する際に使用されます。</p>
 *
 * @param id        記事のID
 * @param title     記事のタイトル
 * @param body      記事の内容
 * @param author    記事の著者を表す {@link UserEntity}
 * @param createdAt 記事の作成日時
 * @param updatedAt 記事の更新日時
 */
@AllArgsConstructor
@Data
public class ArticleEntity {

  private Long id;
  private String title;
  private String body;
  private UserEntity author; // 作成者（著者）の情報
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
