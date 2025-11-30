package com.bemain.spb.domain.user.controller;

import com.bemain.spb.domain.user.dto.auth.LoginRequest;
import com.bemain.spb.domain.user.dto.auth.SignupRequest;
import com.bemain.spb.domain.user.dto.auth.TokenResponse;
import com.bemain.spb.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입: POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
    }

    // 로그인: POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}