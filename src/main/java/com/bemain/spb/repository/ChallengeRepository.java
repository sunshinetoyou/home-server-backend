package com.bemain.spb.repository;

import com.bemain.spb.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    // 기본 CRUD(저장, 조회 등)는 JpaRepository가 다 알아서 해줍니다.
}