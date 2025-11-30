package com.bemain.spb.domain.lab.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; // Spring Service

import java.util.Map;

@Service
@RequiredArgsConstructor
public class K3sService {

    private final KubernetesClient k8sClient;

    public String deploy3TierLab(String uniqueName, String feImg, String beImg, String dbImg) {
        String hostDomain = uniqueName + ".server.io";

        // 1. Deployment 생성 (1 Pod, Multi-Container)
        Deployment deployment = createDeployment(uniqueName, feImg, beImg, dbImg);
        k8sClient.apps().deployments().inNamespace("default").resource(deployment).serverSideApply();

        // 2. Service 생성 (풀 패키지 경로 사용)
        io.fabric8.kubernetes.api.model.Service service = createK8sService(uniqueName);
        k8sClient.services().inNamespace("default").resource(service).serverSideApply();

        // 3. Ingress 생성
        Ingress ingress = createIngress(uniqueName, hostDomain);
        k8sClient.network().v1().ingresses().inNamespace("default").resource(ingress).serverSideApply();

        return "http://" + hostDomain;
    }

    // --- Private Methods ---

    private Deployment createDeployment(String name, String feImg, String beImg, String dbImg) {

        // 1. 빌더 시작 (여기까진 DeploymentBuilder)
        DeploymentBuilder builder = new DeploymentBuilder()
                .withNewMetadata().withName(name).withLabels(Map.of("app", name)).endMetadata();

        // 2. Pod Spec 단계로 진입 (여기서부터 타입이 복잡해지므로 var 사용 추천)
        // var podSpec은 PodSpecFluent.SpecNested 타입이 됩니다.
        var podSpec = builder.withNewSpec()
                .withReplicas(1)
                .withNewSelector().withMatchLabels(Map.of("app", name)).endSelector()
                .withNewTemplate()
                .withNewMetadata().withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec(); // <--- Pod Spec 내부 진입
                // .withImagePullSecrets(new LocalObjectReference("lab-registry-secret")); 나중에 시크릿값 k3s 넣어주면 사용

        // 3. [Container 1] Frontend 추가
        podSpec.addNewContainer()
                .withName("frontend")
                .withImage(feImg)
                .withImagePullPolicy("Always")
                .addNewPort().withContainerPort(80).endPort()
                .addNewEnv().withName("BACKEND_URL").withValue("http://localhost:8080").endEnv()
                .endContainer(); // 컨테이너 설정 끝

        // 4. [Container 2] Backend 시작 (아직 닫지 않음)
        // 변수에 담아서 조건에 따라 환경변수를 더 넣을 수 있게 함
        var backendContainer = podSpec.addNewContainer()
                .withName("backend")
                .withImage(beImg)
                .withImagePullPolicy("Always")
                .addNewPort().withContainerPort(8080).endPort();

        // 5. [조건부 로직] DB 이미지 유무에 따라 분기
        if (dbImg != null && !dbImg.isEmpty()) {
//            // (A) DB 모드: 백엔드에 DB 접속 정보 주입
//            backendContainer
//                    .addNewEnv().withName("DB_URL").withValue("jdbc:postgresql://localhost:5432/labdb").endEnv()
//                    .addNewEnv().withName("DB_USER").withValue("hacker").endEnv()
//                    .addNewEnv().withName("DB_PASS").withValue("hacker").endEnv();
//
//            backendContainer.endContainer(); // Backend 설정 닫기
//
//            // (B) DB 컨테이너 추가
//            podSpec.addNewContainer()
//                    .withName("database")
//                    .withImage(dbImg)
//                    .withImagePullPolicy("Always")
//                    .addNewPort().withContainerPort(5432).endPort()
//                    .addNewEnv().withName("POSTGRES_DB").withValue("labdb").endEnv()
//                    .addNewEnv().withName("POSTGRES_USER").withValue("hacker").endEnv()
//                    .addNewEnv().withName("POSTGRES_PASSWORD").withValue("hacker").endEnv()
//                    .withNewResources()
//                    .withRequests(Map.of("memory", new Quantity("128Mi")))
//                    .withLimits(Map.of("memory", new Quantity("256Mi")))
//                    .endResources()
//                    .endContainer(); // DB 설정 닫기
            throw new UnsupportedOperationException(
                    "현재 홈서버 리소스 제한으로 인해 '별도 DB 컨테이너(3-Tier)' 모드는 지원하지 않습니다. db_image를 비워서 'SQLite 모드'로 요청해주세요."
            );
        } else {
            // (C) SQLite 모드
            backendContainer.addNewEnv().withName("DB_TYPE").withValue("sqlite").endEnv();
            backendContainer.endContainer(); // Backend 설정 닫기
        }

        // 6. 모든 설정 닫고 빌드 (Build)
        // PodSpec -> PodTemplate -> DeploymentSpec -> DeploymentBuilder -> Deployment
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
                .withPort(80) // Service 외부 포트
                .withTargetPort(new IntOrString(80)) // Frontend 포트로 연결
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