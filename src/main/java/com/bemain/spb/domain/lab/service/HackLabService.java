package com.bemain.spb.domain.lab.service;

import org.springframework.data.util.Pair;
import com.bemain.spb.domain.lab.dto.HackLabResponse;
import com.bemain.spb.domain.lab.dto.HackLabStatusResponse;
import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.entity.HackLab;
import com.bemain.spb.domain.lab.entity.LabStatus;
import com.bemain.spb.domain.lab.repository.DevLabRepository;
import com.bemain.spb.domain.lab.repository.HackLabRepository;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HackLabService {

    private final HackLabRepository hackLabRepository;
    private final DevLabRepository devLabRepository;
    private final UserRepository userRepository;
    private final K3sService k3sService;

    // 1. 실습 시작 (파드 할당)
    @Transactional
    public HackLabResponse startLab(Long devLabId, String username) {
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 이미 실행 중인 랩이 있는지 확인
        hackLabRepository.findByHackerId(hacker.getId()).ifPresent(existingLab -> {
            throw new IllegalStateException(
                    "이미 실행 중인 실습(" + existingLab.getDevLab().getTitle() + ")이 있습니다. " +
                            "먼저 종료(Stop) 후 새로운 실습을 시작해주세요."
            );
        });

        DevLab devLab = devLabRepository.findById(devLabId)
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));

        // DevLab 무결성 검사
        if (!devLab.isActive()) {
            throw new IllegalStateException("비활성화된 랩입니다.");
        }
        if (!StringUtils.hasText(devLab.getFeImage()) || !StringUtils.hasText(devLab.getBeImage())) {
            throw new IllegalStateException("해당 랩의 이미지 정보가 손상되었습니다. 관리자에게 문의하세요.");
        }

        HackLab hackLab = HackLab.builder()
                .hacker(hacker)
                .devLab(devLab)
                .expiresAt(LocalDateTime.now())
                .build();

        // POD 생성 규칙
        String uniqueName = "lab-" + devLabId + "-hacker-" + hacker.getId();
        k3sService.deleteLab(uniqueName);

        String accessUrl = k3sService.deployHackLab(devLab, hacker.getId());

        hackLab.setUrl(accessUrl);
        hackLab.setExpiresAt(LocalDateTime.now().plusHours(2));
        hackLabRepository.save(hackLab);

        return new HackLabResponse(hackLab);
    }

    // 실습 수동 종료
    @Transactional
    public void stopLab(Long devLabId, String username) {
        User hacker = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        HackLab hackLab = hackLabRepository.findByHackerId(hacker.getId())
                .filter(lab -> lab.getDevLab().getId().equals(devLabId))
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 실습이 아닙니다."));

        String uniqueName = "lab-" + devLabId + "-hacker-" + hacker.getId();
        k3sService.deleteLab(uniqueName);

        hackLabRepository.delete(hackLab);
    }

    // 3. 내 실습 상태 조회
    @Transactional // 상태 변경(Dirty Checking)이 일어날 수 있으므로 필수
    public HackLabStatusResponse getMyStatus(String username) {
        // 1. 현재 내 계정으로 실행 중인(STOPPED가 아닌) 랩 조회
        HackLab hackLab = hackLabRepository.findTopByHackerUsernameAndStatusNotOrderByCreatedAtDesc(
                username, LabStatus.STOPPED
        ).orElse(null);

        // 실행 중인 랩이 없으면 null 반환 (Controller에서 204 No Content 처리)
        if (hackLab == null) {
            return null;
        }

        // 2. K3s 파드 상태 조회 (실시간 정보)
        // 파드 이름 규칙: lab-{devLabId}-hacker-{hacker_id}
        String uniqueName = getUniqueName(hackLab);

        // K3sService에서 (Enum 상태, 상세 문자열) 쌍을 받아옴
        Pair<LabStatus, String> k3sInfo = k3sService.getPodDetailedStatus(uniqueName);

        LabStatus realStatus = k3sInfo.getFirst();   // 실제 K8s 상태 (예: RUNNING, ERROR)
        String detailMessage = k3sInfo.getSecond();  // 상세 메시지 (예: "Running", "CrashLoopBackOff")

        // 3. 상태 동기화 (Sync)
        // DB 상태와 실제 K8s 상태가 다르면 DB를 업데이트합니다.
        // 예: DB는 PENDING인데, 이미지는 다운로드 실패(ERROR)했거나, 배포가 완료(RUNNING)된 경우
        if (hackLab.getStatus() != realStatus) {
            hackLab.setStatus(realStatus);

            // 만약 RUNNING이 되었다면 URL 등 추가 세팅이 필요할 수 있음
            // (보통 URL은 start 시점에 세팅하지만, 로드밸런서 IP 할당 방식이라면 여기서 할 수도 있음)
        }

        // 4. 응답 DTO 생성
        // 에러 상태일 때만 failureMessage 필드에 상세 내용을 담아줌
        String failureMsg = (realStatus == LabStatus.ERROR) ? detailMessage : null;

        return new HackLabStatusResponse(
                hackLab.getId(),
                hackLab.getStatus(), // Sync된 최신 상태
                detailMessage,       // 화면에 보여줄 날것의 상태 텍스트
                failureMsg,          // 에러 메시지 (에러 아니면 null)
                hackLab.getUrl()     // 접속 URL
        );
    }

    // 4. [Scheduler] 만료된 랩 자동 회수 (매 1분마다)
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void cleanupExpiredLabs() {
        List<HackLab> expiredLabs = hackLabRepository.findAllByExpiresAtBefore(LocalDateTime.now());

        for (HackLab lab : expiredLabs) {
            String uniqueName = "lab-" + lab.getDevLab().getId() +
                    "-hacker-" + lab.getHacker().getId();

            k3sService.deleteLab(uniqueName);
            hackLabRepository.delete(lab);

            System.out.println("자동 반납 완료: " + uniqueName);
        }
    }

    @Async // 반드시 비동기로 실행되어야 함
    @Transactional(readOnly = true)
    public void streamLogs(Long hackLabId, String username, SseEmitter emitter) {
        try {
            // 1. 엔티티 조회 (작성하신 HackLab 사용)
            HackLab hackLab = hackLabRepository.findById(hackLabId)
                    .orElseThrow(() -> new IllegalArgumentException("실습 정보를 찾을 수 없습니다."));

            // 2. 권한 체크 (본인 랩인지 확인)
            if (!hackLab.getHacker().getUsername().equals(username)) {
                sendErrorAndClose(emitter, "권한이 없습니다.");
                return;
            }

            // 3. 파드 이름 조합 (규칙: lab-{devLabId}-hacker-{hackLabId})
            // DevLab 엔티티와의 연관관계를 이용
            String uniqueName = getUniqueName(hackLab);

            // 4. 초기 메시지 전송
            sendToEmitter(emitter, "시스템: 실습 환경(" + uniqueName + ") 로그 연결 중...");

            // 5. K3s 감시 시작 (Blocking)
            k3sService.watchPodEvents(uniqueName, emitter);

        } catch (Exception e) {
            sendErrorAndClose(emitter, "로그 스트리밍 중 에러: " + e.getMessage());
        }
    }

    // ====== helper ======
    private void sendToEmitter(SseEmitter emitter, String msg) throws IOException {
        emitter.send(SseEmitter.event().name("log").data(msg));
    }

    private void sendErrorAndClose(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(msg));
            emitter.completeWithError(new RuntimeException(msg));
        } catch (IOException ignored) {}
    }

    private String getUniqueName(HackLab hackLab) {
        return "lab-" + hackLab.getDevLab().getId() + "-hacker-" + hackLab.getHacker().getId();
    }
}