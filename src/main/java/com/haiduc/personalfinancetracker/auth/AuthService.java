package com.haiduc.personalfinancetracker.auth;

import com.haiduc.personalfinancetracker.auth.dto.AuthResponse;
import com.haiduc.personalfinancetracker.auth.dto.LoginRequest;
import com.haiduc.personalfinancetracker.auth.dto.RefreshTokenRequest;
import com.haiduc.personalfinancetracker.auth.dto.RegisterRequest;
import com.haiduc.personalfinancetracker.common.enums.UserRole;
import com.haiduc.personalfinancetracker.common.exception.AppException;
import com.haiduc.personalfinancetracker.common.exception.DuplicateResourceException;
import com.haiduc.personalfinancetracker.common.exception.ResourceNotFoundException;
import com.haiduc.personalfinancetracker.user.User;
import com.haiduc.personalfinancetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail()
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }


    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", request.getEmail()
                ));

        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }


    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException("Invalid refresh token",
                        HttpStatus.UNAUTHORIZED
                ));

        if (!refreshToken.isValid()) {
            throw new AppException("Refresh token is expired or revoked",
                    HttpStatus.UNAUTHORIZED
            );
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        log.info("Token refreshed for user: {}", user.getEmail());

        return buildAuthResponse(user);
    }


    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out, token revoked");
                });
    }


    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)   // 15 phút = 900 giây
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    private void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = RefreshToken.builder()
                .tokenHash(hashToken(rawToken))
                .user(user)
                .expiresAt(OffsetDateTime.now()
                        .plusSeconds(refreshTokenExpiry / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(token);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
