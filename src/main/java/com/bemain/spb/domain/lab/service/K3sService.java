package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.entity.DevLab;
import com.bemain.spb.domain.lab.entity.LabDbType;
import com.bemain.spb.domain.lab.entity.LabStatus;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class K3sService {

    private final KubernetesClient k8sClient;

    @Value("${app.k8s.namespace:default}")
    private String namespace;

    /**
     * 개발자 랩 (Honeypot) 배포
     * - 이름 규칙: lab-{id}-public
     */
    public String deployDevLab(DevLab devLab) {
        String uniqueName = "lab-" + devLab.getId() + "-public";
        return deployCommonLab(uniqueName, devLab);
    }

    /**
     * 해커 랩 배포
     * - 이름 규칙: lab-{id}-hacker-{uid}
     */
    public String deployHackLab(DevLab devLab, Long hackerId) {
        String uniqueName = "lab-" + devLab.getId() + "-hacker-" + hackerId;
        return deployCommonLab(uniqueName, devLab);
    }

    // 랩 삭제 (공통)
    public void deleteLab(String uniqueName) {
        k8sClient.apps().deployments().inNamespace("default").withName(uniqueName).delete();
        k8sClient.services().inNamespace("default").withName(uniqueName).delete();
        k8sClient.network().v1().ingresses().inNamespace("default").withName(uniqueName).delete();
    }

    // 특정 파드의 이벤트를 실시간으로 감시하여 SSE로 전송
    @Async
    public void watchPodEvents(String uniqueName, SseEmitter emitter) {
        // 해당 라벨(app=uniqueName)을 가진 파드를 감시
        k8sClient.pods().inNamespace(namespace)
                .withLabel("app", uniqueName)
                .watch(new Watcher<Pod>() {
                    @Override
                    public void eventReceived(Action action, Pod pod) {
                        try {
                            String phase = pod.getStatus().getPhase(); // Pending, Running, Failed...

                            // 1. 컨테이너 상태 상세 분석 (에러 감지)
                            if (pod.getStatus().getContainerStatuses() != null) {
                                for (var cs : pod.getStatus().getContainerStatuses()) {

                                    // 대기 중 (Waiting) - 이미지 에러 등
                                    if (cs.getState().getWaiting() != null) {
                                        String reason = cs.getState().getWaiting().getReason();
                                        String message = cs.getState().getWaiting().getMessage();

                                        if ("ErrImagePull".equals(reason) || "ImagePullBackOff".equals(reason)) {
                                            sendLog(emitter, "❌ 이미지 다운로드 실패: " + message);
                                            emitter.complete(); // 종료
                                            return;
                                        }
                                        if (!"ContainerCreating".equals(reason)) {
                                            sendLog(emitter, "⏳ 대기 중: " + reason);
                                        }
                                    }

                                    // 실행 중단 (Terminated) - 크래시
                                    if (cs.getState().getTerminated() != null) {
                                        String reason = cs.getState().getTerminated().getReason();
                                        if ("Error".equals(reason) || "CrashLoopBackOff".equals(reason)) {
                                            sendLog(emitter, "❌ 앱 실행 중 오류 발생 (Crash)!");
                                            // 앱 로그 20줄 긁어오기
                                            fetchAndSendAppLogs(uniqueName, emitter);
                                            emitter.complete();
                                            return;
                                        }
                                    }
                                }
                            }

                            // 2. 정상 실행 완료
                            if ("Running".equals(phase)) {
                                sendLog(emitter, "✅ 컨테이너 실행 완료. 접속 준비 중...");
                                emitter.send(SseEmitter.event().name("complete").data("DONE"));
                                emitter.complete();
                            }

                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onClose(WatcherException cause) {
                        if (cause != null) emitter.completeWithError(cause);
                    }
                });
    }

    public Pair<LabStatus, String> getPodDetailedStatus(String uniqueName) {
        try {
            // 1. 라벨로 파드 검색
            var pods = k8sClient.pods().inNamespace(namespace)
                    .withLabel("app", uniqueName)
                    .list().getItems();

            // 파드가 없으면 -> 이미 삭제되었거나 아직 안 만들어짐 (STOPPED 취급)
            if (pods.isEmpty()) {
                return Pair.of(LabStatus.STOPPED, "Not Found");
            }

            // 가장 최신 파드 하나만 확인
            Pod pod = pods.get(0);
            String phase = pod.getStatus().getPhase(); // Pod Phase (Pending, Running...)

            // 2. 컨테이너 상세 상태 분석 (에러 우선 감지)
            if (pod.getStatus().getContainerStatuses() != null) {
                for (var cs : pod.getStatus().getContainerStatuses()) {

                    // A. 대기 중 (Waiting) 상태 확인
                    if (cs.getState().getWaiting() != null) {
                        String reason = cs.getState().getWaiting().getReason(); // ContainerCreating, ErrImagePull...

                        // [치명적 에러] 즉시 ERROR 리턴
                        if ("ErrImagePull".equals(reason)
                                || "ImagePullBackOff".equals(reason)
                                || "CrashLoopBackOff".equals(reason)
                                || "CreateContainerConfigError".equals(reason)) {
                            return Pair.of(LabStatus.ERROR, reason);
                        }

                        // [일반 대기] 아직 켜지는 중 -> PENDING 리턴
                        return Pair.of(LabStatus.PENDING, reason);
                    }

                    // B. 종료됨 (Terminated) 상태 확인
                    if (cs.getState().getTerminated() != null) {
                        String reason = cs.getState().getTerminated().getReason();
                        // 정상 종료(Completed)가 아니면 에러로 간주
                        if (!"Completed".equals(reason)) {
                            return Pair.of(LabStatus.ERROR, reason); // Error, OOMKilled 등
                        }
                    }
                }
            }

            // 3. 컨테이너 이슈가 없다면 Phase 기준 매핑
            if ("Running".equals(phase)) {
                // 모든 컨테이너가 Running이고 Ready 상태인지 더 정교하게 볼 수도 있지만,
                // 실습용으로는 Phase가 Running이면 충분합니다.
                return Pair.of(LabStatus.RUNNING, "Running");
            }
            if ("Pending".equals(phase)) {
                return Pair.of(LabStatus.PENDING, "Pending");
            }
            if ("Failed".equals(phase)) {
                return Pair.of(LabStatus.ERROR, "Failed");
            }
            if ("Succeeded".equals(phase)) {
                return Pair.of(LabStatus.STOPPED, "Completed");
            }

            // 그 외 알 수 없는 상태
            return Pair.of(LabStatus.PENDING, "Initializing");

        } catch (Exception e) {
            log.error("K3s status check failed: {}", uniqueName, e);
            // K3s 연결 실패 등 예외 발생 시 ERROR 처리
            return Pair.of(LabStatus.ERROR, "Connection Error");
        }
    }
    // --- Private Helpers (공통 로직) ---

    private String deployCommonLab(String uniqueName, DevLab blueprint) {
        String hostDomain = uniqueName + ".server.io";

        // 1. Deployment 생성 (DevLab 설계도 반영)
        Deployment deployment = createDeploymentSpec(uniqueName, blueprint);
        k8sClient.apps().deployments().inNamespace("default").resource(deployment).serverSideApply();

        // 2. Service 생성
        io.fabric8.kubernetes.api.model.Service service = createK8sService(uniqueName);
        k8sClient.services().inNamespace("default").resource(service).serverSideApply();

        // 3. Ingress 생성
        Ingress ingress = createIngress(uniqueName, hostDomain);
        k8sClient.network().v1().ingresses().inNamespace("default").resource(ingress).serverSideApply();

        return "http://" + hostDomain;
    }

    private Deployment createDeploymentSpec(String name, DevLab lab) {

        // 1. Frontend 컨테이너 (항상 존재)
        Container frontendContainer = new ContainerBuilder()
                .withName("frontend")
                .withImage(lab.getFeImage())
                .withImagePullPolicy("Always")
                .withPorts(new ContainerPortBuilder().withContainerPort(80).build()) // 보통 FE는 80 or 3000
                // FE가 같은 파드 내의 BE를 호출할 때 (localhost 사용 가능)
                // 만약 Nginx로 서빙한다면 이 설정 대신 nginx.conf가 필요할 수 있음
                .withEnv(new EnvVar("REACT_APP_API_URL", "http://localhost:8080", null))
                .build();

        // 2. Backend 컨테이너 빌더 준비 (환경변수가 달라지므로 빌더 상태로 시작)
        List<EnvVar> backendEnv = new ArrayList<>();
        // 기본 포트 설정
        backendEnv.add(new EnvVar("SERVER_PORT", "8080", null));

        // ---------------------------------------------------------
        // 🚀 DB 타입에 따른 로직 분기 (Polymorphic Logic)
        // ---------------------------------------------------------
        List<Container> containers = new ArrayList<>();
        containers.add(frontendContainer); // FE 추가

        if (lab.getDbType() == LabDbType.CONTAINER_IMAGE) {
            // [Case A] 3-Tier: 별도 DB 컨테이너 띄우기
//            log.info("Deploying 3-Tier Lab (Container DB): {}", name);

            // 2-1. DB 컨테이너 추가
            Container dbContainer = new ContainerBuilder()
                    .withName("database")
                    .withImage(lab.getDbSource()) // 예: "postgres:15"
                    .withImagePullPolicy("Always")
                    .withPorts(new ContainerPortBuilder().withContainerPort(5432).build()) // 기본 포트 가정
                    // DB 이미지에 따라 필요한 기본 환경변수 (예시: Postgres)
                    .withEnv(new EnvVar("POSTGRES_PASSWORD", "lab_password", null),
                            new EnvVar("POSTGRES_USER", "lab_user", null),
                            new EnvVar("POSTGRES_DB", "lab_db", null))
                    .build();

            containers.add(dbContainer);

            // 2-2. Backend에 DB 접속 정보 주입 (localhost로 접속!)
            // 한 파드(Pod) 내의 컨테이너들은 localhost를 공유합니다.
            backendEnv.add(new EnvVar("DB_URL", "jdbc:postgresql://localhost:5432/lab_db", null));
            backendEnv.add(new EnvVar("DB_USERNAME", "lab_user", null));
            backendEnv.add(new EnvVar("DB_PASSWORD", "lab_password", null));

        } else if (lab.getDbType() == LabDbType.SQLITE_SCRIPT) {
            // [Case B] 2-Tier: Backend 내부 SQLite + 초기화 스크립트 주입
//            log.info("Deploying 2-Tier Lab (SQLite Script): {}", name);

            // 2-1. DB 컨테이너는 추가하지 않음 (containers.add 안 함)

            // 2-2. Backend에 스크립트 주입
            backendEnv.add(new EnvVar("DB_TYPE", "sqlite", null));
            // 주의: EnvVar 길이 제한이 있을 수 있으므로, 실제 운영에선 ConfigMap 마운트가 권장됨.
            // 하지만 간단한 실습용 스크립트라면 Env로 충분함.
            backendEnv.add(new EnvVar("DB_INIT_SQL", lab.getDbSource(), null));
        }

        // 3. Backend 컨테이너 완성 및 추가
        Container backendContainer = new ContainerBuilder()
                .withName("backend")
                .withImage(lab.getBeImage())
                .withImagePullPolicy("Always")
                .withPorts(new ContainerPortBuilder().withContainerPort(8080).build())
                .withEnv(backendEnv) // 위에서 구성한 환경변수 주입
                .build();

        containers.add(backendContainer);

        // ---------------------------------------------------------
        // Deployment 객체 조립
        // ---------------------------------------------------------
        return new DeploymentBuilder()
                .withNewMetadata()
                .withName(name)
                .withLabels(Map.of("app", name)) // Label Selector용
                .endMetadata()
                .withNewSpec()
                .withReplicas(1) // 랩은 기본 1개
                .withNewSelector()
                .withMatchLabels(Map.of("app", name))
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(Map.of("app", name))
                .endMetadata()
                .withNewSpec()
                .withContainers(containers) // FE + BE (+ DB)
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private io.fabric8.kubernetes.api.model.Service createK8sService(String name) {
        return new ServiceBuilder()
                .withNewMetadata().withName(name).withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec()
                .withSelector(Map.of("app", name))
                .withType("ClusterIP")
                .addNewPort()
                .withProtocol("TCP")
                .withPort(80)
                .withTargetPort(new IntOrString(80))
                .endPort()
                .endSpec()
                .build();
    }

    private Ingress createIngress(String name, String host) {
        return new IngressBuilder()
                .withNewMetadata().withName(name).endMetadata()
                .withNewSpec()
                .addNewRule()
                .withHost(host)
                .withNewHttp()
                .addNewPath()
                .withPath("/")
                .withPathType("Prefix")
                .withNewBackend()
                .withNewService()
                .withName(name)
                .withNewPort().withNumber(80).endPort()
                .endService()
                .endBackend()
                .endPath()
                .endHttp()
                .endRule()
                .endSpec()
                .build();
    }

    // 앱 로그 긁어오기 (크래시 났을 때)
    private void fetchAndSendAppLogs(String uniqueName, SseEmitter emitter) {
        try {
            var pods = k8sClient.pods().inNamespace(namespace).withLabel("app", uniqueName).list().getItems();
            if (!pods.isEmpty()) {
                String podName = pods.get(0).getMetadata().getName();
                String logs = k8sClient.pods().inNamespace(namespace)
                        .withName(podName)
                        .tailingLines(20)
                        .getLog();
                sendLog(emitter, "=== Application Logs ===");
                sendLog(emitter, logs);
            }
        } catch (Exception e) {
            try { sendLog(emitter, "로그 조회 실패: " + e.getMessage()); } catch (IOException ignored) {}
        }
    }

    // [보안] 로그 전송 및 마스킹
    private void sendLog(SseEmitter emitter, String text) throws IOException {
        if (text == null) return;
        String safeText = sanitizeLog(text); // 마스킹 적용
        for (String line : safeText.split("\n")) {
            emitter.send(SseEmitter.event().name("log").data(line));
        }
    }

    // 비밀번호 마스킹 로직
    private String sanitizeLog(String rawLog) {
        return rawLog.replaceAll("(?i)(password|pwd|secret|token|key)([:=]\\s*)([^\\s]*)", "$1$2*****");
    }
}