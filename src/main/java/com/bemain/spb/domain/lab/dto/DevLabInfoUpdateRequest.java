package com.bemain.spb.domain.lab.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DevLabInfoUpdateRequest {
    private String title;
    private String description;
    private List<Long> tagIds; // 태그도 여기서 처리
}
