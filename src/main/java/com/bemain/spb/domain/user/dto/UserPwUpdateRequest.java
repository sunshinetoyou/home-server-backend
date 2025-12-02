package com.bemain.spb.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserPwUpdateRequest {
    private String currentPassword; // 검증용
    private String newPassword;     // 변경할 값
}
