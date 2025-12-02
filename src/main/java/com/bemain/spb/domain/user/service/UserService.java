package com.bemain.spb.domain.user.service;

import com.bemain.spb.domain.etc.jwt.JwtTokenProvider; // 기존에 만드신 Provider 경로 확인 필요
import com.bemain.spb.domain.user.dto.*;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public Long register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        // [New] 기본 설정값 생성
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("darkMode", false);   // 다크모드 기본: 꺼짐

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // 암호화 필수
                .nickname(request.getNickname())
                .role(request.getRole())
                .settings(defaultSettings)
                .build();

        return userRepository.save(user).getId();
    }

    // 로그인
    @Transactional
    public TokenResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성 (AccessToken, RefreshToken)
        String accessToken = jwtTokenProvider.createToken(user.getUsername(), user.getRole().name());
        String refreshToken = ""; // 필요시 구현

        return new TokenResponse(accessToken, refreshToken);
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public ProfileResponse getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return new ProfileResponse(user);
    }

    // 내 정보 수정
    @Transactional
    public void updateMe(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 닉네임 수정
        if (request.getNickname() != null) {
            user.updateNickname(request.getNickname());
        }

        // 설정 수정
        if (request.getSettings() != null) {
            user.getSettings().putAll(request.getSettings());
        }

        // 비밀번호 변경
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // 현재 비밀번호 검증
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않아 변경할 수 없습니다.");
            }
            // 2. 새 비밀번호 암호화 후 저장
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // 계정 삭제
    @Transactional
    public void deleteMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        userRepository.delete(user);
    }
}