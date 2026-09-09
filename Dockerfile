# 1. build
FROM eclipse-temurin:21-jdk AS builder
COPY . /app
WORKDIR /app
RUN chmod +x gradlew && ./gradlew bootJar

# 2. runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT [ "java", "-jar", "app.jar" ]