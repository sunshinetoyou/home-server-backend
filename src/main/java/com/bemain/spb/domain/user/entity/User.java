package com.bemain.spb.domain.user.entity;

import com.bemain.spb.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    // JSONB 설정 (MySQL/Postgres 지원)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> settings = new HashMap<>();

    @Builder
    public User(String username, String password, String nickname, RoleType role, Map<String, Object> settings) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.settings = settings != null ? settings : new HashMap<>();
    }

    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}