package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.HackLab;
import com.bemain.spb.domain.lab.entity.LabStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class HackLabResponse {
    private Long id;
    private String labTitle;
    private String accessUrl;
    private LocalDateTime expiresAt;
    private long remainingMinutes; // 남은 시간 (편의성)

    public HackLabResponse(HackLab hackLab) {
        this.id = hackLab.getId();
        this.labTitle = hackLab.getDevLab().getTitle();
        this.accessUrl = hackLab.getUrl();

        this.expiresAt = hackLab.getExpiresAt();

        // 남은 시간 계산 (분 단위)
        if (expiresAt != null && expiresAt.isAfter(LocalDateTime.now())) {
            this.remainingMinutes = java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
        } else {
            this.remainingMinutes = 0;
        }
    }
}