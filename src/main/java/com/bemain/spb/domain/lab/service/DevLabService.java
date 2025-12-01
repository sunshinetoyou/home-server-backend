package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.dto.DevLabCreateRequest;
import com.bemain.spb.domain.lab.dto.DevLabListResponse;
import com.bemain.spb.domain.lab.dto.DevLabResponse;
import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.repository.DevLabRepository;
import com.bemain.spb.domain.tag.entity.Tag;
import com.bemain.spb.domain.tag.repository.TagRepository;
import com.bemain.spb.domain.user.entity.RoleType;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevLabService {

    private final DevLabRepository devLabRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final K3sService k3sService;

    // 랩 등록 (DB 저장 + K8s 배포)
    @Transactional
    public Long createLab(String username, DevLabCreateRequest request) {
        User developer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        if (developer.getRole() != RoleType.DEVELOPER && developer.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("개발자만 랩을 등록할 수 있습니다.");
        }

        // 필수 이미지 검증 로직
        validateImages(request.getFeImage(), request.getBeImage());

        // 1. 엔티티 생성
        DevLab lab = DevLab.builder()
                .developer(developer)
                .title(request.getTitle())
                .description(request.getDescription())
                .feImage(request.getFeImage())
                .beImage(request.getBeImage())
                .dbImage(request.getDbImage())
                .build();

        // 2. 태그 연결
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            lab.getTags().addAll(tags);
        }

        lab = devLabRepository.save(lab); // ID 확보

        // 3. [변경] K3s 배포 (DevLab 객체 전달)
        try {
            // 변경된 K3sService 메소드 호출
            String publicUrl = k3sService.deployDevLab(lab);

            lab.setPublicUrl(publicUrl);

        } catch (Exception e) {
            System.err.println("Preview 배포 실패: " + e.getMessage());
            // 필요 시 예외 throw
        }

        return lab.getId();
    }

    // [Helper] 이미지 유효성 검사
    private void validateImages(String fe, String be) {
        if (!StringUtils.hasText(fe)) {
            throw new IllegalArgumentException("Frontend 이미지는 필수입니다.");
        }
        if (!StringUtils.hasText(be)) {
            throw new IllegalArgumentException("Backend 이미지는 필수입니다.");
        }
        // DB 이미지는 없어도 됨 (SQLite 모드)
    }

    // 랩 목록 조회 (태그 필터링 지원)
    @Transactional(readOnly = true)
    public List<DevLabListResponse> getLabs(String tagName) {
        List<DevLab> labs;

        if (tagName != null && !tagName.isBlank()) {
            labs = devLabRepository.findByTagName(tagName);
        } else {
            labs = devLabRepository.findAllByIsActiveTrueOrderByCreatedAtDesc();
        }

        return labs.stream()
                .map(DevLabListResponse::new)
                .collect(Collectors.toList());
    }

    // 랩 상세 조회
    @Transactional(readOnly = true)
    public DevLabResponse getLab(Long labId) {
        DevLab lab = devLabRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("랩을 찾을 수 없습니다."));
        return new DevLabResponse(lab);
    }

    // 랩 삭제
    @Transactional
    public void deleteLab(Long labId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        DevLab lab = devLabRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));

        // 권한 체크 (작성자 본인 또는 관리자만 삭제 가능)
        if (!lab.getDeveloper().getId().equals(user.getId()) && user.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 1. K3s 리소스(Preview Pod) 정리
        // 생성할 때 규칙: "lab-" + lab.getId() + "-public"
        String uniqueName = "lab-" + lab.getId() + "-public";
        k3sService.deleteLab(uniqueName);

        // 2. DB 삭제 (Cascade 설정에 의해 Tag 연결 정보 등도 정리됨)
        devLabRepository.delete(lab);
    }
}