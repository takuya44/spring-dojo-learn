package com.example.blog.service.user;

public record UserEntity(
    long id,
    String username,
    String password,
    boolean enabled
) {

}
