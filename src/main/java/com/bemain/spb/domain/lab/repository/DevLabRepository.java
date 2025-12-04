package com.bemain.spb.domain.lab.repository;

import com.bemain.spb.domain.lab.entity.DevLab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DevLabRepository extends JpaRepository<DevLab, Long> {

    // 개발자 대시보드용 (내가 만든 랩)
    List<DevLab> findAllByDeveloperId(Long developerId);

    // 활성화된 모든 랩 조회 (최신순)
    List<DevLab> findAllByIsActiveTrueOrderByCreatedAtDesc();
    // 모든 랩 조회
    List<DevLab> findAllByOrderByCreatedAtDesc();
    // 내 랩 조회
    List<DevLab> findAllByDeveloper_UsernameOrderByCreatedAtDesc(String username);

    // 태그 기반 랩 조회
    @Query("SELECT DISTINCT d FROM DevLab d JOIN d.tags t WHERE t.name = :tagName AND d.isActive = true")
    List<DevLab> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT d FROM DevLab d " +
            "JOIN d.labTags lt " +
            "JOIN lt.tag t " +
            "WHERE d.developer.username = :username AND t.name = :tagName " +
            "ORDER BY d.createdAt DESC")
    List<DevLab> findByDeveloperAndTagName(@Param("username") String username, @Param("tagName") String tagName);
}