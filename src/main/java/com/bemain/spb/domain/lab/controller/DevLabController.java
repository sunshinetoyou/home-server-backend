package com.bemain.spb.domain.lab.controller;

import com.bemain.spb.domain.lab.dto.*;
import com.bemain.spb.domain.lab.service.DevLabService;
import jakarta.validation.Valid; // [New] 유효성 검사
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/labs")
@RequiredArgsConstructor
public class DevLabController {

    private final DevLabService devLabService;

    // 랩 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLab(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DevLabCreateRequest request // [New] @Valid 추가
    ) {
        Long labId = devLabService.createLab(userDetails.getUsername(), request);

        // 생성 성공은 201 Created가 정석입니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "랩이 등록되었습니다.",
                "labId", labId
        ));
    }

    // 랩 목록 조회
    @GetMapping
    public ResponseEntity<List<DevLabListResponse>> getLabs(
            @RequestParam(required = false) String tag
    ) {
        return ResponseEntity.ok(devLabService.getLabs(tag));
    }

    // 랩 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<DevLabResponse> getLab(@PathVariable Long id) {
        return ResponseEntity.ok(devLabService.getLab(id));
    }

    // 랩 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        devLabService.deleteLab(id, userDetails.getUsername());
        return ResponseEntity.ok("랩이 삭제되었습니다.");
    }

    // 기본 정보 수정
    @PatchMapping("/{id}/info")
    public ResponseEntity<String> updateInfo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DevLabInfoUpdateRequest request
    ) {
        devLabService.updateInfo(id, userDetails.getUsername(), request);
        return ResponseEntity.ok("랩 기본 정보가 수정되었습니다.");
    }

    // 이미지 및 DB 설정 수정
    @PatchMapping("/{id}/images")
    public ResponseEntity<String> updateImages(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DevLabImagesUpdateRequest request
    ) {
        devLabService.updateImages(id, userDetails.getUsername(), request);
        // [변경] 상황에 따라 재배포가 안 될 수도 있으므로 메시지를 안전하게 변경
        return ResponseEntity.ok("이미지 및 인프라 설정이 수정되었습니다.");
    }

    // 상태 변경 (활성화/비활성화)
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DevLabStatusUpdateRequest request
    ) {
        devLabService.updateStatus(id, userDetails.getUsername(), request);
        String message = request.getIsActive() ? "랩이 활성화되었습니다. (배포 시작)" : "랩이 비활성화되었습니다.";
        return ResponseEntity.ok(message);
    }

    // [New] 랩 배포하기
    // POST /api/v1/labs/{id}/deploy
    @PostMapping("/{id}/deploy")
    public ResponseEntity<String> deployLab(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        devLabService.deployLab(id, userDetails.getUsername());

        // SSE 로그를 보라는 힌트 메시지 전달 가능
        return ResponseEntity.ok("배포가 시작되었습니다. 로그 창을 확인해주세요.");
    }

    // DevLab 배포 로그 스트리밍
    @GetMapping(value = "/{id}/deploy-logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDeployLogs(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 배포는 보통 1~2분 걸리므로 타임아웃 3분 설정
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);

        // 서비스 비동기 호출
        devLabService.streamDeployLogs(id, userDetails.getUsername(), emitter);

        return emitter;
    }
}