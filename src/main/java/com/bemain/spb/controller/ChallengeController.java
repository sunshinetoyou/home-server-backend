package com.bemain.spb.controller;

import com.bemain.spb.entity.Challenge;
import com.bemain.spb.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chall")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    // 1. 챌린지 이미지 목록 확인
    // GET /api/chall/images
    @GetMapping("/images")
    public ResponseEntity<List<Challenge>> getAllImages() {
        return ResponseEntity.ok(challengeService.getAllChallenges());
    }

    // 2. 챌린지 이미지 다운로드 (URL 반환)
    // GET /api/chall/image/{id}/download
    @GetMapping("/image/{id}/download")
    public ResponseEntity<String> downloadImage(@PathVariable Long id) {
        String url = challengeService.getDownloadUrl(id);
        // 클라이언트에게 URL 문자열만 딱 줍니다.
        return ResponseEntity.ok(url);
    }
}