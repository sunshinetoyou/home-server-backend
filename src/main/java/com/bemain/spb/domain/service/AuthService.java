package com.bemain.spb.domain.service;

import com.bemain.spb.domain.dto.auth.LoginRequest;
import com.bemain.spb.domain.dto.auth.SignupRequest;
import com.bemain.spb.domain.dto.auth.TokenResponse;
import com.bemain.spb.domain.entity.User;
import com.bemain.spb.domain.jwt.JwtTokenProvider;
import com.bemain.spb.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public void register(SignupRequest request) {
        // 1. 중복 아이디 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 유저 저장
        User user = new User(
                request.getUsername(),
                encodedPassword,
                request.getEmail(),
                request.getRole() // DTO 필드명 role과 매핑
        );
        userRepository.save(user);
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. ID/PW 인증 (Security가 알아서 검증함)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 인증 통과 시 JWT 토큰 생성
        // Principal에서 권한 정보 가져오기
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String token = jwtTokenProvider.createToken(request.getUsername(), role);

        return new TokenResponse(token, "Bearer");
    }
}