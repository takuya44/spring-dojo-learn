package com.example.blog.service.user;

public record UserEntity(
    String username,
    String password,
    boolean enabled
) {

}
