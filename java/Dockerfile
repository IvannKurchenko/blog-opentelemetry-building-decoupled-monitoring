# Run with: docker build -t products-service .

# Builder image with gradle
FROM gradle:8.5.0-jdk21-alpine AS builder
RUN mkdir -p /usr/src/products-service
COPY . /usr/src/products-service
WORKDIR /usr/src/products-service
RUN ./gradlew bootJar

# Main application image that depends on builder image to fetch ready to use jar from.
FROM eclipse-temurin:21-jre-alpine

# Download OpenTelemetry
ARG OTEL_VERSION=v2.0.0
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$OTEL_VERSION/opentelemetry-javaagent.jar

COPY --from=builder /usr/src/products-service/build/libs/products-service-0.0.1-SNAPSHOT.jar /usr/app/products-service.jar
EXPOSE 8080

# Request `GET /health` to make sure service is and running. App won't get started if it is failed to establish connections
# with any dependencies like elastic or Postgres.
RUN apk add curl
HEALTHCHECK --interval=1s --retries=3 --timeout=1s --start-period=5s CMD curl -f http://localhost:8080/health > /dev/null 2>&1 || exit 1

# Add java agnet to enable telemetry instrumentation
ENTRYPOINT ["java","-javaagent:opentelemetry-javaagent.jar","-jar","/usr/app/products-service.jar"]