package com.bemain.spb.domain.lab.entity;

import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "hack_lab",
        indexes = @Index(name = "idx_hacker_lab_unique", columnList = "hacker_id, lab_id", unique = true))
public class HackLab extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private DevLab devLab;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hacker_id", nullable = false)
    private User hacker;

    @Column(name = "url")
    private String url; // Private Access URL

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public HackLab(DevLab devLab, User hacker, LocalDateTime expiresAt) {
        this.devLab = devLab;
        this.hacker = hacker;
        this.assignedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
}