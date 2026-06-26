# 1. build
FROM eclipse-temurin:21-jdk AS builder
COPY . /app
WORKDIR /app
RUN gradle bootJar

# 2. runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]