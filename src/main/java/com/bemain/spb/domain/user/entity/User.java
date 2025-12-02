package com.bemain.spb.domain.user.entity;

import com.bemain.spb.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Table(name = "users")
@Builder // 1. 빌더 패턴 사용
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 2. JPA용 기본 생성자 (필수)
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 3. 빌더가 사용할 모든 필드 생성자 (필수)
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
    @Builder.Default
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