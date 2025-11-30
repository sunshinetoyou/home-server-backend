package com.bemain.spb.domain.report.repository;

import com.bemain.spb.domain.report.dto.ReportListResponse;
import com.bemain.spb.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 랩에 달린 리포트만 보고 싶을 때 (필터링)
    List<Report> findByLabId(Long labId);

    // 특정 해커가 쓴 리포트만 보고 싶을 때 (내 활동 내역)
    List<Report> findByAuthorId(Long authorId);

    @Query("SELECT new com.bemain.spb.domain.report.dto.ReportListResponse(" +
            "  r.id, r.title, r.author.nickname, r.severity, r.status, r.createdAt, COUNT(c) " +
            ") " +
            "FROM Report r " +
            "LEFT JOIN r.comments c " + // 댓글 조인
            "WHERE r.lab.id = :labId " +
            "GROUP BY r.id, r.title, r.author.nickname, r.severity, r.status, r.createdAt " +
            "ORDER BY r.createdAt DESC")
    List<ReportListResponse> findAllByLabIdWithCommentCount(@Param("labId") Long labId);
}