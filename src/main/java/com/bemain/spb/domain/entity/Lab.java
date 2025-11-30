package com.bemain.spb.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
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

    // 개발자가 제출한 이미지
    @Column(name = "docker_image")
    private String dockerImage;

    // [Re-Add] 실제 컨테이너 포트 (최종 결정된 포트)
    @Column(name = "container_port", nullable = false)
    private Integer containerPort;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Lab(User developer, Images image, String title, String description, String dockerImage, Integer containerPort) {
        this.developer = developer;
        this.image = image;
        this.title = title;
        this.description = description;
        this.dockerImage = dockerImage;
        this.containerPort = containerPort;
        this.isActive = true;
    }
}
