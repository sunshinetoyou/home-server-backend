package com.bemain.spb.domain.user.dto;

import com.bemain.spb.domain.user.entity.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private RoleType role; // JSON에서 "HACKER", "DEVELOPER" 등으로 옴
}