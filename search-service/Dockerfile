# Pre-build: ./mvnw -pl search-service -am package -DskipTests
FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/search-service.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
