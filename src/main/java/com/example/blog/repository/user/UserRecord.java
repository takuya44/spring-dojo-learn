package com.example.blog.repository.user;

public record UserRecord(
    String username,
    String password,
    boolean enabled
) {

}
