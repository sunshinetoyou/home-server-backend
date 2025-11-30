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
}