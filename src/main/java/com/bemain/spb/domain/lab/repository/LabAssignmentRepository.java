package com.bemain.spb.domain.lab.repository;

import com.bemain.spb.domain.lab.entity.LabAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LabAssignmentRepository extends JpaRepository<LabAssignment, Long> {
    // 유저와 랩으로 할당 정보 찾기
    Optional<LabAssignment> findByUserIdAndLabId(Long userId, Long labId);

    // 만료 시간이 지난(과거인) 할당 목록 찾기 (스케줄러용)
    List<LabAssignment> findAllByExpiresAtBefore(LocalDateTime now);

    // 전체 활성화된(실습 중인) 랩 개수 조회 (관리자 대시보드용)
    long countByExpiresAtAfter(LocalDateTime now);

    // 2. 특정 유저가 돌리고 있는 활성화된 랩 개수 조회 (리소스 제한용)
    // 예: "한 유저는 동시에 3개까지만 실습 가능" 같은 로직 짤 때 사용
    // long countByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);
}