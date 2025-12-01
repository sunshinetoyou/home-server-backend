package com.bemain.spb.domain.user.controller;

import com.bemain.spb.domain.user.dto.ProfileResponse;
import com.bemain.spb.domain.user.dto.UserUpdateRequest;
import com.bemain.spb.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping
    public ResponseEntity<ProfileResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponse response = userService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 내 정보 수정
    @PatchMapping
    public ResponseEntity<String> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request
    ) {
        userService.updateMe(userDetails.getUsername(), request);
        return ResponseEntity.ok("정보가 수정되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<String> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteMe(userDetails.getUsername());
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }
}