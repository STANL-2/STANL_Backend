# Base image
FROM openjdk:17-jdk-alpine

# Jar file 복사
COPY build/libs/*.jar /app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Expose 포트
EXPOSE 8080