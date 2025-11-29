package com.bemain.spb.dto.report;

import com.bemain.spb.entity.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReportListResponse {
    private Long reportId;
    private String title;
    private String authorName;
    private String severity;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private Long commentCount; // 깃허브처럼 댓글 수 표시!

    // JPQL 프로젝션용 생성자
    public ReportListResponse(Long reportId, String title, String authorName, String severity, ReportStatus status, LocalDateTime createdAt, Long commentCount) {
        this.reportId = reportId;
        this.title = title;
        this.authorName = authorName;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
    }
}