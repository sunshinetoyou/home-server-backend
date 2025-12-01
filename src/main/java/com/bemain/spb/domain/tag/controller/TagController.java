package com.bemain.spb.domain.tag.controller;

import com.bemain.spb.domain.tag.dto.TagRequest;
import com.bemain.spb.domain.tag.dto.TagResponse;
import com.bemain.spb.domain.tag.entity.TagCategory;
import com.bemain.spb.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 태그 목록 조회
    @GetMapping
    public ResponseEntity<List<TagResponse>> getTags(
            @RequestParam(required = false) TagCategory category
    ) {
        return ResponseEntity.ok(tagService.getTags(category));
    }

    // 태그 생성 (이건 좀 더 고려해봐야 할 듯)
//    @PostMapping
//    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest request) {
//        return ResponseEntity.ok(tagService.createTag(request));
//    }
}