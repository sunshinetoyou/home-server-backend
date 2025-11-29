package com.bemain.spb.entity;

public enum ReportStatus {
    PENDING("검토 대기"),
    IN_PROGRESS("조치 중"),   // 개발자가 확인하고 고치는 중
    RESOLVED("해결 완료"),    // 취약점 패치 완료 (포트폴리오 핵심)
    REJECTED("반려됨");       // 오탐 또는 중복

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
