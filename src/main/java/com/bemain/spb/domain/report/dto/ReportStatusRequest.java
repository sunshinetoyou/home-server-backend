package com.bemain.spb.domain.report.dto;

import com.bemain.spb.domain.report.entity.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportStatusRequest {
    private ReportStatus status;     // RESOLVED, REJECTED 등
    private String developerComment; // "패치 완료했습니다" 등
}