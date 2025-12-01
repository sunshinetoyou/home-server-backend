package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.tag.dto.TagResponse;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DevLabListResponse {
    private Long id;
    private String title;
    private String developerName;
    private List<TagResponse> tags;

    public DevLabListResponse(DevLab lab) {
        this.id = lab.getId();
        this.title = lab.getTitle();
        this.developerName = lab.getDeveloper().getNickname();
        this.tags = lab.getTags().stream().map(TagResponse::new).collect(Collectors.toList());
    }
}