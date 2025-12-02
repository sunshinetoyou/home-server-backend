package com.bemain.spb.domain.lab.repository;

import com.bemain.spb.domain.lab.entity.HackLab;
import com.bemain.spb.domain.lab.entity.LabStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HackLabRepository extends JpaRepository<HackLab, Long> {

    // 특정 해커가 수행 중인 랩 조회
    Optional<HackLab> findByHackerId(Long hackerId);

    // 내(해커)가 수행 중인 모든 랩 조회
    List<HackLab> findAllByHackerId(Long hackerId);

    // 만료된 랩 조회 (스케줄러용)
    List<HackLab> findAllByExpiresAtBefore(LocalDateTime now);

    // JPQL로 작성 (메서드 이름은 짧게 마음대로 지어도 됨)
    // "해커 이름이 일치하고, 상태가 STOPPED가 아닌 것 중 가장 최신 1개"
    @Query("SELECT h FROM HackLab h WHERE h.hacker.username = :username AND h.status <> :status ORDER BY h.createdAt DESC")
    List<HackLab> findRunningLabs(@Param("username") String username, @Param("status") LabStatus status, Pageable pageable);

    // [Default 메서드 활용] Service에서 쓰기 편하게 감싸주기 (Java 8+)
    default Optional<HackLab> findTopByHackerUsernameAndStatusNotOrderByCreatedAtDesc(String username, LabStatus status) {
        // PageRequest.of(0, 1) -> Limit 1 역할
        List<HackLab> result = findRunningLabs(username, status, PageRequest.of(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}