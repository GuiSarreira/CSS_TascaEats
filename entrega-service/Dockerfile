# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9.14-eclipse-temurin-21 AS builder
WORKDIR /workspace
# Cache de dependências (só refaz se pom.xml mudar)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia código-fonte e compila
COPY . .
RUN mvn clean package -DskipTests

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
VOLUME /tmp
RUN mkdir -p /app/logs/
COPY --from=builder /workspace/target/entrega-service-1.0.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
