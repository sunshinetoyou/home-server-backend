package com.bemain.spb.service;

import com.bemain.spb.dto.report.ReportCreateRequest;
import com.bemain.spb.dto.report.ReportDetailResponse;
import com.bemain.spb.dto.report.ReportListResponse;
import com.bemain.spb.dto.report.ReportStatusRequest;
import com.bemain.spb.entity.Lab;
import com.bemain.spb.entity.Report;
import com.bemain.spb.entity.User;
import com.bemain.spb.repository.LabRepository;
import com.bemain.spb.repository.ReportRepository;
import com.bemain.spb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용 트랜잭션 (성능 최적화)
public class ReportService {

    private final ReportRepository reportRepository;
    private final LabRepository labRepository;
    private final UserRepository userRepository;

    // 리포트 상세 조회 (댓글 포함)
    public ReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리포트를 찾을 수 없습니다. id=" + reportId));

        // Entity -> DTO 변환
        return new ReportDetailResponse(report);
    }

    // 1. 리포트 생성 (Issue Open)
    @Transactional
    public Long createReport(String userName, ReportCreateRequest request) {
        User author = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Lab lab = labRepository.findById(request.getLabId())
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));

        Report report = new Report(
                request.getTitle(),
                request.getContent(),
                request.getSeverity(),
                author,
                lab
        );

        return reportRepository.save(report).getId();
    }

    // 2. 리포트 목록 조회 (Issue List)
    public List<ReportListResponse> getReportList(Long labId) {
        return reportRepository.findAllByLabIdWithCommentCount(labId);
    }

    // 3. 리포트 상태 변경 (개발자 전용)
    @Transactional
    public void updateReportStatus(Long reportId, String username, ReportStatusRequest request) {
        // 1. 리포트 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트가 없습니다."));

        // 2. 요청한 유저(개발자) 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        // 리포트가 달린 '랩'의 '개발자 ID'와 현재 로그인한 '유저 ID'가 같은지 확인
        if (!report.getLab().getDeveloper().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 랩에 등록된 리포트만 처리할 수 있습니다.");
        }

        // 3. 상태 및 코멘트 업데이트 (Entity의 Setter 사용)
        report.setStatus(request.getStatus());
        report.setDeveloperComment(request.getDeveloperComment());
    }
}