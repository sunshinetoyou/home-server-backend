package com.bemain.spb.domain.lab.entity;

import com.bemain.spb.domain.image.entity.Images;
import com.bemain.spb.domain.report.entity.Report;
import com.bemain.spb.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "lab")
public class Lab {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private User developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_id", nullable = false)
    private Images image;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // [New] 3-Tier Images
    @Column(name = "fe_image", nullable = false)
    private String feImage;

    @Column(name = "be_image", nullable = false)
    private String beImage;

    @Column(name = "db_image") // Nullable
    private String dbImage;

    @Column(name = "deploy_url")
    private String deployUrl;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;

    @OneToMany(mappedBy = "lab")
    private List<Report> reports = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Lab(User developer, Images image, String title, String description, String feImage, String beImage, String dbImage) {
        this.developer = developer;
        this.image = image;
        this.title = title;
        this.description = description;
        this.feImage = feImage;
        this.beImage = beImage;
        this.dbImage = dbImage;
        this.isActive = true;
    }
}