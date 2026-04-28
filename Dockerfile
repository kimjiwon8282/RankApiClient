# 1단계: Gradle로 Spring Boot 실행 jar 생성
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# 2단계: 실행용 JRE 이미지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Seoul

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]