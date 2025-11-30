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

    @Transactional
    public Long createLab(String username, LabCreateRequest request) {
        // 1. 개발자 조회
        User developer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 2. 템플릿(Images) 조회
        Images templateImage = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿 이미지가 없습니다."));

        // 3. [핵심 로직] 포트 결정 (Default vs Override)
        // 요청에 포트가 있으면 그걸 쓰고, 없으면 템플릿의 기본값 사용
        Integer finalPort = (request.getPort() != null)
                ? request.getPort()
                : templateImage.getDefaultPort();

        // 4. Lab 엔티티 생성 (Builder 사용)
        Lab lab = Lab.builder()
                .developer(developer)
                .image(templateImage)
                .title(request.getTitle())
                .description(request.getDescription())
                .dockerImage(request.getDockerImage()) // 개발자 제출 이미지
                .containerPort(finalPort)              // 결정된 포트
                .build();

        // 5. 저장
        return labRepository.save(lab).getId();
    }

    // 랩 실습 시작 (배포 요청)
    @Transactional
    public String startLabForHacker(Long labId, String username) {
        // 1. DB에서 정보 조회 (Business Logic)
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("랩을 찾을 수 없습니다."));
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2. 배포에 필요한 데이터 준비
        String uniqueName = "lab-" + labId + "-hacker-" + hacker.getId();
        String dockerImage = lab.getDockerImage();
        Integer containerPort = lab.getContainerPort();

        // 3. K3sService 호출 (Infrastructure Logic)
        // "K3s야, 이거 이름이랑 이미지 줄 테니까 띄워줘. 주소만 알려줘."
        String deployUrl = k3sService.deployHackerLab(uniqueName, dockerImage, containerPort);

        // 4. 결과 저장
        lab.setDeployUrl(deployUrl);

        return deployUrl;
    }

    public List<LabSummaryResponse> getActiveLabList() {
        return labRepository.findAllActiveLabsWithStats();
    }
}