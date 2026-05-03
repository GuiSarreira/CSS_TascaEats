FROM maven:3.9.14-eclipse-temurin-21 AS builder
WORKDIR /workspace
ADD pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package

FROM docker.io/eclipse-temurin:21-jre
WORKDIR /app
VOLUME /tmp
RUN mkdir -p /app/
RUN mkdir -p /app/logs/
COPY --from=builder /workspace/target/tascaeats-1.0.jar /app/app.jar
EXPOSE 8080 8082
ENTRYPOINT ["java","-jar", "/app/app.jar"]