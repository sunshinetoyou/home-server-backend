package com.bemain.spb.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    private String nickname;
    private String currentPassword;
    private String newPassword;
    private Map<String, Object> settings; // JSONB 업데이트용
}