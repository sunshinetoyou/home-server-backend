package com.bemain.spb.domain.tag.dto;

import com.bemain.spb.domain.tag.entity.Tag;
import lombok.Getter;

@Getter
public class TagResponse {
    private Long id;
    private String name;
    private Enum category;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.category = tag.getCategory();
    }
}