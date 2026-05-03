# TascaEats — Delivery de Comida
Fase 2 — Construção de Sistemas de Software (CSS) 2025/2026

## Grupo 18

|        Nome        | Número |
|--------------------|--------|
| Rafael Figueiredo  | 61813  |
| Guilherme Sarreira | 61860  |
| José Diogo         | 66014  |

## Pré-requisitos

- Java JDK 21 (projeto configurado para 21)
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- PowerShell (Windows)
- Docker Desktop (ou Docker Engine) + Docker Compose

## Arranque da Base de Dados (Docker)

Nota para Linux/macOS (primeira execução):

- chmod +x mvnw

Na raiz do projeto:

Para fazer build completa e lançar pgserver:
- docker compose up --build -d 

Para lançar o pgserver com imagem já construída:
- docker compose up -d pgserver

Verificar estado:
- docker compose ps

Parar e remover container da BD:
- docker compose down

## Build (Obrigatório para Fase 2)

Sempre que houver alterações no gRPC/proto, correr este comando antes de testar:

- Windows:
	- .\mvnw.cmd -U clean generate-sources compile -DskipTests
- Linux/macOS:
	- ./mvnw -U clean generate-sources compile -DskipTests

Este passo é necessário para:
- gerar classes Protobuf em `target/generated-sources/protobuf/java`
- gerar stubs gRPC em `target/generated-sources/protobuf/grpc-java`
- compilar backend e cliente JavaFX com tipos atualizados

## Executar Fase 2 — Interface Web

Na raiz do projeto:

- Windows:
	- .\mvnw.cmd spring-boot:run
- Linux/macOS:
	- ./mvnw spring-boot:run

Depois de arrancar, aceder no browser a:

- Web app (login): http://localhost:8081
- REST/Swagger: http://localhost:8081/swagger-ui/index.html

Nota: Spring Boot está configurado para porta 8081 porque Docker usa a porta 8080.

## Executar Fase 2 — Interface Nativa JavaFX

Modo suportado: backend e cliente em processos separados.

1. Terminal 1: iniciar backend Spring/gRPC

- Windows:
	- .\mvnw.cmd spring-boot:run
- Linux/macOS:
	- ./mvnw spring-boot:run

2. Terminal 2: iniciar app JavaFX

- Windows:
	- .\mvnw.cmd javafx:run
- Linux/macOS:
	- ./mvnw javafx:run

Nota:
- A BD (`pgserver`) deve estar ativa via Docker Compose antes de iniciar.

## Testes
Validação recomendada para Fase 2 (funcional/compilação):

- Windows:
	- .\mvnw.cmd test
- Linux/macOS:
	- ./mvnw test

Cobertura (regra de qualidade no `pom.xml`):

- Windows:
	- .\mvnw.cmd verify
- Linux/macOS
	- ./mvnw verify

## Vídeo

Fase 1 - `video/video.mp4`.
Fase 2:
Interface Web - `video/interf_web.mp4`
Interface Nativa - `video/interf_nativa.mp4`

Links:

## Fase1 
- https://youtu.be/FUYIWUyKIuU?t=35&is=BVrYl7sToJMr1iHH

## Fase 2 
- Interface Web - 
- Interface Nativa - 