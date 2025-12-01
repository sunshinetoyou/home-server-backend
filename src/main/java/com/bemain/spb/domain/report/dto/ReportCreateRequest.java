package com.bemain.spb.domain.report.dto;

import com.bemain.spb.domain.report.entity.ReportSeverity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {
    private Long labId;        // 어떤 랩에 대한 리포트인지
    private String title;
    private String content;    // PoC 등 상세 내용
    private ReportSeverity severity; // 해커가 생각하는 위험도
}