# 1. 빌드 단계
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 소스 코드 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# 빌드 실행
RUN gradle clean build -x test --no-daemon

# 2. 실행 단계 (여기를 변경했습니다! openjdk -> eclipse-temurin)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 단계에서 만들어진 jar 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]