package com.bemain.spb.domain.report.dto;

import com.bemain.spb.domain.comment.dto.CommentResponse;
import com.bemain.spb.domain.report.entity.Report;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReportResponse extends ReportListResponse {
    private String content; // 상세 내용 포함
    private List<CommentResponse> comments;

    public ReportResponse(Report report) {
        super(report);
        this.content = report.getContent();

        this.comments = report.getComments().stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}