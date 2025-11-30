package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.dto.LabCreateRequest;
import com.bemain.spb.domain.lab.dto.LabStatusResponse;
import com.bemain.spb.domain.image.entity.Images;
import com.bemain.spb.domain.lab.entity.Lab;
import com.bemain.spb.domain.lab.entity.LabAssignment;
import com.bemain.spb.domain.lab.repository.LabAssignmentRepository;
import com.bemain.spb.domain.report.entity.ReportStatus;
import com.bemain.spb.domain.report.repository.ReportRepository;
import com.bemain.spb.domain.user.entity.Role;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.image.repository.ImageRepository;
import com.bemain.spb.domain.lab.repository.LabRepository;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabService {

    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ReportRepository reportRepository;
    private final K3sService k3sService;
    private final LabAssignmentRepository labAssignmentRepository;

    // 1. 랩 등록 (DB 저장 + Public Pod 즉시 배포)
    @Transactional
    public Long createLab(String username, LabCreateRequest request) {
        User developer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Images template = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new IllegalArgumentException("템플릿 없음"));

        // A. 엔티티 생성
        Lab lab = Lab.builder()
                .developer(developer)
                .image(template)
                .title(request.getTitle())
                .description(request.getDescription())
                .feImage(request.getFeImage())
                .beImage(request.getBeImage())
                .dbImage(request.getDbImage())
                .build();

        lab = labRepository.save(lab); // ID 생성을 위해 먼저 저장

        // B. Public Pod (Preview/Honeypot) 배포
        // 이름 규칙: lab-{id}-public
        String uniqueName = "lab-" + lab.getId() + "-public";

        try {
            String deployurl = k3sService.deploy3TierLab(
                    uniqueName,
                    lab.getFeImage(),
                    lab.getBeImage(),
                    lab.getDbImage()
            );

            // C. DB의 deploy_url에 '공개용 주소' 저장
            lab.setDeployUrl(deployurl);

        } catch (Exception e) {
            // 배포 실패 시 로그를 남기거나 예외 처리
            throw new RuntimeException("랩 Public 배포 실패: " + e.getMessage(), e);
        }

        return lab.getId();
    }

    // 2. 실습 시작
    @Transactional
    public String allocationLab(Long labId, String username) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 할당 정보 조회 없으면 생성 (수강 신청 개념)
        LabAssignment assignment = labAssignmentRepository.findByUserIdAndLabId(hacker.getId(), labId)
                .orElseGet(() -> labAssignmentRepository.save(new LabAssignment(hacker, lab)));

        // 이미 실행 중인 컨테이너가 있다면? -> 재사용하거나 삭제 후 재생성
        // 여기서는 "기존 거 지우고 새로 생성 (시간 초기화)" 정책으로 갑니다.
        String uniqueName = "lab-" + labId + "-hacker-" + hacker.getId();
        k3sService.deleteLab(uniqueName); // 기존 것 삭제 (멱등성)

        // 배포
        String accessUrl = k3sService.deploy3TierLab(
                uniqueName, lab.getFeImage(), lab.getBeImage(), lab.getDbImage()
        );

        // [핵심] DB 업데이트 (URL 및 만료 시간 2시간 뒤로 설정)
        assignment.setAccessUrl(accessUrl);
        assignment.setExpiresAt(LocalDateTime.now().plusHours(2));

        // Transactional에 의해 자동 저장됨
        return accessUrl;
    }

    // 3. 실습 종료 (수동 반납)
    @Transactional
    public void stopLab(Long labId, String username) {
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        LabAssignment assignment = labAssignmentRepository.findByUserIdAndLabId(hacker.getId(), labId)
                .orElseThrow(() -> new IllegalArgumentException("실습 중이 아닙니다."));

        // K8s 리소스 삭제
        String uniqueName = "lab-" + labId + "-hacker-" + hacker.getId();
        k3sService.deleteLab(uniqueName);

        // DB 정보 초기화 (URL과 만료시간을 날림 -> '미사용' 상태)
        assignment.setAccessUrl(null);
        assignment.setExpiresAt(null);
    }

    // 4. 스케줄러: 만료된 랩 자동 회수 (매 1분마다 실행)
    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    public void cleanupExpiredLabs() {
        List<LabAssignment> expiredList = labAssignmentRepository.findAllByExpiresAtBefore(LocalDateTime.now());

        for (LabAssignment assignment : expiredList) {
            String uniqueName = "lab-" + assignment.getLab().getId() +
                    "-hacker-" + assignment.getUser().getId();

            // K8s 삭제
            k3sService.deleteLab(uniqueName);

            // DB 초기화
            assignment.setAccessUrl(null);
            assignment.setExpiresAt(null);

            System.out.println("자동 반납 완료: " + uniqueName);
        }
    }


    // Lab CRUD
    @Transactional(readOnly = true)
    public LabStatusResponse getLabStatus() {
        // 1. Live Targets: 현재 Running 상태인 랩 개수 조회
        long liveTargets = labRepository.countByIsActiveTrue();

        // 2. Bugs Found: 전체 리포트 누적 개수
        long bugsFound = reportRepository.count();

        // 3. Active Hunters: 접속 중인 유저
        long activeHunters = labAssignmentRepository.countByExpiresAtAfter(LocalDateTime.now());

        // 4. Threat Level 계산 로직 (비즈니스 로직)
        // 예: 활성화된 랩이 많거나 리포트가 많으면 위험도 상승
        String threatLevel = calculateThreatLevel(bugsFound, reportRepository.countByStatusIn(List.of(ReportStatus.PENDING, ReportStatus.IN_PROGRESS)));

        return LabStatusResponse.builder()
                .liveTargets(liveTargets)
                .threatLevel(threatLevel)
                .bugsFound(bugsFound)
                .activeHunters(activeHunters)
                .build();
    }

    // 위험도 계산 알고리즘 (나중에 더 복잡하게 고도화 가능)
    private String calculateThreatLevel(long bugged, long bugging) {
        if (bugged == 0) {
            return "Low"; // 데이터가 없으면 안전한 것으로 간주
        }

        var p = (double) bugging / bugged;

        if (p > 0.9) {
            return "Critical";
        } else if (p > 0.7) {
            return "High";
        } else if (p > 0.4) {
            return "Medium";
        } else {
            return "Low";
        }
    }
}