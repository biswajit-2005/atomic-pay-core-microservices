package com.atomicpay.auth.controller;

import com.atomicpay.auth.dto.*;
import com.atomicpay.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok(response, requestId), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.ok(null, requestId));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpServletRequest
    ) {
        String requestId = getRequestId(httpServletRequest);
        UserResponse response = authService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(response, requestId));
    }

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }
}
