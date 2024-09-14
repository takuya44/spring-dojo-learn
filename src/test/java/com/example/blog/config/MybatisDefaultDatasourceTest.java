package com.example.blog.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

/**
 * MyBatisのテストでデフォルトのデータソース設定を適用するためのカスタムメタアノテーション。
 *
 * <p>このアノテーションは、MyBatisを使用するテストで、特定のデータソースを使用するためのデフォルト設定を
 * 提供します。通常、MyBatis関連のテストにおいては、インメモリデータベースが自動的に設定されますが、
 * このカスタムアノテーションでは、`AutoConfigureTestDatabase.Replace.NONE` を指定することで、 実際のデータソースを使用する設定にしています。</p>
 *
 * <p>このアノテーションは以下のアノテーションをまとめています:
 * <ul>
 *   <li>{@link MybatisTest}: MyBatis関連のコンポーネントだけをロードし、軽量なテスト環境を提供します。</li>
 *   <li>{@link AutoConfigureTestDatabase#replace()}: デフォルトではSpringが自動的に設定するインメモリデータベースの使用を無効化し、
 *   実際のデータベース接続設定を使用します。</li>
 * </ul>
 * </p>
 *
 * <p>このカスタムアノテーションを使用することで、MyBatisを使ったテストにおけるデータベース設定の重複を避け、
 * 簡潔に定義できます。</p>
 *
 * <p>例:
 * <pre>{@code
 * @MybatisDefaultDatasourceTest
 * class MyBatisRepositoryTest {
 *   // テストメソッド
 * }
 * }</pre>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface MybatisDefaultDatasourceTest {

}
