package com.bemain.spb.repository;

import com.bemain.spb.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 랩에 달린 리포트만 보고 싶을 때 (필터링)
    List<Report> findByLabId(Long labId);

    // 특정 해커가 쓴 리포트만 보고 싶을 때 (내 활동 내역)
    List<Report> findByAuthorId(Long authorId);
}