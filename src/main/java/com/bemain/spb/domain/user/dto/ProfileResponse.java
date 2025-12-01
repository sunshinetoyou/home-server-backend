package com.bemain.spb.domain.user.dto;

import com.bemain.spb.domain.user.entity.RoleType;
import com.bemain.spb.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
public class ProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private RoleType role;
    private Map<String, Object> settings;

    // 개발자용: 내가 올린 랩 요약
    private List<DevLabSummary> deployedLabs;

    // 해커용: 내가 쓴 리포트 요약
    private List<ReportSummary> myReports;

    public ProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.settings = user.getSettings();
    }

    @Getter @Setter
    public static class DevLabSummary {
        private Long id;
        private String title;
        private boolean isActive;
        public DevLabSummary(Long id, String title, boolean isActive) {
            this.id = id; this.title = title; this.isActive = isActive;
        }
    }

    @Getter @Setter
    public static class ReportSummary {
        private Long id;
        private String labTitle; // 어떤 랩에 썼는지
        private String title;
        private String status;   // PENDING, ACCEPTED...
        public ReportSummary(Long id, String labTitle, String title, String status) {
            this.id = id; this.labTitle = labTitle; this.title = title; this.status = status;
        }
    }
}