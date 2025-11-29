package com.bemain.spb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 만든 랩인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private User developer;

    // 템플릿 이미지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_id", nullable = false)
    private Images image;

    @Column(nullable = false)
    private String title; // 랩 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 랩 설명

    @Column(nullable = false)
    private String deployUrl; // 배포된 실제 URL (http://...)

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true; // 활성화 여부 (0/1)

    @OneToMany(mappedBy = "lab")
    private List<Report> reports = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public Lab(String title, String description, String deployUrl, User developer, Images image) {
        this.title = title;
        this.developer = developer;
        this.image = image;
        this.description = description;
        this.deployUrl = deployUrl;
        this.isActive = true;
    }
}
