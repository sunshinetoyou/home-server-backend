package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.tag.dto.TagResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DevLabResponse {
    private Long id;
    private String title;
    private String description;
    private String developerName;

    private String feImage;
    private String beImage;
    private String dbImage;

    private String publicUrl;
    private boolean isActive;

    private List<TagResponse> tags;
    private LocalDateTime createdAt;

    public DevLabResponse(DevLab lab) {
        this.id = lab.getId();
        this.title = lab.getTitle();
        this.description = lab.getDescription();
        this.developerName = lab.getDeveloper().getNickname();

        this.feImage = lab.getFeImage();
        this.beImage = lab.getBeImage();
        this.dbImage = lab.getDbImage();

        this.publicUrl = lab.getPublicUrl();
        this.isActive = lab.isActive();
        this.createdAt = lab.getCreatedAt();

        this.tags = lab.getTags().stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
    }
}