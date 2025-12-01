package com.bemain.spb.domain.report.controller;

import com.bemain.spb.domain.report.dto.*;
import com.bemain.spb.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 리포트 제출
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReportCreateRequest request
    ) {
        Long reportId = reportService.createReport(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "리포트가 제출되었습니다.", "reportId", reportId));
    }

    // 리포트 목록 조회 (특정 랩 필터링)
    // GET /api/v1/reports?labId=5
    @GetMapping
    public ResponseEntity<List<ReportListResponse>> getReports(
            @RequestParam(required = false) Long labId
    ) {
        return ResponseEntity.ok(reportService.getReports(labId));
    }

    // 리포트 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    // 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReportStatusRequest request
    ) {
        reportService.updateStatus(id, userDetails.getUsername(), request);
        return ResponseEntity.ok("리포트 상태가 변경되었습니다.");
    }
}