package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class ArticleCommentService {

  public ArticleCommentEntity create(
      long userId,
      long articleId,
      @NotNull String body
  ) {
    // TODO :mock
    return new ArticleCommentEntity(
        null,
        body,
        new ArticleEntity(articleId, "", "", null, null, null),
        new UserEntity(userId, "", "", true),
        OffsetDateTime.now()
    );
  }
}
