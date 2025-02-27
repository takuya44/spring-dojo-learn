package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ArticleCommentEntity {

  private Long id;
  private String body;
  private ArticleEntity articleEntity;
  private UserEntity author;
  private OffsetDateTime createdAt;

}
