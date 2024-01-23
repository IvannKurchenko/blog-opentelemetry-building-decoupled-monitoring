# Run with: docker build -t products-service .
FROM gradle:8.5.0-jdk21-alpine AS builder
RUN mkdir -p /usr/src/products-service
COPY . /usr/src/products-service
WORKDIR /usr/src/products-service
RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /usr/src/products-service/build/libs/products-service-0.0.1-SNAPSHOT.jar /usr/app/products-service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/products-service.jar"]