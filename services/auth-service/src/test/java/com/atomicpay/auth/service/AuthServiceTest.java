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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private KycProfileRepository kycProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private UserEntity sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = UserEntity.builder()
                .id(userId)
                .email("alice@example.com")
                .passwordHash("hashed_password")
                .fullName("Alice Smith")
                .phone("+919876543210")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully register new user and initialize KYC profile")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alice@example.com")
                .password("securePassword123")
                .fullName("Alice Smith")
                .phone("+919876543210")
                .build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("hashed_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("jwt_access_token");
        when(jwtTokenProvider.hashToken(anyString())).thenReturn("hashed_refresh_token");
        when(jwtProperties.getExpirationMs()).thenReturn(900000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt_access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("alice@example.com", response.getUser().getEmail());
        assertEquals(KycStatus.NOT_SUBMITTED, response.getUser().getKycStatus());

        verify(kycProfileRepository, times(1)).save(any(KycProfileEntity.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("Should throw USER_ALREADY_EXISTS when registering existing email")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alice@example.com")
                .password("password123")
                .fullName("Alice Smith")
                .build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        AuthException ex = assertThrows(AuthException.class, () -> authService.register(request));
        assertEquals(ErrorCode.USER_ALREADY_EXISTS, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("securePassword123")
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("securePassword123", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("jwt_access_token");
        when(jwtTokenProvider.hashToken(anyString())).thenReturn("hashed_refresh_token");
        when(jwtProperties.getExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_access_token", response.getAccessToken());
        assertEquals("alice@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("Should throw INVALID_CREDENTIALS for wrong password")
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should rotate refresh token upon token refresh request")
    void refresh_Success_RotatesRefreshToken() {
        RefreshRequest request = RefreshRequest.builder()
                .refreshToken("raw_refresh_token")
                .build();

        RefreshTokenEntity existingToken = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash("hashed_refresh_token")
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(jwtTokenProvider.hashToken(eq("raw_refresh_token"))).thenReturn("hashed_refresh_token");
        when(jwtTokenProvider.hashToken(argThat(token -> token != null && !token.equals("raw_refresh_token")))).thenReturn("new_hashed_refresh_token");
        when(refreshTokenRepository.findByTokenHash("hashed_refresh_token")).thenReturn(Optional.of(existingToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("new_jwt_access_token");

        AuthResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new_jwt_access_token", response.getAccessToken());
        assertNotNull(existingToken.getRevokedAt()); // Verifies rotation (old token revoked)
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
    }
}
