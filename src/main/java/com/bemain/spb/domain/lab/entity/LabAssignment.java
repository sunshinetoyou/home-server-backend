package com.bemain.spb.domain.lab.entity;

import com.bemain.spb.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "lab_assignment")
public class LabAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    private LocalDateTime assignedAt;

    @Column(name = "access_url")
    private String accessUrl; // 해커 전용 URL

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // 만료 시간

    public LabAssignment(User user, Lab lab) {
        this.user = user;
        this.lab = lab;
        this.assignedAt = LocalDateTime.now();
    }
}