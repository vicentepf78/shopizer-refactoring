# Pre-build: ./mvnw -pl reference-service -am package -DskipTests
FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/reference-service.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
