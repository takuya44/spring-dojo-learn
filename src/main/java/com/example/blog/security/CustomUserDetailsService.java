package com.example.blog.security;

import com.example.blog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.selectByUsername(username)
        .map(r -> User.builder()
            .username(r.username())
            .password(r.password())
            .disabled(!r.enabled())
            .build()
        )
        .orElseThrow(() -> new UsernameNotFoundException(
            "given username is not found: username = " + username
        ));
  }
}
