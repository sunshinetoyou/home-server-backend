package com.bemain.spb.domain.lab.entity;

import com.bemain.spb.domain.tag.entity.Tag;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "dev_lab")
public class DevLab extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private User developer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 3-Tier Images
    @Column(name = "fe_image")
    private String feImage;

    @Column(name = "be_image")
    private String beImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "db_type")
    private LabDbType dbType;

    @Column(columnDefinition = "TEXT")
    private String dbSource;

    @Column(name = "public_url")
    private String publicUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    // Tag와의 N:M 관계 (중간 테이블 lab_tag 자동 생성)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lab_tag",
            joinColumns = @JoinColumn(name = "lab_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder
    public DevLab(User developer, String title, String description, String feImage, String beImage, LabDbType dbType, String dbSource) {
        this.developer = developer;
        this.title = title;
        this.description = description;
        this.feImage = feImage;
        this.beImage = beImage;
        this.dbType = dbType;
        this.dbSource = dbSource;
        this.isActive = false;
    }
}