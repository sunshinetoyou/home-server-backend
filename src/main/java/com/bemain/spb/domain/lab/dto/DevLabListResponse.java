package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.entity.LabDbType;
import com.bemain.spb.domain.tag.dto.TagResponse;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DevLabListResponse {
    private Long id;
    private String title;
    private String author;

    private String feImage;
    private String beImage;
    private LabDbType dbType;

    private List<TagResponse> tags;

    public DevLabListResponse(DevLab lab) {
        this.id = lab.getId();
        this.title = lab.getTitle();
        this.author = lab.getDeveloper().getNickname();
        this.feImage = lab.getFeImage();
        this.beImage = lab.getBeImage();
        this.dbType = lab.getDbType();
        this.tags = lab.getTags().stream().map(TagResponse::new).collect(Collectors.toList());
    }
}