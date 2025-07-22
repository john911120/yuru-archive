# 1. build
FROM gradle:8.4.0-jdk-17 AS builder
COPY . /app
WORKDIR /app
RUN gradle bootJar

# 2. runtime
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]