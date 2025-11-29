package com.bemain.spb.service;

import com.bemain.spb.entity.Challenge;
import com.bemain.spb.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    // 1. 모든 템플릿 목록 조회
    @Transactional(readOnly = true)
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    // 2. 특정 템플릿 다운로드 링크 조회
    @Transactional(readOnly = true)
    public String getDownloadUrl(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
        return challenge.getImageUrl();
    }
}