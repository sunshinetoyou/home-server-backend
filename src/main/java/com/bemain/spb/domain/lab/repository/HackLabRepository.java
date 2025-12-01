package com.bemain.spb.domain.lab.repository;

import com.bemain.spb.domain.lab.entity.HackLab;
import org.springframework.data.jpa.repository.JpaRepository;

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
}