package com.bemain.spb.domain.tag.repository;

import com.bemain.spb.domain.tag.entity.Tag;
import com.bemain.spb.domain.tag.entity.TagCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // 이름으로 중복 체크용
    boolean existsByName(String name);

    // 이름으로 조회 (랩 등록 시 사용)
    Optional<Tag> findByName(String name);

    // 카테고리별 필터링 조회
    List<Tag> findByCategory(TagCategory category);
}