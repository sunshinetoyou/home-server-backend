package com.bemain.spb.domain.user.dto;

import com.bemain.spb.domain.user.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse_front {
    private String token;
    private String username;
    private String role;
    private String nickname;
}