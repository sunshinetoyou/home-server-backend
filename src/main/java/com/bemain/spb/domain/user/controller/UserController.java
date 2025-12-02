package com.bemain.spb.domain.user.controller;

import com.bemain.spb.domain.user.dto.ProfileResponse;
import com.bemain.spb.domain.user.dto.UserProfileUpdateRequest;
import com.bemain.spb.domain.user.dto.UserPwUpdateRequest;
import com.bemain.spb.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponse response = userService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 내 정보 수정
    // 일반 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfileUpdateRequest request
    ) {
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("프로필이 업데이트되었습니다.");
    }

    // 비밀번호 변경
    @PutMapping("/pw")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserPwUpdateRequest request
    ) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteMe(userDetails.getUsername());
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }
}