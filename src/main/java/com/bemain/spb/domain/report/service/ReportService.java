package com.bemain.spb.domain.report.service;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.repository.DevLabRepository;
import com.bemain.spb.domain.report.dto.*;
import com.bemain.spb.domain.report.entity.Report;
import com.bemain.spb.domain.report.entity.ReportStatus;
import com.bemain.spb.domain.report.repository.ReportRepository;
import com.bemain.spb.domain.user.entity.RoleType;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final DevLabRepository devLabRepository;
    private final UserRepository userRepository;

    // 1. 리포트 제출 (해커)
    @Transactional
    public Long createReport(String username, ReportCreateRequest request) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        DevLab lab = devLabRepository.findById(request.getLabId())
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));

        Report report = Report.builder()
                .author(author)
                .devLab(lab)
                .title(request.getTitle())
                .content(request.getContent())
                .severity(request.getSeverity())
                // status는 빌더 기본값(PENDING) 사용
                .build();

        return reportRepository.save(report).getId();
    }

    // 2. 리포트 목록 조회
    @Transactional(readOnly = true)
    public List<ReportListResponse> getReports(Long labId) {
        List<Report> reports;
        if (labId != null) {
            reports = reportRepository.findAllByDevLabIdOrderByCreatedAtDesc(labId);
        } else {
            reports = reportRepository.findAll(); // 전체 조회 (관리자용 혹은 공개용)
        }

        return reports.stream()
                .map(ReportListResponse::new)
                .collect(Collectors.toList());
    }

    // 3. 리포트 상세 조회
    @Transactional(readOnly = true)
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트 없음"));
        return new ReportResponse(report);
    }

    // 4. 상태 변경 (개발자 전용)
    @Transactional
    public void updateStatus(Long reportId, String username, ReportStatusRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트 없음"));

        // [권한 체크] 랩의 주인이거나 관리자여야 함
        if (!report.getDevLab().getDeveloper().getId().equals(user.getId())
                && user.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("해당 랩의 개발자만 상태를 변경할 수 있습니다.");
        }

        report.setStatus(request.getStatus());
    }
}