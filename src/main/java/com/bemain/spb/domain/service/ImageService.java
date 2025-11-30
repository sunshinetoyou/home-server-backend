package com.bemain.spb.domain.service;

import com.bemain.spb.domain.entity.Images;
import com.bemain.spb.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    // 1. 모든 템플릿 목록 조회
    @Transactional(readOnly = true)
    public List<Images> getAllImages() {
        return imageRepository.findAll();
    }

    // 2. 특정 템플릿 다운로드 링크 조회
    @Transactional(readOnly = true)
    public String getDownloadUrl(Long id) {
        Images images = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("템플릿을 찾을 수 없습니다."));
        return images.getImageUrl();
    }
}