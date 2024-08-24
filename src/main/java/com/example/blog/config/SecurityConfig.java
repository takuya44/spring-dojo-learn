package com.example.blog.config;

import com.example.blog.web.filter.CsrfCookieFilter;
import com.example.blog.web.filter.JsonUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // セキュリティの設定を定義するメソッド
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      SecurityContextRepository securityContextRepository,
      SessionAuthenticationStrategy sessionAuthenticationStrategy,
      AuthenticationManager authenticationManager) throws Exception {
    http
        .csrf((csrf) -> csrf
            // CSRFトークンをCookieに格納し、HttpOnly属性を無効にする設定
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // リクエスト属性にCSRFトークンを設定するハンドラを使用
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        )
        // CsrfCookieFilterをBasicAuthenticationFilterの後に追加
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        // カスタム認証フィルタを既存のUsernamePasswordAuthenticationFilterの位置に追加
        .addFilterAt(
            new JsonUsernamePasswordAuthenticationFilter(securityContextRepository,
                sessionAuthenticationStrategy, authenticationManager),
            UsernamePasswordAuthenticationFilter.class
        )
        .securityContext(context -> context.securityContextRepository(securityContextRepository))
        // 全てのリクエストに対して認証が必要であることを指定
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/").permitAll()
            .requestMatchers("/articles/**").permitAll()
            .anyRequest().authenticated()
        );

    // HttpSecurityビルダーを使用してセキュリティ設定を適用し、SecurityFilterChainを返す
    return http.build();
  }

  /**
   * 認証に使用する AuthenticationManager を構成する。 DaoAuthenticationProvider を使用して、ユーザー名とパスワードの認証を行う。
   *
   * @param passwordEncoder    パスワードエンコーダー
   * @param userDetailsService ユーザー情報を提供するサービス
   * @return 認証マネージャー
   */
  @Bean
  public AuthenticationManager authenticationManager(
      PasswordEncoder passwordEncoder, UserDetailsService userDetailsService
  ) {
    var provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder);
    provider.setUserDetailsService(userDetailsService);
    return new ProviderManager(provider);
  }

  @Bean
  public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    // 認証成功時にセッションIDを変更する：セッション固定攻撃を防ぐために使用
    return new ChangeSessionIdAuthenticationStrategy();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    // セキュリティコンテキストをHTTPセッションに保存
    // ユーザーが一度認証されると、その認証情報がセッションを通じて保持され、認証済みの状態が維持される
    return new HttpSessionSecurityContextRepository();
  }

  /**
   * ユーザー認証に使用するユーザー情報を提供するサービスを構成する。 メモリ内でユーザー名 "user" とパスワード "password" を管理する。
   *
   * @return ユーザー情報を提供するサービス
   */
  @Bean
  public UserDetailsService userDetailsService() {
    // シンプルなユーザー情報をメモリ内で管理するInMemoryUserDetailsManagerを使用
    // パスワードエンコーダーを使用して、"user"というユーザー名と"password"というパスワードを設定
    // 修正後は、user、passwordを使用しないとログインできない。
    UserDetails userDetails = User.builder()
        .username("user")
        .password("password")
        .roles("USER")
        .build();

    // InMemoryUserDetailsManagerにユーザー情報を設定して返す
    return new InMemoryUserDetailsManager(userDetails);
  }

  /**
   * パスワードエンコーダーを構成する。 NoOpPasswordEncoder を使用し、パスワードのエンコードは行わない。 （セキュリティ上の理由から、実運用では推奨されない）
   *
   * @return パスワードエンコーダー
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}

