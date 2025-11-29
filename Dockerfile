# 1. 실행 단계 (가벼운 런타임 이미지 사용)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# 2. 밖에서 빌드된 JAR 파일을 복사 (경로 주의!)
# GitHub Actions가 빌드한 파일은 build/libs 폴더에 생깁니다.
COPY build/libs/*.jar app.jar

# 3. 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]