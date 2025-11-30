package com.bemain.spb.domain.dto.lab;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LabSummaryResponse {
    private Long labId;
    private String title;
    private String developerName;
    private String challengeType; // 예: DVWA, Node.js
    private Long reportCount;     // 제보된 리포트 개수

    // JPQL에서 바로 꽂아넣기 위한 생성자
    public LabSummaryResponse(Long labId, String title, String developerName, String challengeType, Long reportCount) {
        this.labId = labId;
        this.title = title;
        this.developerName = developerName;
        this.challengeType = challengeType;
        this.reportCount = reportCount;
    }
}