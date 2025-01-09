package com.example.blog.service;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class DateTimeService {

  public OffsetDateTime now() {
    return OffsetDateTime.now();
  }

}
