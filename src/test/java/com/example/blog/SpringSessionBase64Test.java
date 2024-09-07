package com.example.blog;

import java.util.Base64;
import org.junit.jupiter.api.Test;

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
}
