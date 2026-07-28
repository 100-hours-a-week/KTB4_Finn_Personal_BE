# 1단계: 빌드
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN mkdir -p /app/data /app/uploads

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]