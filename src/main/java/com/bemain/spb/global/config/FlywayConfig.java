package com.bemain.spb.global.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

    @Bean
    // [중요] 이 설정이 있으면 서버 켤 때마다 DB를 싹 밀고 다시 만듭니다.
    // 배포 환경(prod)에서는 절대 켜지면 안 되므로 @Profile("local") 등을 붙이는 게 좋습니다.
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();   // 1. 싹 지우기 (DROP ALL)
            flyway.migrate(); // 2. 다시 만들기 (V1__init.sql 실행)
        };
    }
}