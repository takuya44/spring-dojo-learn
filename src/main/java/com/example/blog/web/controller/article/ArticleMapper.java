package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleListItemDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.service.article.ArticleEntity;
import org.springframework.beans.BeanUtils;

/**
 * 記事エンティティとDTO間のマッピングを提供するユーティリティクラス。
 *
 * <p>このクラスは、エンティティオブジェクト（{@link ArticleEntity}）を
 * データ転送オブジェクト（DTO: {@link ArticleDTO}）に変換する静的メソッドを提供します。</p>
 */
public class ArticleMapper {

  /**
   * 記事エンティティをDTOに変換するメソッド。
   *
   * <p>このメソッドは、記事エンティティ（{@link ArticleEntity}）から必要なデータを抽出し、
   * {@link ArticleDTO} オブジェクトを生成します。また、記事の著者情報も {@link UserDTO} に変換して、DTOに含めます。</p>
   *
   * <p>使用例:</p>
   * <pre>{@code
   * ArticleEntity entity = articleService.findById(articleId).orElseThrow();
   * ArticleDTO dto = ArticleMapper.toArticleDTO(entity);
   * }</pre>
   *
   * @param entity 変換元の記事エンティティ
   * @return 変換された記事DTO
   */
  public static ArticleDTO toArticleDTO(ArticleEntity entity) {
    var userDTO = new UserDTO();
    BeanUtils.copyProperties(entity.getAuthor(), userDTO);

    var articleDTO = new ArticleDTO();
    BeanUtils.copyProperties(entity, articleDTO);
    articleDTO.setAuthor(userDTO);

    return articleDTO;
  }

  /**
   * 記事エンティティをリストアイテムDTOに変換するユーティリティメソッド。
   *
   * <p>このメソッドは、記事エンティティ（{@link ArticleEntity}）から必要なデータを抽出し、
   * リスト表示用のデータ転送オブジェクト（DTO: {@link ArticleListItemDTO}）に変換します。 記事の著者情報も {@link UserDTO}
   * に変換して、DTOに含めます。</p>
   *
   * <p>このメソッドは、記事一覧表示など、簡易的なデータが必要な場面で使用します。</p>
   *
   * <p>使用例:</p>
   * <pre>{@code
   * ArticleEntity entity = articleService.findById(articleId).orElseThrow();
   * ArticleListItemDTO listItemDTO = ArticleMapper.toArticleListItemDTO(entity);
   * }</pre>
   *
   * @param entity 変換元の記事エンティティ
   * @return 変換された記事リストアイテムDTO
   */
  public static ArticleListItemDTO toArticleListItemDTO(ArticleEntity entity) {
    // 著者情報をDTOに変換
    var userDTO = new UserDTO();
    BeanUtils.copyProperties(entity.getAuthor(), userDTO);

    // 記事データをリストアイテムDTOに変換
    var articleListItemDTO = new ArticleListItemDTO();
    BeanUtils.copyProperties(entity, articleListItemDTO);
    articleListItemDTO.setAuthor(userDTO);

    // 変換結果を返却
    return articleListItemDTO;
  }
}
