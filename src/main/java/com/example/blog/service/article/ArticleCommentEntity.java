package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 記事のコメントを表すエンティティクラス。
 *
 * <p>このクラスは、記事に紐づくコメントデータを保持し、
 * データベースとのマッピングを意識したオブジェクト設計となっています。</p>
 *
 * <p>フィールド:</p>
 * <ul>
 *   <li>{@code id} - コメントの一意な識別子（データベースの主キー）</li>
 *   <li>{@code body} - コメントの本文</li>
 *   <li>{@code articleEntity} - コメントが紐づく記事の情報</li>
 *   <li>{@code author} - コメントを作成したユーザーの情報</li>
 *   <li>{@code createdAt} - コメントの作成日時（タイムスタンプ）</li>
 * </ul>
 *
 * <p>アノテーション:</p>
 * <ul>
 *   <li>{@link NoArgsConstructor} - 引数なしのデフォルトコンストラクタを自動生成</li>
 *   <li>{@link AllArgsConstructor} - すべてのフィールドを引数に持つコンストラクタを自動生成</li>
 *   <li>{@link Data} - ゲッター、セッター、toString、equals、hashCode を自動生成</li>
 * </ul>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ArticleCommentEntity {

  /**
   * コメントのID（データベースの主キー）
   */
  private Long id;

  /**
   * コメントの本文
   */
  private String body;

  /**
   * コメントが紐づく記事のエンティティ
   */
  private ArticleEntity articleEntity;

  /**
   * コメントを作成したユーザーのエンティティ
   */
  private UserEntity author;

  /**
   * コメントの作成日時
   */
  private OffsetDateTime createdAt;

}
