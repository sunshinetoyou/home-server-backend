package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.dto.*;
import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.entity.LabDbType;
import com.bemain.spb.domain.lab.repository.DevLabRepository;
import com.bemain.spb.domain.tag.entity.Tag;
import com.bemain.spb.domain.tag.repository.TagRepository;
import com.bemain.spb.domain.user.entity.RoleType;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevLabService {

    private final DevLabRepository devLabRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final K3sService k3sService;

    // 랩 등록
    @Transactional
    public Long createLab(String username, DevLabCreateRequest request) {
        User developer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 1. 엔티티 생성 (Draft 상태)
        DevLab lab = DevLab.builder()
                .developer(developer)
                .title(request.getTitle())
                .description(request.getDescription())
                .feImage(request.getFeImage())
                .beImage(request.getBeImage())
                .dbType(request.getDbType())
                .dbSource(request.getDbSource())
                .build();

        // 2. 태그 연결
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            lab.getTags().addAll(tags);
        }

        // 3. 조건이 충족되면 자동으로 활성화 (Auto-Activate)
        // create 때는 실패해도 에러 내지 않고 그냥 비활성 상태로 둠
        if (canActivate(lab)) {
            lab.setActive(true);
        }

        lab = devLabRepository.save(lab);

        // 4. 활성화 상태라면 배포
        if (lab.isActive()) {
            try {
                String publicUrl = k3sService.deployDevLab(lab);
                lab.setPublicUrl(publicUrl);
            } catch (Exception e) {
                System.err.println("초기 배포 실패: " + e.getMessage());
                lab.setActive(false);
                lab.setPublicUrl(null);
            }
        }

        return lab.getId();
    }

    // 랩 목록 조회
    @Transactional(readOnly = true)
    public List<DevLabListResponse> getLabs(String tagName) {
        if (tagName != null && !tagName.isBlank()) {
            return devLabRepository.findByTagName(tagName).stream()
                    .map(DevLabListResponse::new).collect(Collectors.toList());
        }
//        return devLabRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().stream()
//                .map(DevLabListResponse::new).collect(Collectors.toList());
        return devLabRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DevLabListResponse::new).collect(Collectors.toList());
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
        DevLab lab = validateAndGetLab(labId, username);

        // 1. K3s 리소스 정리
        String uniqueName = "lab-" + lab.getId() + "-public";
        k3sService.deleteLab(uniqueName);

        // 2. DB 삭제
        devLabRepository.delete(lab);
    }

    // 기본 정보 수정 (DB만 수정)
    @Transactional
    public void updateInfo(Long labId, String username, DevLabInfoUpdateRequest request) {
        DevLab lab = validateAndGetLab(labId, username);

        if (StringUtils.hasText(request.getTitle())) lab.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) lab.setDescription(request.getDescription());

        // 태그 업데이트
        if (request.getTagIds() != null) {
            lab.getTags().clear();
            if (!request.getTagIds().isEmpty()) {
                lab.getTags().addAll(tagRepository.findAllById(request.getTagIds()));
            }
        }
    }

    // 이미지 수정 (재배포 필요)
    @Transactional
    public void updateImages(Long labId, String username, DevLabImagesUpdateRequest request) {
        DevLab lab = validateAndGetLab(labId, username);

        boolean changed = false;
        if (request.getFeImage() != null) { lab.setFeImage(request.getFeImage()); changed = true; }
        if (request.getBeImage() != null) { lab.setBeImage(request.getBeImage()); changed = true; }
        if (request.getDbType() != null) { lab.setDbType(request.getDbType()); changed = true; }
        if (request.getDbSource() != null) { lab.setDbSource(request.getDbSource()); changed = true; }

        // 변경사항이 있고 + 현재 활성화 상태라면 -> 조건을 다시 검사하고 재배포
        if (changed && lab.isActive()) {
            if (canActivate(lab)) {
                redeployPublicLab(lab);
            } else {
                lab.setActive(false);
                k3sService.deleteLab("lab-" + lab.getId() + "-public");
                lab.setPublicUrl(null);
            }
        }
    }

    // 상태 변경 (활성화/비활성화)
    @Transactional
    public void updateStatus(Long labId, String username, DevLabStatusUpdateRequest request) {
        DevLab lab = validateAndGetLab(labId, username);

        if (request.getIsActive() != null && lab.isActive() != request.getIsActive()) {

            if (request.getIsActive()) {
                if (!canActivate(lab)) {
                    throw new IllegalStateException("필수 이미지(FE/BE)와 테이블 구조(Schema) 설명이 모두 입력되어야 활성화할 수 있습니다.");
                }

                lab.setActive(true);
                redeployPublicLab(lab);
            }
            else {
                lab.setActive(false);
                k3sService.deleteLab("lab-" + lab.getId() + "-public");
                lab.setPublicUrl(null);
            }
        }
    }

    // [New] 랩 명시적 배포 (Draft -> Active)
    // POST /api/v1/labs/{id}/deploy
    @Transactional
    public void deployLab(Long labId, String username) {
        // 1. 랩 조회 및 권한 체크
        DevLab lab = validateAndGetLab(labId, username);

        // 2. 배포 가능한 상태인지 검증 (이미지, DB설정 등)
        if (!canActivate(lab)) {
            throw new IllegalStateException("배포를 위한 필수 정보(이미지, DB설정 등)가 부족합니다.");
        }

        // 3. 상태 활성화
        lab.setActive(true);

        // 4. K3s 배포 (기존 redeploy 메소드 재활용)
        redeployPublicLab(lab);
    }

    @Async // 비동기 필수
    @Transactional(readOnly = true)
    public void streamDeployLogs(Long labId, String username, SseEmitter emitter) {
        try {
            // 1. 랩 조회 및 권한 체크 (기존 메서드 활용)
            DevLab lab = validateAndGetLab(labId, username);

            // 2. [New] 활성화 상태(isActive) 검증
            // 비활성(Draft) 상태라면 배포된 파드가 없으므로 로그를 볼 수 없음
            if (!lab.isActive()) {
                sendToEmitter(emitter, "⚠️ 현재 '작성 중(Draft)' 상태입니다.");
                sendToEmitter(emitter, "⚠️ 랩을 '활성화'해야 배포가 시작됩니다.");
                emitter.complete(); // 연결 종료
                return;
            }

            // 2. 파드 이름 규칙 (DevLab은 Public Preview임)
            // 규칙: "lab-" + lab.getId() + "-public"
            String uniqueName = "lab-" + lab.getId() + "-public";

            // 3. 초기 메시지
            emitter.send(SseEmitter.event().name("log").data("시스템: [" + lab.getTitle() + "] 배포 로그 연결됨..."));

            // 4. K3s 감시 시작 (이미 만들어둔 K3sService 재사용! 꿀이득 🍯)
            k3sService.watchPodEvents(uniqueName, emitter);

        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("에러: " + e.getMessage()));
                emitter.completeWithError(e);
            } catch (IOException ignored) {}
        }
    }

    // ====== Helper Methods ======

    // 검증 로직 (Boolean 반환)
    private boolean canActivate(DevLab lab) {
        // 1. 필수 이미지 체크
        if (!StringUtils.hasText(lab.getFeImage()) || !StringUtils.hasText(lab.getBeImage())) {
            return false;
        }

        // 2. DB 설정 필수
        if (lab.getDbType() == null || !StringUtils.hasText(lab.getDbSource())) {
            return false;
        }

        // 3. DB 타입별 간단한 정합성 체크 (선택 사항)
        if (lab.getDbType() == LabDbType.CONTAINER_IMAGE) {
            // 이미지 타입인데 SQL문("CREATE ...") 같은게 들어오면 안 됨 (줄바꿈 체크 등)
            if (lab.getDbSource().trim().contains("\n") || lab.getDbSource().toUpperCase().startsWith("CREATE")) {
                return false;
            }
        } else if (lab.getDbType() == LabDbType.SQLITE_SCRIPT) {
            // TODO 입력값 검증 로직 필요
            // SQL 스크립트인데 너무 짧으면 의심
            if (lab.getDbSource().trim().length() < 5) {
                return false;
            }
        }
        return true;
    }

    private DevLab validateAndGetLab(Long labId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        DevLab lab = devLabRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랩입니다."));

        if (!lab.getDeveloper().getId().equals(user.getId()) && user.getRole() != RoleType.ADMIN) {
            throw new IllegalArgumentException("해당 랩을 수정할 권한이 없습니다.");
        }
        return lab;
    }

    private void redeployPublicLab(DevLab lab) {
        String uniqueName = "lab-" + lab.getId() + "-public";
        k3sService.deleteLab(uniqueName); // 기존 삭제
        String url = k3sService.deployDevLab(lab); // 신규 배포
        lab.setPublicUrl(url);
    }

    private void sendToEmitter(SseEmitter emitter, String msg) throws IOException {
        emitter.send(SseEmitter.event().name("log").data(msg));
    }
}