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

> Para correr o stack completo (modo recomendado) é necessário apenas Docker. O JDK e o Maven são necessários apenas para desenvolvimento local ou para reconstruir as imagens.

## Arranque com Docker (Stack Completo)

O projeto corre inteiramente em Docker com 7 serviços:

| Serviço | Descrição |
|---|---|
| `pgserver` | PostgreSQL — base de dados do monolito |
| `springbootapp` | Backend Spring Boot (monolito) |
| `entrega-db` | PostgreSQL — base de dados do serviço de entregas |
| `zookeeper` | Coordenação do Kafka |
| `kafka` | Broker de mensagens (tópicos: `pedido.pago`, `entrega.atribuida`, `entrega.status.atualizada`) |
| `entrega-service` | Microserviço de gestão de entregas |
| `nginx` | Reverse proxy (HTTP → `springbootapp:8082`, gRPC → `springbootapp:9092`) |

Nota para Linux/macOS (primeira execução):

- chmod +x mvnw

Na raiz do projeto:

Para fazer build completa e lançar pgserver:
- docker compose up --build -d 

Para lançar o pgserver com imagem já construída:
- docker compose up -d

Verificar estado de todos os serviços:
- docker compose ps

Parar e remover todos os containers:
- docker compose down

## Portas

### Modo Docker (acesso via nginx)

- Interface web: `http://localhost` (nginx na porta 80)
- gRPC (via nginx): `localhost:9090`

### Modo desenvolvimento local (sem Docker para a app)

- Interface web Spring Boot: `http://localhost:8082`
- Swagger/OpenAPI: `http://localhost:8082/swagger-ui/index.html`
- Servidor gRPC: `localhost:9092`

As portas `8082` e `9092` são usadas internamente para evitar conflitos. Em modo Docker, o nginx faz proxy das portas `80` e `9090` para elas.

### Arranque recomendado: `start-dev.bat`

No Windows, o fluxo mais seguro para backend + JavaFX é:

- `.\start-dev.bat`

O script faz isto:

1. corre `clean compile -DskipTests`
2. arranca o Spring Boot usando as classes já compiladas
3. arranca o cliente JavaFX sem recompilar protobuf/java outra vez

### Se alterar `.proto` ou contratos gRPC

Antes de testar, recompila tudo uma vez:

- Windows:
	- `.\mvnw.cmd -U clean generate-sources compile -DskipTests`
- Linux/macOS:
	- `./mvnw -U clean generate-sources compile -DskipTests`

Isto regenera:

- classes Protobuf em `target/generated-sources/protobuf/java`
- stubs gRPC em `target/generated-sources/protobuf/grpc-java`
- classes compiladas usadas pelo backend e pela app JavaFX

## Correr a Interface Web

### Modo Docker (recomendado)

1. Faz build e arranca todo o stack:
	- `docker compose up --build -d`
2. Abre no browser:
	- login web: `http://localhost`
	- Swagger: `http://localhost/swagger-ui/index.html`

### Modo desenvolvimento local

1. Garante que a base de dados está ativa:
	- `docker compose up -d pgserver`
2. Arranca o backend web:
	- Windows: `.\mvnw.cmd spring-boot:run`
	- Linux/macOS: `./mvnw spring-boot:run`
3. Abre no browser:
	- login web: `http://localhost:8082`
	- Swagger: `http://localhost:8082/swagger-ui/index.html`

Notas:

- O `spring-boot:run` está configurado no `pom.xml` para arrancar localmente na `8082` e o gRPC na `9092`.
- Em modo local, o Kafka e o serviço de entregas não estão ativos; para pipeline completa usa o modo Docker.

## Correr Backend + Interface Nativa JavaFX

Modo recomendado no Windows:

- `.\start-dev.bat`

Modo manual:
 
1. Terminal 1:
	- Windows: `.\mvnw.cmd spring-boot:run -Dmaven.main.skip=true -Dprotoc.skip=true -Dmaven.test.skip=true`
	- Linux/macOS: `./mvnw spring-boot:run -Dmaven.main.skip=true -Dprotoc.skip=true -Dmaven.test.skip=true`
2. Terminal 2:
	- Windows: `.\mvnw.cmd javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true`
	- Linux/macOS: `./mvnw javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true`

Notas:

- A BD (`pgserver`) deve estar ativa antes do arranque: `docker compose up -d pgserver`.
- Neste modo, o JavaFX fala com REST em `8082` e gRPC em `9092` diretamente (sem nginx).

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

Nos videos da fase 2 não fizemos clone, porque estávamos a ter problemas com o spring-boot:run a recompilar, e por estar em cima do prazo de entrega não fizemos o clone e não mostramos o build.

Fase 1 - `video/video.mp4`.
Fase 2:
Interface Web - `video/interf_web.mp4`
Interface Nativa - `video/interf_nativa.mp4` - Não foi colocado por tamanho muito grande, ver pelo link

Links:

## Fase1 
- https://youtu.be/FUYIWUyKIuU?t=35&is=BVrYl7sToJMr1iHH

## Fase 2 
- Interface Web - https://youtu.be/Gs2GXsSE2_E?is=RNlQV_ncfoPoOg8J
- Interface Nativa - https://youtu.be/OWqookbjX-8?is=1LCHZRDwA-wfNdFi