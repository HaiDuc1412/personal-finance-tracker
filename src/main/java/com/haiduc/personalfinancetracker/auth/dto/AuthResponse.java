package com.haiduc.personalfinancetracker.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;       // seconds
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private String id;
        private String email;
        private String fullName;
        private String role;
    }
}
