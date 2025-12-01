package com.bemain.spb.domain.tag.dto;

import com.bemain.spb.domain.tag.entity.TagCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagRequest {
    private String name;
    private TagCategory category;
}