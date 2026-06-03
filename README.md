# TascaEats — Delivery de Comida
Fase 3 — Construção de Sistemas de Software (CSS) 2025/2026

## Grupo 18

|        Nome        | Número |
|--------------------|--------|
| Rafael Figueiredo  | 61813  |
| Guilherme Sarreira | 61860  |
| José Diogo         | 66014  |

### Execução

```bash
# Linux/macOS (primeira vez)
chmod +x mvnw

# Todos os SOs
docker compose up --build -d
```

**Interface Web e Swagger:**
- Web: http://localhost (porta 80)
- Swagger: http://localhost/swagger-ui/index.html

### Iniciar a Interface Nativa (JavaFX)

Para iniciar a interface nativa, temos duas formas: com toda a infraestrutura Docker já ligada ou do zero.

#### Opção 1: Com o `docker compose up` a correr 
Se já estão os serviços Docker a correr (Nginx, Monólito, Microserviço, Kafka, etc.), executar apenas o javafx:

```bash
# Windows
.\mvnw.cmd javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true

# Linux/macOS
./mvnw javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true
```
**Acesso Web e JavaFX:**
- Web: `http://localhost` (proxy porta 80)
- JavaFX: Liga-se automaticamente ao Nginx (porta 9090)

#### Opção 2: Do Zero (Com Docker desligado)
Se não está nenhum contentor a correr (certificar com `docker compose down`), usar o script de desenvolvimento que levanta apenas a BD e o Kafka, compila e lança o Monólito e o JavaFX localmente.

```bash
# Garantir que tudo está desligado primeiro
docker compose down
```

```powershell
# No Windows
.\start-dev.bat
```

```bash
# No Linux/macOS (equivalente ao start-dev.bat)
docker compose up -d pgserver zookeeper kafka
./mvnw clean compile -DskipTests
./mvnw spring-boot:run -Dmaven.main.skip=true -Dprotoc.skip=true -Dmaven.test.skip=true &
./mvnw javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true
```

**Acesso nesta opção:**
- Web: `http://localhost:8082` (direto ao Monólito, sem Nginx)
- JavaFX: Aplicação abre e liga-se diretamente ao Monólito (porta 9092)


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

## Vídeos

## Fase1 
- Repositório `video/video.mp4`
- https://youtu.be/FUYIWUyKIuU?t=35&is=BVrYl7sToJMr1iHH

## Fase 2 
### Interface Web 
- Repositório `video/interf_web.mp4`
- Interface Web - https://youtu.be/Gs2GXsSE2_E?is=RNlQV_ncfoPoOg8J

### Interface Nativa 
- Interface Nativa - https://youtu.be/OWqookbjX-8?is=1LCHZRDwA-wfNdFi
- `video/interf_nativa.mp4` - Não foi colocado por tamanho muito grande, ver pelo link

## Fase 3
- Repositório `video/video3.mp4`
- https://youtu.be/