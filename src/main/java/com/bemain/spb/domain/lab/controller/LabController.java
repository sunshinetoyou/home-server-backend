package com.bemain.spb.domain.lab.controller;

import com.bemain.spb.domain.lab.dto.LabCreateRequest;
import com.bemain.spb.domain.lab.dto.LabSummaryResponse;
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

    // 랩 목록 (대시보드용)
    // GET /api/lab/list
    @GetMapping("/list")
    public ResponseEntity<List<LabSummaryResponse>> getLabList() {
        List<LabSummaryResponse> labs = labService.getActiveLabList();
        return ResponseEntity.ok(labs);
    }
}