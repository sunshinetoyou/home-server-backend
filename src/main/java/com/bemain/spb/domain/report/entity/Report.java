package com.bemain.spb.domain.report.entity;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.global.entity.BaseTimeEntity;
import com.bemain.spb.domain.comment.entity.Comment; // Comment 임포트
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "report")
public class Report extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private DevLab devLab;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportSeverity severity;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Report(User author, DevLab devLab, String title, String content, ReportSeverity severity) {
        this.author = author;
        this.devLab = devLab;
        this.title = title;
        this.content = content;
        this.severity = severity;
        this.status = ReportStatus.PENDING;
    }
}