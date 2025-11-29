package com.bemain.spb.service;

import com.bemain.spb.dto.report.ReportDetailResponse;
import com.bemain.spb.entity.Report;
import com.bemain.spb.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용 트랜잭션 (성능 최적화)
public class ReportService {

    private final ReportRepository reportRepository;

    // 리포트 상세 조회 (댓글 포함)
    public ReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리포트를 찾을 수 없습니다. id=" + reportId));

        // Entity -> DTO 변환
        return new ReportDetailResponse(report);
    }
}