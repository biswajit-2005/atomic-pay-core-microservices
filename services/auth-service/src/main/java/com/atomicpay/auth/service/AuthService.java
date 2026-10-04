package com.atomicpay.auth.service;

import com.atomicpay.auth.domain.KycStatus;
import com.atomicpay.auth.domain.UserRole;
import com.atomicpay.auth.domain.UserStatus;
import com.atomicpay.auth.dto.*;
import com.atomicpay.auth.entity.KycProfileEntity;
import com.atomicpay.auth.entity.RefreshTokenEntity;
import com.atomicpay.auth.entity.UserEntity;
import com.atomicpay.auth.exception.AuthException;
import com.atomicpay.auth.exception.ErrorCode;
import com.atomicpay.auth.repository.KycProfileRepository;
import com.atomicpay.auth.repository.RefreshTokenRepository;
import com.atomicpay.auth.repository.UserRepository;
import com.atomicpay.auth.security.JwtProperties;
import com.atomicpay.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KycProfileRepository kycProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            KycProfileRepository kycProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.kycProfileRepository = kycProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new AuthException(ErrorCode.USER_ALREADY_EXISTS, "Email is already registered");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.USER;

        UserEntity user = UserEntity.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // Initialize KYC profile
        KycProfileEntity kycProfile = KycProfileEntity.builder()
                .userId(savedUser.getId())
                .status(KycStatus.NOT_SUBMITTED)
                .build();
        kycProfileRepository.save(kycProfile);

        log.info("Registered new user with id: {}", savedUser.getId());
        return createAuthResponse(savedUser, KycStatus.NOT_SUBMITTED);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        validateUserStatus(user);

        KycStatus kycStatus = kycProfileRepository.findByUserId(user.getId())
                .map(KycProfileEntity::getStatus)
                .orElse(KycStatus.NOT_SUBMITTED);

        log.info("User logged in successfully: {}", user.getId());
        return createAuthResponse(user, kycStatus);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = jwtTokenProvider.hashToken(request.getRefreshToken());
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException(ErrorCode.TOKEN_EXPIRED, "Invalid or expired refresh token"));

        if (refreshTokenEntity.getRevokedAt() != null) {
            log.warn("Attempt to use revoked refresh token for user: {}", refreshTokenEntity.getUserId());
            throw new AuthException(ErrorCode.TOKEN_EXPIRED, "Refresh token has been revoked");
        }

        if (refreshTokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AuthException(ErrorCode.TOKEN_EXPIRED, "Refresh token has expired");
        }

        // Refresh Token Rotation: Revoke current token
        refreshTokenEntity.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(refreshTokenEntity);

        UserEntity user = userRepository.findById(refreshTokenEntity.getUserId())
                .orElseThrow(() -> new AuthException(ErrorCode.UNAUTHORIZED, "User no longer exists"));

        validateUserStatus(user);

        KycStatus kycStatus = kycProfileRepository.findByUserId(user.getId())
                .map(KycProfileEntity::getStatus)
                .orElse(KycStatus.NOT_SUBMITTED);

        log.info("Rotated refresh token for user: {}", user.getId());
        return createAuthResponse(user, kycStatus);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = jwtTokenProvider.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevokedAt(OffsetDateTime.now());
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token for user: {}", token.getUserId());
            });
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.UNAUTHORIZED, "User not found"));

        KycStatus kycStatus = kycProfileRepository.findByUserId(user.getId())
                .map(KycProfileEntity::getStatus)
                .orElse(KycStatus.NOT_SUBMITTED);

        return mapToUserResponse(user, kycStatus);
    }

    private AuthResponse createAuthResponse(UserEntity user, KycStatus kycStatus) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = jwtTokenProvider.hashToken(rawRefreshToken);

        OffsetDateTime refreshExpiry = OffsetDateTime.now().plusNanos(jwtProperties.getRefreshExpirationMs() * 1_000_000L);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .userId(user.getId())
                .tokenHash(hashedRefreshToken)
                .expiresAt(refreshExpiry)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpirationMs())
                .user(mapToUserResponse(user, kycStatus))
                .build();
    }

    private UserResponse mapToUserResponse(UserEntity user, KycStatus kycStatus) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .kycStatus(kycStatus)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void validateUserStatus(UserEntity user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthException(ErrorCode.USER_SUSPENDED);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthException(ErrorCode.USER_LOCKED);
        }
        if (user.getStatus() == UserStatus.CLOSED) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS, "User account is closed");
        }
    }
}
