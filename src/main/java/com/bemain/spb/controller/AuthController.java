package com.bemain.spb.controller;

import com.bemain.spb.dto.auth.LoginRequest;
import com.bemain.spb.dto.auth.SignupRequest;
import com.bemain.spb.dto.auth.TokenResponse;
import com.bemain.spb.service.AuthService;
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
        try {
            TokenResponse token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            // 🚨 서버 로그에 에러의 진짜 원인을 출력합니다 (BadCredentialsException인지 확인)
            e.printStackTrace(); 
            
            // Postman 응답으로도 원인을 보여줍니다.
            return ResponseEntity.status(403)
                    .body("로그인 실패 원인: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
        }
    }
}