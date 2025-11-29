package com.bemain.spb.controller;

import com.bemain.spb.dto.report.ReportDetailResponse;
import com.bemain.spb.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}