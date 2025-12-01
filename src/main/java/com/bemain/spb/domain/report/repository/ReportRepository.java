package com.bemain.spb.domain.report.repository;

import com.bemain.spb.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 랩에 달린 리포트들 조회 (최신순)
    List<Report> findAllByDevLabIdOrderByCreatedAtDesc(Long devLabId);

    // 내가 쓴 리포트 조회 (마이페이지용)
    List<Report> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}