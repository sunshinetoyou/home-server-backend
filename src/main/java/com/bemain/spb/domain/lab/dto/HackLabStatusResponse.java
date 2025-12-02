package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.LabStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HackLabStatusResponse {
    private Long id;                // 랩 ID (종료 요청 등에 사용)
    private LabStatus status;       // PENDING, RUNNING, ERROR (프론트 로직 분기용)
    private String detailStatus;    // "ContainerCreating", "ErrImagePull" (화면 표시용)
    private String failureMessage;  // 에러일 때만 채워지는 메시지 (없으면 null)
    private String containerUrl;    // 성공 시 접속할 URL
}