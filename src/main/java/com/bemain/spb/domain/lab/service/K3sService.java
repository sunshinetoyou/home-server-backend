package com.bemain.spb.domain.lab.service;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class K3sService {
    private final KubernetesClient k8sClient;

    // [핵심] 파드와 서비스를 띄우고 접속 도메인을 반환
    public String deployHackerLab(String uniqueName, String dockerImage, int port) {

        // 1. Deployment 생성
        Deployment deployment = createDeployment(uniqueName, dockerImage, port);
        k8sClient.apps().deployments().inNamespace("default").resource(deployment).serverSideApply();

        // 2. Service 생성 (풀 패키지 경로 사용)
        io.fabric8.kubernetes.api.model.Service service = createK8sService(uniqueName, port);
        k8sClient.services().inNamespace("default").resource(service).serverSideApply();

        // 3. 접속 주소 구성 (나중엔 Ingress Host를 여기서 설정)
        return "http://" + uniqueName + ".server.io";
    }

    // --- Private Helpers (내부 구현 상세) ---

    private Deployment createDeployment(String name, String image, int port) {
        return new DeploymentBuilder()
                .withNewMetadata().withName(name).withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewSelector().withMatchLabels(Map.of("app", name)).endSelector()
                .withNewTemplate()
                .withNewMetadata().withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(name)
                .withImage(image)
                .withImagePullPolicy("Always")
                .addNewPort().withContainerPort(port).endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private io.fabric8.kubernetes.api.model.Service createK8sService(String name, int port) {
        return new ServiceBuilder()
                .withNewMetadata().withName(name).withLabels(Map.of("app", name)).endMetadata()
                .withNewSpec()
                .withSelector(Map.of("app", name))
                .withType("ClusterIP")
                .addNewPort()
                .withProtocol("TCP")
                .withPort(80)
                .withTargetPort(new IntOrString(port))
                .endPort()
                .endSpec()
                .build();
    }
}
