package com.bemain.spb.domain.report.dto;

import com.bemain.spb.domain.report.entity.Report;
import com.bemain.spb.domain.report.entity.ReportSeverity;
import com.bemain.spb.domain.report.entity.ReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportListResponse {
    private Long id;
    private String title;
    private String authorName;
    private String labTitle;
    private ReportSeverity severity;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public ReportListResponse(Report report) {
        this.id = report.getId();
        this.title = report.getTitle();
        this.authorName = report.getAuthor().getNickname();
        this.labTitle = report.getDevLab().getTitle();
        this.severity = report.getSeverity();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
    }
}