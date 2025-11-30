package com.bemain.spb.domain.controller;

import com.bemain.spb.domain.entity.Images;
import com.bemain.spb.domain.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // 1. 챌린지 이미지 목록 확인
    // GET /api/image/all
    @GetMapping("/all")
    public ResponseEntity<List<Images>> getAllImages() {
        return ResponseEntity.ok(imageService.getAllImages());
    }

    // 2. 챌린지 이미지 다운로드 (URL 반환)
    // GET /api/image/{id}/download
    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadImage(@PathVariable Long id) {
        String url = imageService.getDownloadUrl(id);
        // 클라이언트에게 URL 문자열만 딱 줍니다.
        return ResponseEntity.ok(url);
    }
}