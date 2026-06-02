# TascaEats — Delivery de Comida
Fase 3 — Construção de Sistemas de Software (CSS) 2025/2026

## Grupo 18

|        Nome        | Número |
|--------------------|--------|
| Rafael Figueiredo  | 61813  |
| Guilherme Sarreira | 61860  |
| José Diogo         | 66014  |

## Pré-requisitos

- **Docker Desktop** (em execução)
- Java JDK 21 + Maven (opcional — apenas para compilação local)

## Arranque Rápido

### Modo Completo (Docker — Recomendado)

```bash
# Linux/macOS (primeira vez)
chmod +x mvnw

# Todos os SOs
docker compose up --build -d
```

**Acesso:**
- Web: http://localhost (porta 80)
- Swagger: http://localhost/swagger-ui/index.html

### Windows — Backend + JavaFX Local

```powershell
.\start-dev.bat
```

**Acesso:**
- Web: http://localhost:8082
- JavaFX: aplicação nativa

## Arquitetura

| Serviço | Descrição | Acesso |
|---|---|---|
| **Nginx** | Reverse Proxy (REST + gRPC) | localhost:80, 9090 |
| **Monólito** | Spring Boot (Backend) | localhost:8082 (dev) |
| **Microserviço de Entregas** | Gestão de entregadores | Interno:8081 |
| **Kafka** | Comunicação assíncrona | Interno |
| **PostgreSQL (monólito)** | BD monólito | Interno |
| **PostgreSQL (entregas)** | BD microserviço | Interno |

## Parar Tudo

```bash
docker compose down
```

## Testes (Opcional)

```bash
# Linux/macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

## Vídeo de Demonstração

[Inserir link do vídeo aqui]

**Demonstra:**
- Fluxo E2E: pagamento → Kafka → atribuição de entregador
- Isolamento de bases de dados
- Nginx como Reverse Proxy
- Atualização de estados de entrega
- CRUD de entregadores
