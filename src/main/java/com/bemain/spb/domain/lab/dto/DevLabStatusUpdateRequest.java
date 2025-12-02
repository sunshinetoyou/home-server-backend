package com.bemain.spb.domain.lab.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DevLabStatusUpdateRequest {
    private Boolean isActive; // 필수
}
