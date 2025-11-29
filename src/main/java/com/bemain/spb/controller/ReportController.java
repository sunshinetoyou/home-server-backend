package com.bemain.spb.controller;

import com.bemain.spb.dto.report.ReportCreateRequest;
import com.bemain.spb.dto.report.ReportDetailResponse;
import com.bemain.spb.dto.report.ReportListResponse;
import com.bemain.spb.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vul/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 리포트 상세 조회 API
    // GET /api/vul/report/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponse> getReport(@PathVariable Long id) {
        ReportDetailResponse response = reportService.getReportDetail(id);
        return ResponseEntity.ok(response);
    }

    // 2. 리포트 생성 (글쓰기)
    // POST /api/vul/report
    @PostMapping
    public ResponseEntity<String> createReport(
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails // 로그인한 유저 정보
    ) {
        String userName = userDetails.getUsername();
        Long reportId = reportService.createReport(userName, request);
        return ResponseEntity.ok("리포트가 등록되었습니다. ID: " + reportId);
    }

    // 3. 특정 랩의 리포트 목록 조회
    // GET /api/vul/report/list?labId=1
    @GetMapping("/list")
    public ResponseEntity<List<ReportListResponse>> getReportList(@RequestParam Long labId) {
        return ResponseEntity.ok(reportService.getReportList(labId));
    }
}