# syntax=docker/dockerfile:1.7

# ---------- Build stage ----------
# Ubuntu 기반 (alpine ARM64 manifest 누락 이슈 회피)
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version --no-daemon

COPY src ./src
# Spring Boot 가 기본으로 생성하는 plain jar 는 제거 (COPY 대상이 하나뿐이게).
RUN ./gradlew clean bootJar -x test --no-daemon \
    && rm -f build/libs/*-plain.jar

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system app && useradd --system --gid app app \
    && apt-get update && apt-get install -y --no-install-recommends tzdata curl \
    && ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/*.jar app.jar
RUN chown -R app:app /app
USER app

ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k -Duser.timezone=Asia/Seoul"
EXPOSE 8080

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]