package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.dto.LabCreateRequest;
import com.bemain.spb.domain.lab.dto.LabSummaryResponse;
import com.bemain.spb.domain.image.entity.Images;
import com.bemain.spb.domain.lab.entity.Lab;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.image.repository.ImageRepository;
import com.bemain.spb.domain.lab.repository.LabRepository;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabService {

    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final K3sService k3sService;

    // 랩 등록
    @Transactional
    public Long createLab(String username, LabCreateRequest request) {
        User developer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Images template = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿 없음"));

        Lab lab = Lab.builder()
                .developer(developer)
                .image(template)
                .title(request.getTitle())
                .description(request.getDescription())
                // [New] 3-Tier 이미지 정보 저장
                .feImage(request.getFeImage())
                .beImage(request.getBeImage())
                .dbImage(request.getDbImage())
                .build();

        return labRepository.save(lab).getId();
    }

    // 실습 시작
    @Transactional
    public String startLabForHacker(Long labId, String username) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        String uniqueName = "lab-" + labId + "-hacker-" + hacker.getId();

        // [New] K3sService에 3개 이미지 모두 전달
        String deployUrl = k3sService.deploy3TierLab(
                uniqueName,
                lab.getFeImage(),
                lab.getBeImage(),
                lab.getDbImage()
        );

        lab.setDeployUrl(deployUrl);
        return deployUrl;
    }

    public List<LabSummaryResponse> getActiveLabList() {
        return labRepository.findAllActiveLabsWithStats();
    }
}