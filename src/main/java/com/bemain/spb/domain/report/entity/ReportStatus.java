package com.bemain.spb.domain.report.entity;

public enum ReportStatus {
    PENDING,    // 검토 대기 중
    REVIEWING,  // 검토 중 (개발자가 확인 중)
    APPROVED,   // 유효한 취약점 인정 (포인트 지급 대상)
    REJECTED,   // 반려 (오탐, 중복, 재현 불가)
    FIXED       // 패치 완료
}