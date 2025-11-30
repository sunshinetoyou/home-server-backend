package com.bemain.spb.domain.lab.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LabCreateRequest {
    // 1. 어떤 템플릿을 사용하는가? (Images ID)
    private Long imageId;

    // 2. 랩 기본 정보
    private String title;
    private String description;

    // 3. 개발자가 제출한 배포 정보
    private String feImage; // 필수
    private String beImage; // 필수
    private String dbImage; // 선택 (없으면 SQLite)
}