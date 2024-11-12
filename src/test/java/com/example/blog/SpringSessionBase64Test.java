package com.example.blog;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SpringSessionBase64Test {

  @Test
  public void test() {
    var uuid = "1dbec3f9-f9f3-41c7-a4dd-0d84297fd8a3";
    var base64Bytes = Base64.getEncoder().encode(uuid.getBytes());
    var base64String = new String(base64Bytes);
    System.out.println(base64String);
    // => MWRiZWMzZjktZjlmMy00MWM3LWE0ZGQtMGQ4NDI5N2ZkOGEz
    // => MWRiZWMzZjktZjlmMy00MWM3LWE0ZGQtMGQ4NDI5N2ZkOGEz
  }

  @Test
  public void bcrypt() {
    var encoder = new BCryptPasswordEncoder();

    // PWの最低文字数が10文字になったため。
    System.out.println(encoder.encode("password00"));
    System.out.println(encoder.encode("password00"));
    System.out.println(encoder.encode("password00"));
    // 同じパスワードでも出力値が異なることに注意 <= ソルトの付加
    // $2a$10$B0SVL8nAzks3ES5G8cFS1ulpS4OZLQyCvnjzi0ISWx9FrXVzKbkLK
    // $2a$10$JbGAmUhXF.KFaI3VrnL8ZOxzVNCOnMqonPlL82uHF7O2qwROSLUWK
    // $2a$10$/x0LDWs0tk6V90.VQ2ObdezEgBCDKuoLi1LWtIe49NKUrc7LqrWGq

    // 8文字↓
    // $2a$10$yov2wEmxbog1xF0pmeoMB.HMKkSoFWuT95xrDZs8K5XLDTkvWe9ua
    // $2a$10$R98uxPGVBI4bmBBU5vYmC.M0ukEaIySVZUEjzjPI6vAnoeuma7juK
    // $2a$10$2hIkgw9SXNZ4xUWUVrqzP.cWkAVkUfvYcqRerGqNPXQ4XUSABIEJm
  }
}
