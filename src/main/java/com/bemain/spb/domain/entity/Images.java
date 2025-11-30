package com.bemain.spb.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Images {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 이미지 이름 (예: Web Server Basic)

    @Column(nullable = false)
    private String imageUrl; // 다운로드 링크 또는 파일 경로

    @Column(columnDefinition = "TEXT")
    private String description; // 설명 및 규칙

    @Column(length = 50)
    private String type;

    public Images(String title, String imageUrl, String description, String type) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.type = type;
    }
}
