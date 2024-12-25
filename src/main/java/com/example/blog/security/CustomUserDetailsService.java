package com.example.blog.security;

import com.example.blog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * ユーザー名に基づいてユーザー情報をロードします。
   *
   * <p>このメソッドは Spring Security によって呼び出され、
   * 認証プロセスで使用するユーザー情報を返します。</p>
   *
   * <p>流れ:</p>
   * <ol>
   *   <li>指定されたユーザー名を基にデータベースからユーザー情報を検索。</li>
   *   <li>検索結果が存在する場合は {@link LoggedInUser} オブジェクトを作成。</li>
   *   <li>検索結果が存在しない場合は {@link UsernameNotFoundException} をスロー。</li>
   * </ol>
   *
   * @param username 認証のために使用されるユーザー名
   * @return ユーザー情報を含む {@link UserDetails} オブジェクト
   * @throws UsernameNotFoundException 指定されたユーザー名が見つからない場合
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // リポジトリからユーザー名を基にユーザー情報を検索
    return userRepository.selectByUsername(username)
        // 検索結果を LoggedInUser に変換
        .map(r -> new LoggedInUser(
                r.getId(), // ユーザーID
                r.getUsername(),
                r.getPassword(),
                r.isEnabled()
            )
        )
        // ユーザーが見つからない場合は例外をスロー
        .orElseThrow(() -> new UsernameNotFoundException(
            "given username is not found: username = " + username
        ));
  }
}
