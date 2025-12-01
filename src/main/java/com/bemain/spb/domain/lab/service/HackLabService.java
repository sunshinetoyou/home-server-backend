package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.dto.HackLabResponse;
import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.entity.HackLab;
import com.bemain.spb.domain.lab.repository.DevLabRepository;
import com.bemain.spb.domain.lab.repository.HackLabRepository;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

        // [중요] 기존 파드 정리 (Clean Start)
        // 규칙: "lab-{devLabId}-hacker-{hackerId}"
        String uniqueName = "lab-" + devLabId + "-hacker-" + hacker.getId();
        k3sService.deleteLab(uniqueName);

        // [변경] K3s 배포 (DevLab 객체 + Hacker ID 전달)
        // K3sService 내부에서 DevLab 설정을 보고 똑같이 파드를 띄워줌
        String accessUrl = k3sService.deployHackLab(devLab, hacker.getId());

        // DB 업데이트
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
    @Transactional(readOnly = true)
    public HackLabResponse getMyStatus(Long devLabId, String username) {
        User hacker = userRepository.findByUsername(username).orElseThrow();

        return hackLabRepository.findByHackerId(hacker.getId())
                .map(HackLabResponse::new)
                .orElse(null); // 실습 중 아님
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
}