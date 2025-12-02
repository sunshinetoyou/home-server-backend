package com.bemain.spb.domain.lab.entity;

import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자 보호
@Table(name = "hack_lab") // 유니크 인덱스 제거 (히스토리 관리를 위해)
public class HackLab extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private DevLab devLab;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hacker_id", nullable = false)
    private User hacker;

    @Setter
    @Column(name = "url")
    private String url;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabStatus status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Setter
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public HackLab(DevLab devLab, User hacker, LocalDateTime expiresAt) {
        this.devLab = devLab;
        this.hacker = hacker;
        this.assignedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.status = LabStatus.PENDING; // 생성 시 기본값
    }

}