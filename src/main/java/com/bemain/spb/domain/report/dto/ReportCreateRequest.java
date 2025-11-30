package com.bemain.spb.domain.report.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {
    private Long labId;      // 어떤 랩에서 발견했는지
    private String title;    // 제목
    private String content;  // 내용 (마크다운 지원 예정)
    private String severity; // HIGH, MEDIUM, LOW
}