# Pre-build: ./mvnw -pl content-service -am package -DskipTests
FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/content-service.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
