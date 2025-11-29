package com.bemain.spb.dto.report;

import com.bemain.spb.entity.Report;
import com.bemain.spb.entity.Comment;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReportDetailResponse {

    private Long reportId;
    private String title;
    private String content;
    private String severity; // HIGH, MEDIUM, LOW
    private String status;   // PENDING, RESOLVED...
    private String authorName; // 작성자 닉네임
    private String labTitle;   // 대상 랩 제목
    private LocalDateTime createdAt;

    // 개발자 피드백 (최종 결론)
    private String developerComment;

    // 대화형 댓글 목록
    private List<CommentDto> comments;

    // 생성자에서 Entity -> DTO 변환
    public ReportDetailResponse(Report report) {
        this.reportId = report.getId();
        this.title = report.getTitle();
        this.content = report.getContent();
        this.severity = report.getSeverity();
        this.status = report.getStatus().name();
        this.authorName = report.getAuthor().getNickname();
        this.labTitle = report.getLab().getTitle();
        this.createdAt = report.getCreatedAt();
        this.developerComment = report.getDeveloperComment();

        // 댓글 리스트 변환
        this.comments = report.getComments().stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class CommentDto {
        private Long id;
        private String authorName;
        private String content;
        private LocalDateTime createdAt;

        public CommentDto(Comment comment) {
            this.id = comment.getId();
            this.authorName = comment.getAuthor().getNickname();
            this.content = comment.getContent();
            this.createdAt = comment.getCreatedAt();
        }
    }
}