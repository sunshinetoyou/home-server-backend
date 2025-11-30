package com.bemain.spb.domain.controller;

import com.bemain.spb.domain.dto.lab.LabSummaryResponse;
import com.bemain.spb.domain.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    // 랩 목록 (대시보드용)
    // GET /api/lab/list
    @GetMapping("/list")
    public ResponseEntity<List<LabSummaryResponse>> getLabList() {
        List<LabSummaryResponse> labs = labService.getActiveLabList();
        return ResponseEntity.ok(labs);
    }
}