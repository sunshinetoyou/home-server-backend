package com.bemain.spb.repository;

import com.bemain.spb.entity.Images;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Images, Long> {
    // 기본 CRUD(저장, 조회 등)는 JpaRepository가 다 알아서 해줍니다.
}