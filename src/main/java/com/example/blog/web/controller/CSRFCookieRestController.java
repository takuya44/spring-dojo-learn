package com.example.blog.web.controller;

import com.example.blog.api.CsrfCookieApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CSRFCookieRestController implements CsrfCookieApi {

  @Override
  public ResponseEntity<Void> getCsrfCookie() {
    return ResponseEntity.noContent().build();
  }
}
