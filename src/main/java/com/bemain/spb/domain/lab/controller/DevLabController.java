package com.bemain.spb.domain.lab.controller;

import com.bemain.spb.domain.lab.dto.DevLabCreateRequest;
import com.bemain.spb.domain.lab.dto.DevLabListResponse;
import com.bemain.spb.domain.lab.dto.DevLabResponse;
import com.bemain.spb.domain.lab.service.DevLabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/labs")
@RequiredArgsConstructor
public class DevLabController {

    private final DevLabService devLabService;

    // 랩 등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> createLab(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DevLabCreateRequest request
    ) {
        Long labId = devLabService.createLab(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of(
                "message", "랩이 등록되었습니다.",
                "labId", labId
        ));
    }

    // 랩 목록 조회 (필터링)
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
}