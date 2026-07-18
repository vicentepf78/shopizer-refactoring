# Pre-build: ./mvnw -pl tax-service -am package -DskipTests
FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/tax-service.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
