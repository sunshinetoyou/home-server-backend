package com.bemain.spb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 저장
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 취약점 제목 (예: XSS 발견)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용 (PoC, 재현 과정)

    @Column(nullable = false)
    private String severity; // 위험도 (High, Medium, Low)

    @CreatedDate
    private LocalDateTime createdAt; // 작성일

    // 누가 썼는지 (Hacker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    // 어떤 랩에 대한 리포트인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    public Report(String title, String content, String severity, User author, Lab lab) {
        this.title = title;
        this.content = content;
        this.severity = severity;
        this.author = author;
        this.lab = lab;
    }
}