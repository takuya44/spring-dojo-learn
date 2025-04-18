package com.example.blog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.exception.UnauthorizedResourceAccessException;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

/**
 * {@link ArticleService} のテストクラス。
 *
 * <p>このクラスでは、記事作成サービスの動作を検証します。具体的には、articlesテーブルへのレコード挿入が
 * 正常に行われるかを確認します。</p>
 *
 * <p>使用するアノテーション:</p>
 * <ul>
 *   <li>{@link MybatisDefaultDatasourceTest}: MyBatisとデフォルトデータソース設定を使用したテスト環境を構築。</li>
 *   <li>{@link Import}: {@link ArticleService} をテスト環境にインポートしてテスト対象に指定。</li>
 * </ul>
 */
@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceTest {

  @Autowired
  private ArticleService cut;
  @Autowired
  private UserRepository userRepository;
  @MockBean
  private DateTimeService mockDateTimeService;
  @Autowired
  ArticleRepository articleRepository;

  /**
   * {@link ArticleService} と {@link UserRepository} が正しく初期化されていることを確認します。
   * <p>このテストはテスト環境のセットアップ確認用です。</p>
   */
  @Test
  void setup() {
    // テスト対象のインスタンスがnullでないことを検証
    assertThat(cut).isNotNull();
    assertThat(userRepository).isNotNull();
  }

  /**
   * 記事作成処理の動作を検証するテスト。
   *
   * <p>このテストでは、以下を検証します:</p>
   * <ul>
   *   <li>articlesテーブルに新しいレコードが挿入されること。</li>
   *   <li>挿入された記事データが正しい値を持つこと。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>テスト用のユーザーを準備してデータベースに挿入。</li>
   *   <li>作成する記事のタイトルと本文を準備。</li>
   *   <li>記事作成サービスを呼び出し、新しい記事を作成。</li>
   *   <li>作成された記事が期待通りのデータを持っていることを検証。</li>
   * </ol>
   */
  @Test
  @DisplayName("create: articles テーブルにレコードが insert される")
  void create_success() {
    // ## Arrange ##
    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user1"); // ユーザー名を設定
    expectedUser.setPassword("test_password1"); // パスワードを設定
    expectedUser.setEnabled(true); // 有効なユーザーであることを示す
    // ユーザーをデータベースに挿入
    userRepository.insert(expectedUser);

    // 日付を固定：この値がDBに登録される
    var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

    // 記事のタイトルと本文を準備
    var expectedTitle = "test_article_title";
    var expectedBody = "test_article_body";

    // ## Act ##
    // 記事作成サービスを呼び出し、新しい記事を作成
    var actual = cut.create(expectedUser.getId(), expectedTitle, expectedBody);

    // ## Assert ##
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getTitle()).isEqualTo(expectedTitle);
    assertThat(actual.getBody()).isEqualTo(expectedBody);
    assertThat(actual.getAuthor()).satisfies(user -> {
      assertThat(user.getId()).isEqualTo(expectedUser.getId());
      assertThat(user.getUsername()).isEqualTo(expectedUser.getUsername());
      assertThat(user.getPassword()).isNull(); // パスワードはnullであるべき（セキュリティ対策）
      assertThat(user.isEnabled()).isEqualTo(expectedUser.isEnabled());
    });
    assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
    assertThat(actual.getUpdatedAt()).isEqualTo(actual.getCreatedAt()); // 作成日時と更新日時が一致していることを確認
  }

  /**
   * 記事が存在しない場合の findAll メソッドの動作をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>テーブルに記事データが存在しない場合、findAll メソッドが空のリストを返すこと。</li>
   *   <li>エラーが発生せず、正常にメソッドが動作すること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テストの実行前に {@code DELETE FROM articles;} によってテーブルがクリアされている。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("findAll: 記事が存在しないとき、空のリストが取得できる")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void findAll_returnEmptyList() {
    // ## Arrange ## SQlで実行済み

    // ## Act ##
    var actual = cut.findAll(); // 記事がない状態で findAll を呼び出す

    // ## Assert ##
    assertThat(actual).isEmpty(); // 空のリストが返されることを確認
  }

  /**
   * 記事が存在する場合の findAll メソッドの動作をテストします。
   *
   * <p>このテストでは、以下を確認します:</p>
   * <ul>
   *   <li>テーブルに複数の記事が存在する場合、findAll メソッドが正しい順序でそれらの記事を返すこと。</li>
   *   <li>返されるリストのサイズが記事の件数と一致すること。</li>
   *   <li>返される記事の内容が期待値と一致すること。</li>
   * </ul>
   *
   * <p>前提条件:</p>
   * <ul>
   *   <li>テスト開始時にテーブルが空である（{@code DELETE FROM articles;} を実行済み）。</li>
   *   <li>新規ユーザーを登録し、そのユーザーに紐づけて記事を作成する。</li>
   * </ul>
   *
   * @throws Exception テスト実行中に発生する例外
   */
  @Test
  @DisplayName("findAll: 記事が存在するとき、リストを返す")
  @Sql(statements = {
      "DELETE FROM articles;"
  })
  void findAll_returnMultipleArticle() {
    // ## Arrange ##
    // 固定された日時を使用
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2022, 1, 1, 10, 10))
        .thenReturn(TestDateTimeUtil.of(2022, 2, 2, 10, 20));

    // ユーザーを作成
    var user1 = new UserEntity();
    user1.setUsername("test_username1");
    user1.setPassword("test_password1");
    user1.setEnabled(true);
    userRepository.insert(user1); // ユーザーをデータベースに登録

    // 記事を作成
    var expectedArticle1 = cut.create(user1.getId(), "test_title1", "test_body1");
    var expectedArticle2 = cut.create(user1.getId(), "test_title2", "test_body2");

    // ## Act ##
    // 記事をすべて取得
    var actual = cut.findAll();

    // ## Assert ##
    // リストサイズを検証
    assertThat(actual).hasSize(2);

    // 記事の順序と内容を検証
    assertThat(actual.get(0)).isEqualTo(expectedArticle2); // 新しい記事が先頭にくる
    assertThat(actual.get(1)).isEqualTo(expectedArticle1);
  }

  /**
   * update_success: 記事の更新に成功することを検証するテストです。
   *
   * <p>このテストでは、以下の点を検証しています:</p>
   * <ul>
   *   <li>更新前と更新後で、記事のタイムスタンプが正しく設定されること</li>
   *   <li>更新後のタイトルと本文が期待通りに反映されること</li>
   *   <li>記事作成時の作成日時は変わらず、更新日時のみが更新されること</li>
   *   <li>記事の作者情報が正しく保持され、セキュリティ上の理由からパスワード情報は返されないこと</li>
   *   <li>更新後のデータがデータベースにも正しく反映されていること</li>
   * </ul>
   *
   * <p>テストの手順:</p>
   * <ol>
   *   <li>固定の日時をモックし、更新前と更新後で異なる日時を返すように設定する。</li>
   *   <li>テスト用のユーザーを作成し、データベースに登録する。</li>
   *   <li>作成済みの記事を生成し、その記事のタイトルと本文を更新する。</li>
   *   <li>更新された記事オブジェクトのフィールド値と、データベース上のレコードが期待値と一致することを検証する。</li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("update: 記事の更新に成功する")
  void update_success() {
    // ## Arrange ##
    // 更新処理で使用される期待の更新日時を設定します。
    // ここでは、最初の呼び出しで更新前の日時（1日前）を返し、
    // 2回目の呼び出しで更新後の日時として expectedUpdatedAt を返すようにモックしています。
    var expectedUpdatedAt = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now())
        .thenReturn(expectedUpdatedAt.minusDays(1))
        .thenReturn(expectedUpdatedAt);

    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user1"); // ユーザー名を設定
    expectedUser.setPassword("test_password1"); // パスワードを設定
    expectedUser.setEnabled(true); // 有効なユーザーであることを示す
    userRepository.insert(expectedUser); // ユーザーをデータベースに挿入

    // 作成済みの記事を生成します。ここでは、上記で登録したユーザーが作者となります。
    var existingArticle = cut.create(expectedUser.getId(), "test_article_title",
        "test_article_body");
    var expectedTitle = "updated_title"; // 更新後に設定するタイトル
    var expectedBody = "updated_body"; // 更新後に設定する本文

    // ## Act ##
    // update() メソッドを呼び出し、記事のタイトルと本文を更新します。
    var actual = cut.update(existingArticle.getId(), expectedUser.getId(), expectedTitle,
        expectedBody);

    // ## Assert ##
    // 返された記事オブジェクトのフィールド値が、期待される値と一致していることを検証します。
    // ・IDは変わらず、作成日時はそのままで、更新日時が expectedUpdatedAt に更新されていること。
    // ・タイトルと本文がそれぞれ更新後の値に変更されていること。
    // ・作者情報が正しく保持され、パスワード情報は返されない（null）こと。
    // assert return value of ArticleService#update
    assertThat(actual.getId()).isEqualTo(existingArticle.getId());
    assertThat(actual.getTitle()).isEqualTo(expectedTitle); // タイトルが更新されていること
    assertThat(actual.getBody()).isEqualTo(expectedBody); // 本文が更新されていること
    assertThat(actual.getCreatedAt()).isEqualTo(existingArticle.getCreatedAt());
    assertThat(actual.getUpdatedAt()).isEqualTo(expectedUpdatedAt); // 更新日時が正しく設定されること
    assertThat(actual.getAuthor().getId()).isEqualTo(expectedUser.getId());
    assertThat(actual.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
    assertThat(actual.getAuthor().getPassword()).isNull(); // パスワード情報はセキュリティのため返されない
    assertThat(actual.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());

    // assert record in articles table：DBに登録されている記事レコードが、更新後の情報に一致しているかも検証します。
    var actualRecordOpt = articleRepository.selectById(existingArticle.getId());
    assertThat(actualRecordOpt).hasValueSatisfying(actualRecord -> {
      assertThat(actualRecord.getId()).isEqualTo(existingArticle.getId());
      assertThat(actualRecord.getTitle()).isEqualTo(expectedTitle); // タイトルが更新されていること
      assertThat(actualRecord.getBody()).isEqualTo(expectedBody); // 本文が更新されていること
      assertThat(actualRecord.getCreatedAt()).isEqualTo(existingArticle.getCreatedAt());
      assertThat(actualRecord.getUpdatedAt()).isEqualTo(expectedUpdatedAt); // 更新日時が一致すること
      assertThat(actualRecord.getAuthor().getId()).isEqualTo(expectedUser.getId());
      assertThat(actualRecord.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
      assertThat(actualRecord.getAuthor().getPassword()).isNull(); // パスワード情報は返されないこと
      assertThat(actualRecord.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());
    });
  }

  /**
   * update_throwResourceNotFoundException: 指定された記事 ID に該当する記事が存在しない場合、update 処理が
   * ResourceNotFoundException を throw することを検証するテストです。
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>存在しない記事 ID を指定した場合、update メソッドが ResourceNotFoundException を発生させること。</li>
   *   <li>テスト用のユーザー情報は正常に作成され、データベースに登録されていること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>存在しない記事 ID（invalidArticleId）を定義する。</li>
   *   <li>テスト用のユーザー（expectedUser）を作成し、データベースに挿入する。</li>
   *   <li>存在しない記事 ID を使用して update メソッドを呼び出し、ResourceNotFoundException が throw されることを検証する。</li>
   * </ol>
   */
  @Test
  @DisplayName("update: 指定された ID の記事が見つからないとき ResourceNotFoundException を throw する")
  void update_throwResourceNotFoundException() {
    // ## Arrange ##
    // 存在しない記事 ID を定義します。ここでは 0L を使用しています。
    var invalidArticleId = 0L;

    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user"); // ユーザー名を設定
    expectedUser.setPassword("test_password"); // パスワードを設定
    expectedUser.setEnabled(true); // 有効なユーザーであることを示す
    userRepository.insert(expectedUser); // ユーザーをデータベースに挿入

    // ## Act & Assert ##
    // 存在しない記事 ID を指定して update() メソッドを呼び出し、
    // ResourceNotFoundException が throw されることを検証
    assertThrows(ResourceNotFoundException.class, () -> {
      cut.update(invalidArticleId, expectedUser.getId(), "updated_title", "updated_body");
    });
  }

  /**
   * update_throwUnauthorizedResourceAccessException:
   * <p>
   * 自分以外が作成した記事を編集しようとした場合、update 処理が UnauthorizedResourceAccessException を throw することを検証するテストです。
   * </p>
   *
   * <p>このテストでは、以下の点を確認します:</p>
   * <ul>
   *   <li>記事作成者と異なるユーザーが記事の更新を試みた場合、権限がないとして例外が発生すること。</li>
   *   <li>正しいユーザー情報および記事データが用意され、記事の更新処理が実行される前提条件が整っていること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     <strong>Arrange:</strong>
   *     <ul>
   *       <li>
   *         更新処理で使用される日時を固定し、モックからその日時を返すように設定します（ここでは expectedUpdatedAt として 2020/1/2 10:20）。
   *       </li>
   *       <li>
   *         記事の作成者となるユーザー（author）を生成し、必要な情報（ユーザー名、パスワード、アクティブ状態）を設定後、データベースに挿入します。
   *       </li>
   *       <li>
   *         上記のユーザーを用いて記事を作成し、更新対象となる記事（existingArticle）を生成します。
   *       </li>
   *       <li>
   *         別のユーザー（otherUser）を生成し、同様に必要な情報を設定後、データベースに挿入します。こちらが記事更新を試みるユーザーです。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     <strong>Act & Assert:</strong>
   *     <ul>
   *       <li>
   *         存在する記事に対して、作成者とは異なるユーザー（otherUser）の ID を指定して更新処理（cut.update）を実行します。
   *       </li>
   *       <li>
   *         この際、更新処理が UnauthorizedResourceAccessException を throw することを assertThrows により検証します。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("update: 自分以外が作成した記事を編集しようとしたとき UnauthorizedResourceAccessException を throw する")
  void update_throwUnauthorizedResourceAccessException() {
    // ## Arrange ##
    // 更新処理で使用される期待の更新日時を設定します。
    var expectedUpdatedAt = TestDateTimeUtil.of(2020, 1, 2, 10, 20);
    when(mockDateTimeService.now())
        .thenReturn(expectedUpdatedAt);

    // 作成者ユーザーを生成し、必要な情報を設定した上でデータベースに登録します。
    var author = new UserEntity();
    author.setUsername("test_user"); // 作成者のユーザー名を設定
    author.setPassword("test_password"); // パスワードを設定
    author.setEnabled(true); // 有効なユーザーであることを示す
    userRepository.insert(author); // データベースに作成者ユーザーを挿入

    // 作成者ユーザーを用いて記事を作成します。
    var existingArticle = cut.create(author.getId(), "test_title", "test_body");

    // 更新を試みる別のユーザーを生成し、データベースに登録します。
    var otherUser = new UserEntity();
    otherUser.setUsername("other_user"); // 別のユーザー名を設定
    otherUser.setPassword("other_password"); // 別のパスワードを設定
    otherUser.setEnabled(true); // ユーザーが有効であることを示す
    userRepository.insert(otherUser); // データベースに別のユーザーを挿入

    // ## Act & Assert ##
    // 存在する記事に対し、作成者とは異なるユーザー (otherUser) の ID を指定して更新処理を実行し、
    // UnauthorizedResourceAccessException が throw されることを検証します。
    assertThrows(UnauthorizedResourceAccessException.class, () -> {
      cut.update(existingArticle.getId(), otherUser.getId(), "updated_title", "updated_body");
    });
  }

  /**
   * delete_success: 記事の削除に成功することを検証するテストです。
   *
   * <p>このテストでは、以下の点を検証します:</p>
   * <ul>
   *   <li>ユーザーが作成した記事が、削除処理によってデータベースから確実に削除されること。</li>
   *   <li>削除後、データベースから該当記事を取得しようとすると、空の結果 (Optional.empty()) が返されること。</li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>テスト用の日時を固定するため、mockDateTimeService に期待の日時を返すよう設定します。</li>
   *       <li>テスト用のユーザー (author) を生成し、データベースに登録します。</li>
   *       <li>author を用いて記事 (existingArticle) を作成し、データベースに登録します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act:
   *     <ul>
   *       <li>サービス層の delete メソッドを呼び出し、author の ID と existingArticle の ID を引数として記事削除処理を実行します。</li>
   *     </ul>
   *   </li>
   *   <li>
   *     Assert:
   *     <ul>
   *       <li>記事削除後、articleRepository を用いて対象の記事を取得し、結果が空であることを確認します。</li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("delete: 記事の削除に成功する")
  void delete_success() {
    // ## Arrange ##
    // テスト用日時を固定：記事削除処理時に使用する日時をモックで設定
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20));

    // ユーザー情報を生成し、データベースに登録する
    var author = new UserEntity();
    author.setUsername("test_user1");     // ユーザー名の設定
    author.setPassword("test_password1"); // パスワードの設定
    author.setEnabled(true);              // ユーザーが有効であることを示す
    userRepository.insert(author);        // ユーザーをデータベースに登録

    // 作成済みの記事を生成します。ここでは、上記で登録したユーザーが作者となります。
    var existingArticle = cut.create(author.getId(), "test_article_title",
        "test_article_body");

    // ## Act ##
    // サービス層の delete メソッドを呼び出し、記事の削除処理を実行する
    // 引数として、ユーザーID (author.getId()) と記事ID (existingArticle.getId()) を指定する
    cut.delete(author.getId(), existingArticle.getId());

    // ## Assert ##
    // データベースから削除対象の記事を取得しようとする
    // 取得結果が空 (Optional.empty()) であれば、記事が正常に削除されたことを確認できる
    var actual = articleRepository.selectById(existingArticle.getId());
    assertThat(actual).isEmpty();
  }

  /**
   * delete: 指定された ID の記事が見つからないとき、ResourceNotFoundException を throw することを検証するテストです。
   *
   * <p>このテストでは、以下の点を検証します:</p>
   * <ul>
   *   <li>
   *     存在しない記事 ID を指定した場合、サービス層の delete メソッドが ResourceNotFoundException をスローすること。
   *   </li>
   *   <li>
   *     テスト用のユーザー情報が正しくデータベースに登録され、削除処理が試みられる前提条件が整っていること。
   *   </li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>
   *         存在しない記事 ID として 0L を定義します。
   *       </li>
   *       <li>
   *         テスト用のユーザー (expectedUser) を生成し、必要な情報（ユーザー名、パスワード、アクティブ状態）を設定後、
   *         データベースに登録します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act & Assert:
   *     <ul>
   *       <li>
   *         存在しない記事 ID を指定して delete() メソッドを呼び出し、ResourceNotFoundException がスローされることを
   *         assertThrows を用いて検証します。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   */
  @Test
  @DisplayName("delete: 指定された ID の記事が見つからないとき、ResourceNotFoundException を throw する")
  void delete_throwResourceNotFoundException() {
    // ## Arrange ##
    // 存在しない記事 ID を定義します。ここでは 0L を使用しています。
    var invalidArticleId = 0L;

    // ユーザー情報を準備してデータベースに挿入
    var expectedUser = new UserEntity();
    expectedUser.setUsername("test_user");     // ユーザー名を設定
    expectedUser.setPassword("test_password"); // パスワードを設定
    expectedUser.setEnabled(true);             // 有効なユーザーであることを示す
    userRepository.insert(expectedUser);       // ユーザーをデータベースに挿入

    // ## Act & Assert ##
    // 存在しない記事 ID を指定して delete() メソッドを呼び出し、
    // ResourceNotFoundException がスローされることを assertThrows により検証します。
    assertThrows(ResourceNotFoundException.class, () -> {
      cut.delete(expectedUser.getId(), invalidArticleId);
    });
  }

  /**
   * delete: 他人の記事を削除しようとしたとき、UnauthorizedResourceAccessException を throw することを検証するテストです。
   *
   * <p>このテストでは、以下の点を検証します:</p>
   * <ul>
   *   <li>
   *     作成者とは異なるユーザーが記事の削除を試みた場合、削除処理が UnauthorizedResourceAccessException をスローすること。
   *   </li>
   *   <li>
   *     記事の作成者 (author) および削除対象の記事 (existingArticle) が正しく設定され、削除を試みる別のユーザー (otherUser) が
   *     適切にデータベースに登録されていること。
   *   </li>
   * </ul>
   *
   * <p>テストの流れ:</p>
   * <ol>
   *   <li>
   *     Arrange:
   *     <ul>
   *       <li>
   *         モックされた日付サービス (mockDateTimeService) で期待する日時を設定し、記事の作成時刻を固定します。
   *       </li>
   *       <li>
   *         作成者ユーザー (author) を生成し、必要な情報（ユーザー名、パスワード、有効フラグ）を設定後、データベースに登録します。
   *       </li>
   *       <li>
   *         作成者ユーザーを用いて記事 (existingArticle) を生成・登録します。
   *       </li>
   *       <li>
   *         削除を試みるため、作成者とは異なるユーザー (otherUser) を生成し、データベースに登録します。
   *       </li>
   *     </ul>
   *   </li>
   *   <li>
   *     Act & Assert:
   *     <ul>
   *       <li>
   *         作成者ではないユーザー (otherUser) の ID を指定して、記事削除処理 (cut.delete) を実行し、
   *         UnauthorizedResourceAccessException がスローされることを assertThrows で検証します。
   *       </li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @throws Exception テスト実行中に例外が発生した場合
   */
  @Test
  @DisplayName("delete: 他人の記事を削除しようとしたとき UnauthorizedResourceAccessException を throw する")
  void delete_throwUnauthorizedResourceAccessException() {
    // ## Arrange ##
    when(mockDateTimeService.now())
        .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20));

    // 作成者ユーザーを生成し、必要な情報を設定した上でデータベースに登録します。
    var author = new UserEntity();
    author.setUsername("test_user");     // 作成者のユーザー名を設定
    author.setPassword("test_password"); // パスワードを設定
    author.setEnabled(true);             // 有効なユーザーであることを示す
    userRepository.insert(author);       // データベースに作成者ユーザーを挿入

    // 作成者ユーザーを用いて記事を作成します。
    var existingArticle = cut.create(author.getId(), "test_title", "test_body");

    // 更新を試みる別のユーザーを生成し、データベースに登録します。
    var otherUser = new UserEntity();
    otherUser.setUsername("other_user");     // 別のユーザー名を設定
    otherUser.setPassword("other_password"); // 別のパスワードを設定
    otherUser.setEnabled(true);              // ユーザーが有効であることを示す
    userRepository.insert(otherUser);        // データベースに別のユーザーを挿入

    // ## Act & Assert ##
    // 存在する記事に対し、作成者とは異なるユーザー (otherUser) の ID を指定して削除処理を実行し、
    // UnauthorizedResourceAccessException が throw されることを検証します。
    assertThrows(UnauthorizedResourceAccessException.class, () -> {
      cut.delete(otherUser.getId(), existingArticle.getId());
    });
  }
}