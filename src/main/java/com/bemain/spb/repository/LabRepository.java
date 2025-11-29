package com.bemain.spb.repository;

import com.bemain.spb.dto.lab.LabSummaryResponse;
import com.bemain.spb.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabRepository extends JpaRepository<Lab, Long> {

    // 1. 모든 활성화된 랩만 가져오기 (메인 랩 보드용)
    // SQL: SELECT * FROM lab WHERE is_active = true
    List<Lab> findAllByIsActiveTrue();

    // 2. 특정 개발자가 만든 랩만 가져오기 (마이 페이지용)
    // SQL: SELECT * FROM lab WHERE developer_id = ?
    List<Lab> findByDeveloperId(Long developerId);

    // 랩 정보와 리포트 개수를 한 번에 가져오는 쿼리
    @Query("SELECT new com.bemain.spb.dto.lab.LabSummaryResponse(" +
            "  l.id, l.title, l.developer.nickname, l.image.title, COUNT(r) " +
            ") " +
            "FROM Lab l " +
            "LEFT JOIN l.reports r " +  // <--- 여기가 핵심 변경 포인트!
            "WHERE l.isActive = true " +
            "GROUP BY l.id, l.title, l.developer.nickname, l.image.title " +
            "ORDER BY l.createdAt DESC")
    List<LabSummaryResponse> findAllActiveLabsWithStats();
}