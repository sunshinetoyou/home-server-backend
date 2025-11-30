package com.bemain.spb.global.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class K8sConfig {

    @Bean
    public KubernetesClient kubernetesClient() {
        // Pod 내부에서 실행될 때, 자동으로 ServiceAccount 토큰을 읽어서 연결합니다.
        return new KubernetesClientBuilder().build();
    }
}