package com.bemain.spb.domain.lab.repository;

import com.bemain.spb.domain.lab.dto.LabStatusResponse;
import com.bemain.spb.domain.lab.entity.Lab;
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

    long countByIsActiveTrue();
}