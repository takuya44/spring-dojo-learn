package com.example.blog.config;

import com.example.blog.web.filter.JsonUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // セキュリティの設定を定義するメソッド
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      SecurityContextRepository securityContextRepository,
      SessionAuthenticationStrategy sessionAuthenticationStrategy) throws Exception {
    http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/login"))
        // カスタム認証フィルタを既存のUsernamePasswordAuthenticationFilterの位置に追加
        .addFilterAt(
            new JsonUsernamePasswordAuthenticationFilter(securityContextRepository,
                sessionAuthenticationStrategy),
            UsernamePasswordAuthenticationFilter.class
        )
        .securityContext(context -> context.securityContextRepository(securityContextRepository))
        // 全てのリクエストに対して認証が必要であることを指定
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/articles/**").permitAll()
            .anyRequest().authenticated()
        )
        // フォームログインを有効化（デフォルト設定を使用）
        .formLogin(Customizer.withDefaults());

    // HttpSecurityビルダーを使用してセキュリティ設定を適用し、SecurityFilterChainを返す
    return http.build();
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

  // ユーザー認証に使用するユーザー情報を提供するメソッド
  @Bean
  public UserDetailsService userDetailsService() {
    // シンプルなユーザー情報をメモリ内で管理するInMemoryUserDetailsManagerを使用
    // デフォルトのパスワードエンコーダーを使用して、"user"というユーザー名と"password"というパスワードを設定
    UserDetails userDetails = User.withDefaultPasswordEncoder()
        .username("user")
        .password("password")
        .roles("USER")
        .build();

    // InMemoryUserDetailsManagerにユーザー情報を設定して返す
    return new InMemoryUserDetailsManager(userDetails);
  }
}

