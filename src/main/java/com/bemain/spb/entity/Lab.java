package com.bemain.spb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 랩 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 랩 설명

    @Column(nullable = false)
    private String deployUrl; // 배포된 실제 URL (http://...)

    private boolean isActive = true; // 활성화 여부 (0/1)

    // 누가 만든 랩인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User developer;

    public Lab(String title, String description, String deployUrl, User developer) {
        this.title = title;
        this.description = description;
        this.deployUrl = deployUrl;
        this.developer = developer;
        this.isActive = true;
    }
}
