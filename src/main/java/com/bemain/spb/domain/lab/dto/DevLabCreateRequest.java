package com.bemain.spb.domain.lab.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class DevLabCreateRequest {
    private String title;
    private String description;

    // 3-Tier Images
    private String feImage;
    private String beImage;
    private String dbImage;

    private List<Long> tagIds;
}