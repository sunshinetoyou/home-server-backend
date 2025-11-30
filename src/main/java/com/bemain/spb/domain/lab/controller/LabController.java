package com.bemain.spb.domain.lab.controller;

import com.bemain.spb.domain.lab.dto.LabCreateRequest;
import com.bemain.spb.domain.lab.dto.LabStatusResponse;
import com.bemain.spb.domain.lab.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    // 랩 등록 API (POST /api/lab)
    @PostMapping
    public ResponseEntity<String> createLab(
            @RequestBody LabCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 토큰에 있는 username을 Service로 넘깁니다.
        Long labId = labService.createLab(userDetails.getUsername(), request);

        return ResponseEntity.ok("랩이 성공적으로 등록되었습니다. ID: " + labId);
    }

    // 랩 실습 시작 (POST /api/lab/{id}/start)
    @PostMapping("/{id}/start")
    public ResponseEntity<String> startLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 해커의 ID와 랩 ID를 넘겨 K8s 파드 생성을 요청합니다.
        String deployUrl = labService.allocationLab(id, userDetails.getUsername());

        return ResponseEntity.ok("실습 환경이 성공적으로 배포되었습니다. 접속 주소: " + deployUrl);
    }

    // 실습 종료 (반납)
    // POST /api/lab/{id}/stop
    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stopLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        labService.stopLab(id, userDetails.getUsername());
        return ResponseEntity.ok("실습 환경이 반납되었습니다.");
    }

    // 랩 목록 (대시보드용)
    // GET /api/lab/status
    @GetMapping("/status")
    public ResponseEntity<LabStatusResponse> getLabStatus() {
        LabStatusResponse labs = labService.getLabStatus();
        return ResponseEntity.ok(labs);
    }
}