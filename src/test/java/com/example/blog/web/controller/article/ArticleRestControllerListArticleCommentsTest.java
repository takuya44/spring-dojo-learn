package com.example.blog.web.controller.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.service.article.ArticleCommentEntity;
import com.example.blog.service.article.ArticleCommentService;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArticleRestControllerListArticleCommentsTest
 * <p>
 * テスト対象:
 * <ul>
 *   <li>GET /articles/{articleId}/comments エンドポイント</li>
 * </ul>
 * <p>
 * 目的:
 * <ul>
 *   <li>指定した記事IDに関連付けられたコメント一覧を正しく取得できるか検証する</li>
 * </ul>
 * セットアップ:
 * <ul>
 *   <li>記事作成者を登録し、記事を作成</li>
 *   <li>2件のコメント作成者を登録し、それぞれコメントを作成</li>
 * </ul>
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerListArticleCommentsTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserService userService;
  @Autowired
  private ArticleService articleService;
  @Autowired
  private ArticleCommentService articleCommentService;

  private ArticleEntity article;
  private ArticleCommentEntity comment1;
  private ArticleCommentEntity comment2;

  /**
   * テスト前の共通セットアップ。
   * <p>
   * ・記事作成者を登録し、記事を作成する<br> ・2人のコメント作成者を登録し、それぞれの記事コメントを作成する<br> セットアップされた記事とコメントは、各テストメソッドで利用可能。
   * </p>
   */
  @BeforeEach
  void beforeEach() {
    // 記事作成者の登録と記事の作成
    var articleAuthor = userService.register("test_username1", "test_password1");
    article = articleService.create(
        articleAuthor.getId(),
        "test_article_title",
        "test_article_body"
    );

    // コメント作成者1の登録とコメントの作成
    var commentAuthor1 = userService.register("test_username2", "test_password2");
    // コメント作成者2の登録とコメントの作成
    var commentAuthor2 = userService.register("test_username3", "test_password3");
    comment1 = articleCommentService.create(commentAuthor1.getId(), article.getId(),
        "test_comment_body1");
    comment2 = articleCommentService.create(commentAuthor2.getId(), article.getId(),
        "test_comment_body2");
  }

  /**
   * DIコンテナから必要なコンポーネントが正しく注入されていることを確認するテスト。
   */
  @Test
  void setup() {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
    assertThat(articleService).isNotNull();
    assertThat(articleCommentService).isNotNull();
  }

  /**
   * GET /articles/{articleId}/comments テスト
   * <p>
   * 目的: 指定された記事IDに紐づくコメント一覧を取得できることを検証する。
   * <ol>
   *   <li>
   *     事前準備: {@link #beforeEach()} により、記事と2件のコメントが作成される。
   *   </li>
   *   <li>
   *     GETリクエスト送信: /articles/{articleId}/comments エンドポイントへJSON形式のリクエストを送信する。
   *   </li>
   *   <li>
   *     レスポンス検証:
   *     <ul>
   *       <li>HTTPステータス200 OK</li>
   *       <li>Content-Type が application/json</li>
   *       <li>レスポンスJSON内の comments 配列に、各コメントの id, body, author (id, username), createdAt が含まれている</li>
   *       <li>作成済みのコメントが、レスポンス内に正しい順序で含まれていること</li>
   *     </ul>
   *   </li>
   * </ol>
   * </p>
   *
   * @throws Exception リクエスト実行中に発生する例外
   */
  @Test
  @DisplayName("GET /articles/{articleId}/comments: 指定した記事IDに紐づくコメントの一覧を取得できる")
  void listArticleComments_200OK() throws Exception {
    // ## Arrange ##

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles/{articleId}/comments", article.getId())
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    // ・HTTPステータス200 OK
    // ・Content-Type が application/json
    // ・レスポンスJSON内の comments 配列の各要素が、事前に作成されたコメントの内容と一致する
    actual
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // 1件目のコメントの検証
        .andExpect(jsonPath("$.comments[0].id").value(comment1.getId()))
        .andExpect(jsonPath("$.comments[0].body").value(comment1.getBody()))
        .andExpect(jsonPath("$.comments[0].author.id").value(comment1.getAuthor().getId()))
        .andExpect(
            jsonPath("$.comments[0].author.username").value(comment1.getAuthor().getUsername()))
        .andExpect(jsonPath("$.comments[0].createdAt").value(comment1.getCreatedAt().toString()))
        // 2件目のコメントの検証
        .andExpect(jsonPath("$.comments[1].id").value(comment2.getId()))
        .andExpect(jsonPath("$.comments[1].body").value(comment2.getBody()))
        .andExpect(jsonPath("$.comments[1].author.id").value(comment2.getAuthor().getId()))
        .andExpect(
            jsonPath("$.comments[1].author.username").value(comment2.getAuthor().getUsername()))
        .andExpect(jsonPath("$.comments[1].createdAt").value(comment2.getCreatedAt().toString()))
    ;
  }

  /**
   * GET /articles/{articleId}/comments のテスト:
   * <p>
   * 指定された記事IDが存在しない場合、404 Not Found を返すことを検証するテスト。
   * </p>
   * <p>
   * 【テストの流れ】
   * <ol>
   *   <li><b>Arrange:</b>
   *     <ul>
   *       <li>存在しない記事ID (例: 0) を定義する。</li>
   *     </ul>
   *   </li>
   *   <li><b>Act:</b>
   *     <ul>
   *       <li>存在しない記事IDに対して GET リクエストを送信し、コメント一覧の取得を試みる。</li>
   *     </ul>
   *   </li>
   *   <li><b>Assert:</b>
   *     <ul>
   *       <li>レスポンスの HTTP ステータスが 404 であることを確認する。</li>
   *       <li>レスポンスの Content-Type が {@code MediaType.APPLICATION_PROBLEM_JSON} であることを検証する。</li>
   *       <li>JSON 内の {@code title}, {@code status}, {@code detail}, {@code instance} フィールドが期待通りの値になっていることを確認する。</li>
   *     </ul>
   *   </li>
   * </ol>
   * </p>
   *
   * @throws Exception リクエスト実行中に発生する例外
   */
  @Test
  @DisplayName("GET /articles/{articleId}/comments: 指定されたIDの記事が存在しないとき、404を返す")
  void listArticleComments_404NotFound() throws Exception {
    // ## Arrange ##
    // 存在しない記事IDを定義
    var invalidArticleId = 0;

    // ## Act ##
    var actual = mockMvc.perform(
        get("/articles/{articleId}/comments", invalidArticleId)
            .contentType(MediaType.APPLICATION_JSON)
    );

    // ## Assert ##
    actual
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("NotFound"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("リソースが見つかりません"))
        .andExpect(
            jsonPath("$.instance").value("/articles/%d/comments".formatted(invalidArticleId)))
    ;
  }
}