package com.bemain.spb.domain.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabStatusResponse {

    // 1. Live Targets (활성화된 랩 개수)
    private long liveTargets;

    // 2. Threat Level (위험도: "Low", "Moderate", "High", "Critical")
    private String threatLevel;

    // 3. Bugs Found (총 발견된 취약점 리포트 수)
    private long bugsFound;

    // 4. Active Hunters (활동 중인 해커 수)
    private long activeHunters;
}