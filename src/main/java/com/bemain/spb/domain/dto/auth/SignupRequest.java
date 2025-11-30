package com.bemain.spb.domain.dto.auth;

import com.bemain.spb.domain.entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private Role role; // JSON 보낼 때 "HACKER" 또는 "DEVELOPER"
}