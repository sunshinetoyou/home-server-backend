package com.bemain.spb.domain.lab.controller;

import com.bemain.spb.domain.lab.dto.HackLabResponse;
import com.bemain.spb.domain.lab.service.HackLabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/labs")
@RequiredArgsConstructor
public class HackLabController {

    private final HackLabService hackLabService;

    // 실습 시작
    @PostMapping("/{id}/start")
    public ResponseEntity<HackLabResponse> startLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(hackLabService.startLab(id, userDetails.getUsername()));
    }

    // 실습 종료
    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stopLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        hackLabService.stopLab(id, userDetails.getUsername());
        return ResponseEntity.ok("실습 환경이 반납되었습니다.");
    }

    // 내 실습 상태 조회
    @GetMapping("/my-status")
    public ResponseEntity<HackLabResponse> getMyStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        HackLabResponse status = hackLabService.getMyStatus(userDetails.getUsername());
        if (status == null) {
            return ResponseEntity.noContent().build(); // 204 No Content (실습 중 아님)
        }
        return ResponseEntity.ok(status);
    }
}