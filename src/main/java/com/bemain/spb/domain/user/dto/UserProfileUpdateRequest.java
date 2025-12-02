package com.bemain.spb.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private UserSettingsRequest settings;
}