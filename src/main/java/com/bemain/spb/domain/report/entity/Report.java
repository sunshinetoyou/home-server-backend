package com.bemain.spb.domain.report.entity;

import com.bemain.spb.domain.comment.entity.Comment;
import com.bemain.spb.domain.lab.entity.Lab;
import com.bemain.spb.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 저장
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 썼는지 (Hacker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // 어떤 랩에 대한 리포트인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    @Column(nullable = false)
    private String title; // 취약점 제목 (예: XSS 발견)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용 (PoC, 재현 과정)

    @Column(nullable = false)
    private String severity; // 위험도 (High, Medium, Low)

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // 상태 (PENDING, IN_PROGRESS, RESOLVED, REJECTED)

    // 개발자 피드백 (반려 사유 or 해결 코멘트)
    @Column(columnDefinition = "TEXT")
    private String developerComment;

    // 댓글 리스트 (양방향 매핑)
    // 리포트가 삭제되면 댓글도 같이 삭제되도록 cascade 설정
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt; // 작성일

    public Report(String title, String content, String severity, User author, Lab lab) {
        this.title = title;
        this.content = content;
        this.severity = severity;
        this.author = author;
        this.lab = lab;
        this.status = ReportStatus.PENDING;
    }
}