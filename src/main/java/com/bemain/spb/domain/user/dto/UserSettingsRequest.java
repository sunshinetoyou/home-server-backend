package com.bemain.spb.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern; // 유효성 검사

@Getter
@NoArgsConstructor
public class UserSettingsRequest {

    private Boolean darkMode;

    // 값의 범위도 제한 가능 (예: ko, en만 허용)
    @Pattern(regexp = "^(ko|en)$", message = "언어는 'ko' 또는 'en'만 가능합니다.")
    private String language;
}