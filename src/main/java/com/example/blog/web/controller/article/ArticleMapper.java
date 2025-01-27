package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleDTO;
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

  ;
}
