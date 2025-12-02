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

        DevLab devLab = devLabRepository.findById(devLabId)
                .orElseThrow(() -> new IllegalArgumentException("랩 없음"));

        // 1. [Get-or-Create] 껍데기 확보 (핵심 로직 통합)
        HackLab hackLab = hackLabRepository.findByHackerAndDevLab(hacker, devLab)
                .orElseGet(() -> {
                    // 없으면 새로 생성 (Shell 생성)
                    HackLab newShell = HackLab.builder()
                            .devLab(devLab)
                            .hacker(hacker)
                            .expiresAt(LocalDateTime.now().plusHours(1))
                            .build();
                    newShell.setStatus(LabStatus.STOPPED);
                    return hackLabRepository.save(newShell);
                });

        // 2. 이미 실행 중인지 체크
        if (hackLab.getStatus() == LabStatus.RUNNING) {
            return new HackLabResponse(hackLab);
        }

        // 3. 상태 초기화 (재시작 준비)
        // 기존에 에러가 있었더라도, 재시도 하는 거니까 PENDING으로 변경하고 에러 로그 지움
        hackLab.prepareForStart();

        // 4. 배포 시도 (에러 발생 시 DB에 기록)
        try {
            // K3s 배포 요청 (리소스 생성 자체는 빠름)
            String url = k3sService.deployHackLab(devLab, hackLab.getId());

            // 성공 시 URL 세팅 (상태는 PENDING 유지 -> SSE/Polling으로 Running 확인)
            hackLab.setUrl(url);

        } catch (Exception e) {
            hackLab.markAsError("배포 요청 실패: " + e.getMessage());
        }

        // 5. 결과 리턴
        // 프론트엔드는 이 응답의 ID를 가지고 바로 /logs SSE를 연결하면 됨
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

    @Async
    @Transactional(readOnly = true)
    public void streamLogs(Long hackLabId, String username, SseEmitter emitter) {
        try {
            HackLab hackLab = hackLabRepository.findById(hackLabId).orElseThrow();

            // 권한 체크
            if (!hackLab.getHacker().getUsername().equals(username)) {
                sendErrorAndClose(emitter, "권한이 없습니다.");
                return;
            }

            // 1. [Dead Log] 이미 에러로 죽어있는 상태라면? -> DB 기록 보여주고 끝냄
            if (hackLab.getStatus() == LabStatus.ERROR) {
                sendToEmitter(emitter, "❌ 이전 배포가 실패했습니다.");
                sendToEmitter(emitter, "--- 저장된 에러 로그 ---");

                String savedLog = hackLab.getLastErrorLog();
                if (savedLog != null) {
                    for (String line : savedLog.split("\n")) {
                        sendToEmitter(emitter, line);
                    }
                } else {
                    sendToEmitter(emitter, "저장된 상세 로그가 없습니다.");
                }

                emitter.complete(); // 연결 종료
                return;
            }

            // 2. [Live Log] 살아있거나(RUNNING) 뜨는 중(PENDING)이라면? -> K3s 연결
            String uniqueName = "lab-" + hackLab.getDevLab().getId() + "-hacker-" + hackLab.getId();
            sendToEmitter(emitter, "시스템: 실시간 로그 연결 중...");

            k3sService.watchPodEvents(uniqueName, emitter);

        } catch (Exception e) {
            sendErrorAndClose(emitter, "로그 스트리밍 에러: " + e.getMessage());
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