package com.bemain.spb.domain.lab.service;

import com.bemain.spb.domain.lab.entity.DevLab;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class K3sService {

    private final KubernetesClient k8sClient;

    /**
     * [Public] 개발자 프리뷰(Honeypot) 배포
     * - DevLab 등록 시 자동 호출
     * - 이름 규칙: lab-{id}-public
     */
    public String deployDevLab(DevLab devLab) {
        String uniqueName = "lab-" + devLab.getId() + "-public";
        return deployCommonLab(uniqueName, devLab);
    }

    /**
     * 해커 실습 인스턴스 배포
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
        // 빌더 시작
        DeploymentBuilder builder = new DeploymentBuilder()
                .withNewMetadata().withName(name).withLabels(Map.of("app", name)).endMetadata();

        // Pod Spec 구성
        var podSpec = builder.withNewSpec()
                .withReplicas(1)
                .withNewSelector().withMatchLabels(Map.of("app", name)).endSelector()
                .withNewTemplate()
                .withNewMetadata().withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec();

        // (옵션) Private Registry Secret 사용 시 주석 해제
        // .withImagePullSecrets(new LocalObjectReference("lab-registry-secret"));

        // [Container 1] Frontend
        podSpec.addNewContainer()
                .withName("frontend")
                .withImage(lab.getFeImage()) // DevLab 의존
                .withImagePullPolicy("Always")
                .addNewPort().withContainerPort(80).endPort()
                .addNewEnv().withName("BACKEND_URL").withValue("http://localhost:8080").endEnv()
                .endContainer();

        // [Container 2] Backend
        var backendContainer = podSpec.addNewContainer()
                .withName("backend")
                .withImage(lab.getBeImage()) // DevLab 의존
                .withImagePullPolicy("Always")
                .addNewPort().withContainerPort(8080).endPort();

        // [Container 3] Database (조건부 생성)
        if (lab.getDbImage() != null && !lab.getDbImage().isBlank()) {
            // A. DB 이미지 모드 (3-Tier)
            backendContainer
                    .addNewEnv().withName("DB_URL").withValue("jdbc:postgresql://localhost:5432/labdb").endEnv()
                    .addNewEnv().withName("DB_USER").withValue("hacker").endEnv()
                    .addNewEnv().withName("DB_PASS").withValue("hacker").endEnv();

            backendContainer.endContainer(); // BE 닫기

            // DB 컨테이너 추가
            podSpec.addNewContainer()
                    .withName("database")
                    .withImage(lab.getDbImage())
                    .withImagePullPolicy("Always")
                    .addNewPort().withContainerPort(5432).endPort()
                    .addNewEnv().withName("POSTGRES_DB").withValue("labdb").endEnv()
                    .addNewEnv().withName("POSTGRES_USER").withValue("hacker").endEnv()
                    .addNewEnv().withName("POSTGRES_PASSWORD").withValue("hacker").endEnv()
                    .withNewResources()
                    .withRequests(Map.of("memory", new Quantity("128Mi")))
                    .withLimits(Map.of("memory", new Quantity("256Mi")))
                    .endResources()
                    .endContainer(); // DB 닫기

        } else {
            // B. SQLite 모드 (2-Tier)
            backendContainer.addNewEnv().withName("DB_TYPE").withValue("sqlite").endEnv();
            backendContainer.endContainer(); // BE 닫기
        }

        // 최종 빌드
        return podSpec.endSpec().endTemplate().endSpec().build();
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
}