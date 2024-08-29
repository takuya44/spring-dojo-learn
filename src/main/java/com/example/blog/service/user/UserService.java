package com.example.blog.service.user;

import com.example.blog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public void register(String username, String password) {
    userRepository.insert(username, password, true);
  }
}
