package com.bemain.spb.domain.tag.service;

import com.bemain.spb.domain.tag.dto.TagRequest;
import com.bemain.spb.domain.tag.dto.TagResponse;
import com.bemain.spb.domain.tag.entity.Tag;
import com.bemain.spb.domain.tag.entity.TagCategory;
import com.bemain.spb.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    // 태그 목록 조회 (카테고리 필터링 지원)
    @Transactional(readOnly = true)
    public List<TagResponse> getTags(TagCategory category) {
        List<Tag> tags;
        if (category != null) {
            tags = tagRepository.findByCategory(category);
        } else {
            tags = tagRepository.findAll();
        }

        return tags.stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
    }

    // 태그 생성 (추후 고민)
//    @Transactional
//    public TagResponse createTag(TagRequest request) {
//        // 이미 존재하면 기존 것 반환 (또는 에러 던지기 선택)
//        // 여기서는 "있으면 그거 쓰고, 없으면 만든다" 정책
//        return tagRepository.findByName(request.getName())
//                .map(TagResponse::new)
//                .orElseGet(() -> {
//                    Tag newTag = new Tag(request.getName(), request.getCategory());
//                    return new TagResponse(tagRepository.save(newTag));
//                });
//    }
}