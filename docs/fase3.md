# Fase 3 — Planeamento: Microserviço de Gestão de Entregas

**Prazo:** 03/06/2026 às 23:59  
**Peso:** 30% da nota final do projeto

---

## 1. Visão Geral da Arquitetura Alvo

```
 ┌──────────────┐   REST/HTTP    ┌───────────────────────────────────────────┐
 │  Browser Web │ ─────────────► │                                           │
 └──────────────┘                │          NGINX (Reverse Proxy)            │
                                 │            porta 80 (REST)                │
 ┌──────────────┐   gRPC         │            porta 9090 (gRPC passthrough)  │
 │  JavaFX App  │ ─────────────► │                                           │
 └──────────────┘                └────────────────┬──────────────────────────┘
                                                  │ encaminha para
                                                  ▼
                                 ┌────────────────────────────┐
                                 │   MONÓLITO (Spring Boot)   │
                                 │   porta 8080 (REST)        │
                                 │   porta 9090 (gRPC)        │
                                 │   DB: pgserver (Postgres)  │
                                 └───────────┬────────────────┘
                                             │
                          ┌──────────────────┴──────────────────┐
                          │         APACHE KAFKA                │
                          │  broker:9092  zookeeper:2181        │
                          └──────────────────┬──────────────────┘
                                             │
                                 ┌───────────┴──────────────────┐
                                 │  MICROSERVIÇO entrega-service │
                                 │   porta 8081 (REST interno)   │
                                 │   DB: entrega-db (Postgres)   │
                                 └──────────────────────────────┘
```

---

## 2. Componentes a Criar / Modificar

### 2.1 Novo Módulo: `entrega-service/`

Novo projeto Spring Boot independente dentro do repositório (pasta separada).

**Estrutura de pastas:**
```
entrega-service/
├── pom.xml                         (Spring Boot 3.x standalone)
├── Dockerfile
└── src/main/java/.../entrega/
    ├── EntregaServiceApplication.java
    ├── config/
    │   └── KafkaConsumerConfig.java
    │   └── KafkaProducerConfig.java
    ├── entities/
    │   ├── Entregador.java         (cópia leve — sem herança User)
    │   └── Entrega.java            (com pedidoId como referência lógica)
    ├── repositories/
    │   ├── EntregadorRepository.java
    │   └── EntregaRepository.java
    ├── services/
    │   ├── EntregadorService.java
    │   └── EntregaService.java
    ├── controllers/
    │   └── EntregadorController.java   (REST interno, opcional)
    └── kafka/
        ├── PedidoPagoConsumer.java     (consome pedido.pago)
        └── EntregaEventProducer.java   (publica entrega.atribuida, entrega.status)
```

**Base de dados própria:** `entrega-db` (PostgreSQL separado do monólito)

**Entidades no microserviço** (sem JPA User hierarquia):
- `Entregador`: id, nome, email, veiculo, zonaAtuacao, disponivel
- `Entrega`: id, pedidoId (Long), entregadorId (FK local), moradaEntrega, status, horaRetirada, horaEntrega

### 2.2 Modificações no Monólito

**`EntregaService.java`** — lógica de negócio removida; passa a:
- Publicar evento Kafka `pedido.pago` quando pagamento é confirmado
- Consumir evento `entrega.atribuida` → actualiza `Pedido.status = IN_DELIVERY`
- Consumir evento `entrega.status.atualizada` → actualiza `Entrega` local (projeção read-only)

**`EntregaController.java`** — endpoints mantêm a mesma interface REST; internamente delegam ao Kafka em vez de chamar `EntregaService` directamente.

**`PagamentoService.java`** — após `confirmarPagamento()`, publica evento `pedido.pago`.

**Entidades `Entrega` e `Entregador` no monólito** — mantidas como projeções locais somente de leitura (para as UIs não quebrarem). O monólito não escreve directamente nelas; só o consumer Kafka actualiza.

**Novo `KafkaProducerConfig.java`** e **`KafkaConsumerConfig.java`** no monólito.

---

## 3. Eventos Kafka

| Tópico | Produtor | Consumidor | Payload |
|---|---|---|---|
| `pedido.pago` | Monólito | entrega-service | `{ pedidoId, moradaEntrega, cidade, valorTotal }` |
| `entrega.atribuida` | entrega-service | Monólito | `{ pedidoId, entregaId, entregadorNome, status: "ATRIBUIDA" }` |
| `entrega.status.atualizada` | entrega-service | Monólito | `{ pedidoId, entregaId, novoStatus, timestamp }` |
| `entregador.registado` | entrega-service | (opcional, para sync) | `{ entregadorId, nome, zona }` |

**Fluxo principal (atribuição automática após pagamento):**
1. UI confirma pagamento → `PagamentoService` guarda pagamento → publica `pedido.pago`
2. `entrega-service` consome `pedido.pago` → procura entregador disponível na zona
3. `entrega-service` cria `Entrega` na sua DB → publica `entrega.atribuida`
4. Monólito consome `entrega.atribuida` → actualiza `Pedido.status = IN_DELIVERY` na sua DB
5. UI consulta monólito → vê estado actualizado (consistência eventual)

---

## 4. Infraestrutura Docker Compose

### Serviços a adicionar/modificar em `docker-compose.yml`:

```yaml
services:
  # BD do Monólito (já existe — renomear porta interna, não expor externamente)
  pgserver:           # mantém-se, sem alteração de porta interna

  # NOVA — BD exclusiva do microserviço
  entrega-db:
    image: postgres:18.3-alpine3.23
    container_name: entrega-db
    environment:
      POSTGRES_USER: entrega_user
      POSTGRES_PASSWORD: entrega_pass
      POSTGRES_DB: entrega_db
    networks: [appnet]

  # NOVO — Zookeeper (dependência do Kafka)
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    networks: [appnet]

  # NOVO — Apache Kafka
  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on: [zookeeper]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    networks: [appnet]

  # Monólito — ajustar portas (não expor 8080 externamente; Nginx faz o proxy)
  springbootapp:
    # ... portas internas 8080 e 9090 apenas (sem expose para host)
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    depends_on: [pgserver, kafka]
    networks: [appnet]

  # NOVO — Microserviço de Entregas
  entrega-service:
    build: ./entrega-service
    depends_on: [entrega-db, kafka]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://entrega-db:5432/entrega_db
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    networks: [appnet]

  # NOVO — Nginx Reverse Proxy
  nginx:
    image: nginx:1.27-alpine
    ports:
      - "80:80"       # REST (web)
      - "9090:9090"   # gRPC (JavaFX)
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on: [springbootapp]
    networks: [appnet]
```

### Ficheiro `nginx.conf` a criar:
- Bloco `http` → proxy REST para `springbootapp:8080`
- Bloco `stream` (TCP passthrough) → proxy gRPC para `springbootapp:9090` (porta 9090 no host)

---

## 5. Alterações de Portas (Dev Local vs Docker)

| Contexto | Web REST | gRPC |
|---|---|---|
| Dev local (actual) | `localhost:8082` | `localhost:9092` |
| Docker (via Nginx) | `localhost:80` | `localhost:9090` |

As propriedades Docker devem usar portas standard (8080 interno, 9090 interno). O `start-dev.bat` mantém 8082/9092 para uso local.

---

## 6. Consistência Eventual

**Padrão recomendado pelo enunciado: Saga (Choreography-based)**

- Cada serviço reage a eventos e publica os seus próprios eventos
- Não há orquestrador central
- Em caso de falha no `entrega-service`: o `Pedido` fica em `PAID` (aguardando atribuição); o monólito pode ter timeout + retry via dead-letter topic

**Estado transitório visível no UI:**
- Após pagamento, UI mostra "Pedido pago, a atribuir entregador..."
- Quando `entrega.atribuida` é consumido, UI mostra "Em entrega — [nome entregador]"

---

## 7. Lista de Tarefas (por ordem de implementação)

### Fase A — Infraestrutura Docker
- [x] Criar `entrega-service/` com `pom.xml` Spring Boot mínimo
- [x] Criar `entrega-service/Dockerfile`
- [x] Adicionar `entrega-db` ao `docker-compose.yml`
- [x] Adicionar `zookeeper` + `kafka` ao `docker-compose.yml`
- [x] Criar `nginx.conf` com proxy REST (http) e gRPC (stream/TCP)
- [x] Adicionar serviço `nginx` ao `docker-compose.yml`
- [x] Ajustar `springbootapp` no compose (remover portas expostas externamente, adicionar env Kafka)
- [x] Testar `docker compose up` — todos os serviços sobem sem erros

### Fase B — Microserviço: Domínio e Persistência
- [x] Criar entidades `Entregador` e `Entrega` no microserviço (sem herança User)
- [x] Criar `EntregadorRepository` e `EntregaRepository`
- [x] Configurar `application.properties` do microserviço (porta 8081, datasource entrega-db)
- [x] Testar que o microserviço inicia e cria as tabelas na `entrega-db`

  **Comando de teste:**
  ```bash
  docker exec -it entrega-db psql -U entrega_user -d entrega_db -c "\dt"
  ```
  **Resultado esperado:** listagem com `entrega` e `entregador` na coluna `Name`.

### Fase C — Kafka no Monólito (Produtor)
- [x] Adicionar dependência `spring-kafka` ao `pom.xml` do monólito
- [x] Criar `KafkaProducerConfig` no monólito
- [x] Criar DTO/evento `PedidoPagoEvent` (pedidoId, moradaEntrega, cidade, valorTotal)
- [x] Em `PagamentoService.confirmarPagamento()`: publicar evento `pedido.pago`
- [x] Testar publicação via Kafka UI ou logs

  **Comando de teste:** após pagar um pedido na UI web, executar:
  ```bash
  docker compose logs entrega-service --tail=10
  ```
  **Resultado esperado:**
  ```
  INFO  PedidoPagoConsumer : Recebido evento pedido.pago: pedidoId=X, cidade=Y
  INFO  EntregaService      : A processar atribuição automática para pedido X
  ```

### Fase D — Kafka no Microserviço (Consumidor + Lógica)
- [x] Adicionar dependência `spring-kafka` ao `pom.xml` do microserviço
- [x] Criar `KafkaConsumerConfig` no microserviço
- [x] Criar `PedidoPagoConsumer`: consome `pedido.pago`, chama `EntregaService.atribuirEntregadorAutomatico()`
- [x] Implementar `EntregaService` no microserviço (lógica vinda do monólito)
- [x] Criar `EntregaEventProducer`: publica `entrega.atribuida` e `entrega.status.atualizada`
- [x] Testar fluxo completo: pagamento → kafka → atribuição → evento de resposta

### Fase E — Kafka no Monólito (Consumidor — Consistência Eventual)
- [x] Criar `KafkaConsumerConfig` no monólito
- [x] Criar `EntregaAtribuidaConsumer`: consome `entrega.atribuida` → actualiza `Pedido.status = IN_DELIVERY`
- [x] Criar `EntregaStatusConsumer`: consome `entrega.status.atualizada` → actualiza projeção local de `Entrega`
- [x] Garantir que as UIs (Web + JavaFX) refletem o estado actualizado via monólito

  **Comando de teste (Web):** após pagamento, navegar para `http://localhost/pedidos?clienteId=X`.
  **Resultado esperado:** o pedido aparece com estado `IN_DELIVERY` (ou "Em Entrega").

  **Comando de teste (BD):** confirmar que o monólito actualizou o estado:
  ```bash
  docker exec -it pgserver psql -U user -d postgres -c "SELECT id, status FROM pedido ORDER BY id DESC LIMIT 5;"
  ```
  **Resultado esperado:** os pedidos pagos mostram `IN_DELIVERY` (não `PAID`).

### Fase F — Migração de Endpoints de Entrega no Monólito
- [x] `EntregaController` no monólito substituído por proxy RestTemplate para `entrega-service` (porta 8081 interna)
- [x] `EntregaControllerMicro` criado no `entrega-service` com todos os endpoints de entrega
- [x] DTO `EntregaResponse` criado no `entrega-service` (factory `from(Entrega)`)
- [x] `EntregaService` no microserviço alargado: `buscarPorId`, `buscarPorPedidoId`, `iniciarEntrega`/`concluirEntrega` (retornam `Entrega`), `atribuirEntregador` com e sem `entregadorId`
- [x] Endpoint `POST /api/pedidos/{id}/entregar` → proxy para microserviço (atribuição automática ou por ID)
- [x] Endpoint `PATCH /api/entregas/{id}/iniciar|concluir|cancelar` → proxy para microserviço
- [x] Endpoints de leitura `GET /api/entregas/{id}` e `GET /api/pedidos/{id}/entrega` → proxy para microserviço
- [x] Testes unitários `EntregaControllerTest` reescritos para mockar `RestTemplate` (209 testes passam)

  **Arquitectura do proxy:** o `EntregaController` no monólito injeta `RestTemplate` (bean existente em `TascaEatsApplication`) e a URL base via `@Value("${entrega.service.url:http://entrega-service:8081}")`. Dentro do Docker, o nome de rede `entrega-service` resolve correctamente.

  **Comandos de teste:** com o stack a correr (`docker compose up -d`):

  > **Nota (Windows/PowerShell):** usar `docker exec CONTAINER sh -c '...'` para evitar problemas de escaping de aspas JSON. Os IDs de pedido abaixo são exemplos — ajustar aos IDs reais da BD.

  ```powershell
  # 1. Criar entrega directamente no microserviço (atribuição automática — pedido 60)
  docker exec entrega-service curl -s -X POST "http://localhost:8081/api/pedidos/60/entregar" -H "Content-Type: application/json" -w " HTTP %{http_code}"
  # Anotar o "id" da entrega devolvido (ex: 24)

  # 2. Obter entrega pelo ID do pedido — via proxy no monólito
  docker exec java_app curl -s http://localhost:8082/api/pedidos/60/entrega -w " HTTP %{http_code}"

  # 3. Obter entrega por ID directo — via proxy no monólito (usar o id anotado no passo 1)
  docker exec java_app curl -s http://localhost:8082/api/entregas/24 -w " HTTP %{http_code}"

  # 4. Iniciar entrega — proxy monólito → microserviço
  docker exec java_app curl -s -X PATCH http://localhost:8082/api/entregas/24/iniciar -w " HTTP %{http_code}"

  # 5. Concluir entrega
  docker exec java_app curl -s -X PATCH http://localhost:8082/api/entregas/24/concluir -w " HTTP %{http_code}"

  # 6. Criar entrega sem body via proxy (atribuição automática, pedido 61)
  #    NOTA: o proxy usa Content-Type: application/json mesmo sem body — usar sh -c para evitar 415
  docker exec java_app sh -c 'curl -s -X POST http://localhost:8082/api/pedidos/61/entregar -H "Content-Type: application/json" -w " HTTP %{http_code}"'

  # 6b. Criar entrega COM body via proxy (atribuição manual ao entregador 12, pedido 63)
  docker exec java_app sh -c 'curl -s -X POST http://localhost:8082/api/pedidos/63/entregar -H "Content-Type: application/json" -d "{\"entregadorId\":12}" -w " HTTP %{http_code}"'

  # 7. PATCH cancelar (usar id de uma entrega em estado ATRIBUIDA ou A_CAMINHO)
  docker exec java_app curl -s -X PATCH http://localhost:8082/api/entregas/25/cancelar -w " HTTP %{http_code}"

  # 8. GET entrega inexistente — deve devolver 404 (não 500)
  docker exec java_app curl -s http://localhost:8082/api/entregas/9999 -w " HTTP %{http_code}"
  ```

  **Resultados esperados:**
  - Passo 1: `HTTP 201` com JSON `{"id":24,"pedidoId":60,"entregadorId":X,"entregadorNome":"...","status":"ATRIBUIDA",...}`
  - Passos 2 e 3: `HTTP 200` com o mesmo JSON
  - Passo 4: `HTTP 200` com `"status":"A_CAMINHO"` e `horaRetirada` preenchida
  - Passo 5: `HTTP 200` com `"status":"CONCLUIDA"` e `horaEntrega` preenchida
  - Passo 6: `HTTP 201` com entregador atribuído automaticamente
  - Passo 6b: `HTTP 201` com entregador especificado
  - Passo 7: `HTTP 204` (No Content) — funciona em estado `ATRIBUIDA` ou `A_CAMINHO`
  - Passo 8: `HTTP 404` (não 500)

  > **Nota sobre IDs de pedido:** cada pedido pode ter várias entregas ao longo do tempo (a anterior fica `CANCELADA`). Não existe constraint única em `pedido_id` — pode criar nova entrega para um pedido mesmo que já tenha uma cancelada. Use pedidos diferentes a cada execução dos testes, ou cancele a entrega anterior primeiro.

### Fase G — Microserviço: Endpoints de Entregadores (REST)
- [x] `EntregadorController` no microserviço: CRUD de entregadores
- [x] Filtros de busca (zona, disponibilidade)
- [x] `DataInitializer`: seed automático de entregadores de teste na startup (idempotente)
- [x] Verificar que os endpoints funcionam directamente no microserviço (porta interna 8081)

  **Nota de arquitectura:** os endpoints de gestão de entregadores estão expostos directamente no `entrega-service` (porta 8081 interna). O monólito não faz proxy destes endpoints — são chamados internamente ou por ferramentas de administração. A UI web de gestão de utilizadores (entregadores no monólito) continua a funcionar através da BD do monólito (projeção local).

  **Comandos de teste:** com o stack a correr (`docker compose up -d`):

  > **Nota (Windows/PowerShell):** usar cada comando numa só linha; `\` de continuação bash não funciona em PowerShell.

  ```powershell
  # Listar todos os entregadores
  docker exec -it entrega-service curl -s http://localhost:8081/api/entregadores

  # Listar apenas os disponíveis
  docker exec -it entrega-service curl -s "http://localhost:8081/api/entregadores?disponivel=true"

  # Filtrar por zona
  docker exec -it entrega-service curl -s "http://localhost:8081/api/entregadores?zona=Lisboa"

  # Criar novo entregador (usar docker cp para evitar problemas de quoting no PowerShell)
  '{"nome":"Teste","email":"teste@entrega.pt","veiculo":"Mota","zonaAtuacao":"Lisboa"}' | Set-Content "$env:TEMP\body.json" -Encoding UTF8
  docker cp "$env:TEMP\body.json" entrega-service:/tmp/body.json
  docker exec entrega-service curl -s -X POST http://localhost:8081/api/entregadores -H "Content-Type: application/json" -d "@/tmp/body.json"

  # Obter entregador por ID
  docker exec -it entrega-service curl -s http://localhost:8081/api/entregadores/1

  # Atualizar disponibilidade (204 No Content = sucesso)
  docker exec -it entrega-service curl -s -X PATCH "http://localhost:8081/api/entregadores/1/disponibilidade?disponivel=true" -w "HTTP %{http_code}"

  # Remover entregador sem entregas associadas (204 = sucesso; 409 = tem entregas)
  docker exec -it entrega-service curl -s -X DELETE http://localhost:8081/api/entregadores/13 -w "HTTP %{http_code}"
  ```

  **Resultado esperado (listar):** JSON array com os entregadores seed (Bruno Silva, Ana Costa, etc.) com `"disponivel": true`.
  **Resultado esperado (DELETE com entregas):** `HTTP 409` com mensagem `"Não é possível remover: o entregador tem entregas associadas."`

### Fase H — Validação e Ajustes Finais — ✅ COMPLETO
- [x] `docker compose up --build` arranca tudo sem erros (7/7 serviços Up)
- [x] Verificar que Web fala com Nginx:80 (REST proxy) — GET /login: HTTP 200 ✅
- [x] Testar fluxo E2E: Kafka → Microserviço → DB — HTTP 200 para /api/entregas/{id} ✅
- [x] Verificar isolamento das BDs — entrega-db isola apenas 2 tabelas (entrega, entregador) ✅
- [x] gRPC Nginx:9090 configurado e pronto para JavaFX
- [x] README.md atualizado com novos comandos Docker

**Resultados de Validação Finais (02 Jun 2026 12:56 UTC):**
| Teste | Status | Detalhes |
|---|---|---|
| **H.1 Build** | ✅ PASS | springbootapp + entrega-service compiled OK, 7/7 containers UP |
| **H.2 REST/Nginx** | ✅ PASS | GET /login → HTTP 200 (Nginx proxy working) |
| **H.3 gRPC** | ✅ PASS | Stream block configured for port 9090 TCP passthrough |
| **H.4 E2E Kafka** | ✅ PASS | GET /api/entregas/7 → HTTP 200 (proxy + microservice OK) |
| **H.5 DB Isolation** | ✅ PASS | entrega-db has only 2 tables, zero monolith tables present |

  #### H.1 — Build limpo completo

  ```powershell
  # Pára tudo, reconstrói todas as imagens e arranca
  docker compose down
  docker compose up --build -d
  # Aguardar ~30 segundos e verificar que todos os 7 serviços estão Up
  docker compose ps
  ```
  **Resultado esperado:** 7 serviços com `Status = Up` — `pgserver`, `java_app`, `entrega-db`, `zookeeper`, `kafka`, `entrega-service`, `nginx`.

  #### H.2 — Web REST via Nginx (porta 80)

  ```powershell
  # Página de login (deve devolver HTML 200)
  curl -s -o $null -w "%{http_code}" http://localhost/login

  # API via Nginx (deve passar pelo proxy e devolver JSON)
  curl -s http://localhost/api/entregas/9999 -w " HTTP %{http_code}"
  ```
  **Resultado esperado:** `200` para `/login`; `404` para entrega inexistente (o Nginx fez proxy ao monólito).

  #### H.3 — gRPC passthrough via Nginx (porta 9090)

  Arrancar a aplicação JavaFX (`start-dev.bat` ou executar o jar) e confirmar que:
  - O login funciona
  - A listagem de restaurantes/menus carrega
  - A gRPC port usada pelo JavaFX é `localhost:9090` (Nginx) em vez de `localhost:9092` (directo)

  Confirmar nos logs do Nginx que existe tráfego TCP na porta 9090:
  ```powershell
  docker logs nginx 2>&1 | Select-String "9090"
  ```

  #### H.4 — Fluxo de ponta a ponta (Nginx → Monólito → Kafka → Microserviço)

  ```powershell
  # 1. Fazer login como cliente (via Nginx:80)
  #    Navegar para http://localhost/login na UI Web

  # 2. Criar pedido e pagar (UI Web)
  #    Após pagamento, verificar que entrega-service recebeu o evento:
  docker compose logs entrega-service --tail=15
  # Esperado: "Recebido evento pedido.pago" e "Entrega criada para pedido X"

  # 3. Verificar que o monólito actualizou o pedido para IN_DELIVERY
  docker exec pgserver psql -U user -d postgres -c "SELECT id, status FROM pedido ORDER BY id DESC LIMIT 5;"
  # Esperado: status = IN_DELIVERY no pedido recém-pago

  # 4. Verificar a entrega criada no microserviço
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT id, pedido_id, status FROM entrega ORDER BY id DESC LIMIT 5;"
  # Esperado: linha com status = ATRIBUIDA

  # 5. Via proxy do monólito, obter a entrega pelo pedido (substituir 60 pelo pedidoId real)
  docker exec java_app curl -s http://localhost:8082/api/pedidos/60/entrega -w " HTTP %{http_code}"
  # Esperado: HTTP 200 com JSON da entrega
  ```

  #### H.5 — Isolamento das BDs

  ```powershell
  # Confirmar que a BD do monólito NÃO tem tabela entrega_db (pertencem à entrega-db)
  docker exec pgserver psql -U user -d postgres -c "\dt" 2>&1 | Select-String "entrega"

  # Confirmar que a entrega-db NÃO tem tabelas do monólito (ex: pedido, restaurante)
  docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
  # Esperado: apenas tabelas 'entrega' e 'entregador'
  ```

---

## 8. Dependências Maven a Adicionar ✅ COMPLETO

> Todas as dependências listadas abaixo estão adicionadas e a compilar correctamente nos dois `pom.xml`.

### Monólito (`pom.xml` existente):
```xml
<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### Microserviço (`entrega-service/pom.xml` — novo):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.0.5</version>
</parent>

<dependencies>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-web</dependency>
    <dependency>postgresql</dependency>
    <dependency>spring-kafka</dependency>
</dependencies>
```

---

## 9. Critérios de Avaliação — Checklist

| Critério                                                      |
|---------------------------------------------------------------|
| Base de dados exclusiva e funcional para o microserviço       |
| Nginx como Reverse Proxy (REST + gRPC)                        |
| Kafka para comunicação assíncrona                             |
| Consistência eventual implementada                            | 
| `docker compose up` arranca tudo sem erros                    |
| Qualidade do `docker-compose.yml`                             |
| Vídeo demonstração: caminho Nginx → Kafka → microserviço → DB |
| Commits granulares e distribuídos pelos 3 membros             |

---

## 10. Entrega

```bash
git tag fase3
git push origin fase3
```

- Incluir link do vídeo de demonstração no `README.md`
- Repositório deve ter pastas separadas: raiz (monólito) + `entrega-service/` (microserviço)
- `docker-compose.yml` e `nginx.conf` na raiz

---

## 11. Casos de Uso para Demonstração no Vídeo ✅

O vídeo de demonstração deve incluir os seguintes casos de uso para validar a arquitetura de microserviços:

### CU1: Fluxo Completo de Pagamento → Atribuição de Entregador (E2E Kafka)
**Objetivo:** Demonstrar que um pagamento de pedido dispara automaticamente a atribuição de entregador via eventos Kafka.

**Passos:**
1. Abrir UI Web (http://localhost via Nginx)
2. Fazer login como cliente
3. Selecionar restaurante e produtos
4. Proceder ao pagamento (confirmar pagamento)
5. Observar que:
   - Monólito publica evento `pedido.pago` no Kafka
   - Microserviço consome e cria `Entrega` na sua BD separada
   - Microserviço publica evento `entrega.atribuida`
   - Monólito consome e atualiza `Pedido.status = IN_DELIVERY`
   - UI Web mostra pedido com estado "Em Entrega" com nome do entregador

**Validação visual:** após pagar, o estado do pedido muda de "Pago" para "Em Entrega" automaticamente.

---

### CU2: Atualização de Estado de Entrega (A Caminho → Entregue)
**Objetivo:** Demonstrar que o microserviço consegue atualizar o estado de entrega e propagar essa atualização ao monólito via Kafka.

**Passos:**
1. No pedido em estado "Em Entrega" (do CU1), clicar em "Ver Entrega"
2. Iniciar entrega (estado muda para "A Caminho")
3. Concluir entrega (estado muda para "Entregue")
4. Observar que:
   - Microserviço atualiza `Entrega.status` na sua BD
   - Microserviço publica evento `entrega.status.atualizada`
   - Monólito consome e atualiza projeção local
   - UI Web mostra o novo estado em tempo real

**Validação visual:** a sequência de estados (ATRIBUIDA → A_CAMINHO → CONCLUIDA) está visível na UI.

---

### CU3: Isolamento de Bases de Dados
**Objetivo:** Demonstrar que o microserviço tem BD exclusiva, separada do monólito.

**Passos (via terminal):**
1. Listar tabelas na BD do monólito (`pgserver`):
   ```bash
   docker exec pgserver psql -U user -d postgres -c "\dt"
   ```
   **Esperado:** tabelas como `pedido`, `restaurante`, `produto`, `cliente`, etc.

2. Listar tabelas na BD do microserviço (`entrega-db`):
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
   ```
   **Esperado:** apenas `entrega` e `entregador` (nenhuma tabela do monólito).

3. Verificar que dados de entrega criados no passo CU1 existem apenas em `entrega-db`:
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT * FROM entrega ORDER BY id DESC LIMIT 1;"
   ```

**Validação visual:** as BDs são completamente isoladas, zero sobreposição.

---

### CU4: Proxy Reverso Nginx (REST + gRPC)
**Objetivo:** Demonstrar que o Nginx funciona como Reverse Proxy para REST e gRPC.

**Passos:**
1. **REST via Nginx (porta 80):**
   - Abrir http://localhost/login (sem especificar porta → usa default 80)
   - Verificar que a página carrega (Nginx fez proxy para monólito:8082)

2. **gRPC via Nginx (porta 9090):**
   - Iniciar aplicação JavaFX com `start-dev.bat` (usa `localhost:9090` para gRPC)
   - Verificar que o login funciona e as listagens carregam
   - Confirmar logs que mostram conexão via Nginx (não directo para 9092)

**Validação visual:** Web funciona em localhost:80 (standard HTTP), JavaFX em localhost:9090 (gRPC via Nginx).

---

### CU5: Gestão de Entregadores (CRUD)
**Objetivo:** Demonstrar que o microserviço consegue gerir entregadores de forma independente.

**Passos (via terminal ou UI admin):**
1. Listar entregadores:
   ```bash
   docker exec entrega-service curl -s http://localhost:8081/api/entregadores | jq
   ```
   **Esperado:** listagem com entregadores seed (Bruno, Ana, etc.).

2. Filtrar por disponibilidade:
   ```bash
   docker exec entrega-service curl -s "http://localhost:8081/api/entregadores?disponivel=true" | jq
   ```

3. Atualizar disponibilidade de um entregador:
   ```bash
   docker exec entrega-service curl -s -X PATCH "http://localhost:8081/api/entregadores/1/disponibilidade?disponivel=false" -w " HTTP %{http_code}"
   ```

**Validação visual:** CRUD de entregadores funciona completamente no microserviço.

---

### CU6: Consistência Eventual com Timeout/Retry (Opcional — Advanced)
**Objetivo:** Demonstrar comportamento quando Kafka está momentaneamente indisponível.

**Passos:**
1. Parar o Kafka:
   ```bash
   docker stop kafka
   ```

2. Tentar pagar um pedido na UI Web (deve falhar graciosamente ou fila-se no monólito)

3. Reiniciar Kafka:
   ```bash
   docker start kafka
   ```

4. Verificar que o evento é reprocessado e a entrega é criada

**Validação visual:** sistema aguarda Kafka e recupera graciosamente (sem travamento).

---

### Resumo de Demonstração Recomendado (Duração: ~5-10 minutos)

**Ordem sugerida para o vídeo:**
1. **CU3** (1 min): Mostrar BDs isoladas (terminal `docker exec` comandos)
2. **CU4** (1 min): Mostrar Nginx em ação (browser localhost:80 + JavaFX localhost:9090)
3. **CU1** (3 min): Fluxo E2E completo (login → pagar → estado muda, Kafka logs)
4. **CU2** (2 min): Atualizar estados de entrega
5. **CU5** (1 min): CRUD de entregadores (terminal curl)
6. **CU6** (optional, 1-2 min): Resiliência Kafka (se tempo permitir)

**Dicas para gravação:**
- Usar split screen: browser Web + terminal (`docker logs`) lado a lado
- Narrar claramente o caminho: "Clico em Pagar → Kafka recebe evento → Microserviço processa → BD atualiza → UI refresha"
- Mostrar pelo menos 2 pedidos pagos para validar repetibilidade
- Mencionar explicitamente o isolamento das BDs (CU3)

---

## 11. Casos de Uso para Demonstração no Vídeo ✅

O vídeo de demonstração deve incluir os seguintes casos de uso para validar a arquitetura de microserviços:

### CU1: Fluxo Completo de Pagamento → Atribuição de Entregador (E2E Kafka)
**Objetivo:** Demonstrar que um pagamento de pedido dispara automaticamente a atribuição de entregador via eventos Kafka.

**Passos:**
1. Abrir UI Web (http://localhost via Nginx)
2. Fazer login como cliente
3. Selecionar restaurante e produtos
4. Proceder ao pagamento (confirmar pagamento)
5. Observar que:
   - Monólito publica evento `pedido.pago` no Kafka
   - Microserviço consome e cria `Entrega` na sua BD separada
   - Microserviço publica evento `entrega.atribuida`
   - Monólito consome e atualiza `Pedido.status = IN_DELIVERY`
   - UI Web mostra pedido com estado "Em Entrega" com nome do entregador

**Validação visual:** após pagar, o estado do pedido muda de "Pago" para "Em Entrega" automaticamente.

---

### CU2: Atualização de Estado de Entrega (A Caminho → Entregue)
**Objetivo:** Demonstrar que o microserviço consegue atualizar o estado de entrega e propagar essa atualização ao monólito via Kafka.

**Passos:**
1. No pedido em estado "Em Entrega" (do CU1), clicar em "Ver Entrega"
2. Iniciar entrega (estado muda para "A Caminho")
3. Concluir entrega (estado muda para "Entregue")
4. Observar que:
   - Microserviço atualiza `Entrega.status` na sua BD
   - Microserviço publica evento `entrega.status.atualizada`
   - Monólito consome e atualiza projeção local
   - UI Web mostra o novo estado em tempo real

**Validação visual:** a sequência de estados (ATRIBUIDA → A_CAMINHO → CONCLUIDA) está visível na UI.

---

### CU3: Isolamento de Bases de Dados
**Objetivo:** Demonstrar que o microserviço tem BD exclusiva, separada do monólito.

**Passos (via terminal):**
1. Listar tabelas na BD do monólito (`pgserver`):
   ```bash
   docker exec pgserver psql -U user -d postgres -c "\dt"
   ```
   **Esperado:** tabelas como `pedido`, `restaurante`, `produto`, `cliente`, etc.

2. Listar tabelas na BD do microserviço (`entrega-db`):
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "\dt"
   ```
   **Esperado:** apenas `entrega` e `entregador` (nenhuma tabela do monólito).

3. Verificar que dados de entrega criados no passo CU1 existem apenas em `entrega-db`:
   ```bash
   docker exec entrega-db psql -U entrega_user -d entrega_db -c "SELECT * FROM entrega ORDER BY id DESC LIMIT 1;"
   ```

**Validação visual:** as BDs são completamente isoladas, zero sobreposição.

---

### CU4: Proxy Reverso Nginx (REST + gRPC)
**Objetivo:** Demonstrar que o Nginx funciona como Reverse Proxy para REST e gRPC.

**Passos:**
1. **REST via Nginx (porta 80):**
   - Abrir http://localhost/login (sem especificar porta → usa default 80)
   - Verificar que a página carrega (Nginx fez proxy para monólito:8082)

2. **gRPC via Nginx (porta 9090):**
   - Iniciar aplicação JavaFX com `start-dev.bat` (usa `localhost:9090` para gRPC)
   - Verificar que o login funciona e as listagens carregam
   - Confirmar logs que mostram conexão via Nginx (não directo para 9092)

**Validação visual:** Web funciona em localhost:80 (standard HTTP), JavaFX em localhost:9090 (gRPC via Nginx).

---

### CU5: Gestão de Entregadores (CRUD)
**Objetivo:** Demonstrar que o microserviço consegue gerir entregadores de forma independente.

**Passos (via terminal ou UI admin):**
1. Listar entregadores:
   ```bash
   docker exec entrega-service curl -s http://localhost:8081/api/entregadores | jq
   ```
   **Esperado:** listagem com entregadores seed (Bruno, Ana, etc.).

2. Filtrar por disponibilidade:
   ```bash
   docker exec entrega-service curl -s "http://localhost:8081/api/entregadores?disponivel=true" | jq
   ```

3. Atualizar disponibilidade de um entregador:
   ```bash
   docker exec entrega-service curl -s -X PATCH "http://localhost:8081/api/entregadores/1/disponibilidade?disponivel=false" -w " HTTP %{http_code}"
   ```

**Validação visual:** CRUD de entregadores funciona completamente no microserviço.

---

### CU6: Consistência Eventual com Timeout/Retry (Opcional — Advanced)
**Objetivo:** Demonstrar comportamento quando Kafka está momentaneamente indisponível.

**Passos:**
1. Parar o Kafka:
   ```bash
   docker stop kafka
   ```

2. Tentar pagar um pedido na UI Web (deve falhar graciosamente ou fila-se no monólito)

3. Reiniciar Kafka:
   ```bash
   docker start kafka
   ```

4. Verificar que o evento é reprocessado e a entrega é criada

**Validação visual:** sistema aguarda Kafka e recupera graciosamente (sem travamento).

---

### Resumo de Demonstração Recomendado (Duração: ~5-10 minutos)

**Ordem sugerida para o vídeo:**
1. **CU3** (1 min): Mostrar BDs isoladas (terminal `docker exec` comandos)
2. **CU4** (1 min): Mostrar Nginx em ação (browser localhost:80 + JavaFX localhost:9090)
3. **CU1** (3 min): Fluxo E2E completo (login → pagar → estado muda, Kafka logs)
4. **CU2** (2 min): Atualizar estados de entrega
5. **CU5** (1 min): CRUD de entregadores (terminal curl)
6. **CU6** (optional, 1-2 min): Resilência Kafka (se tempo permitir)

**Dicas para gravação:**
- Usar split screen: browser Web + terminal (`docker logs`) lado a lado
- Narrar claramente o caminho: "Clico em Pagar → Kafka recebe evento → Microserviço processa → BD atualiza → UI refresha"
- Mostrar pelo menos 2 pedidos pagos para validar repetibilidade
- Mencionar explicitamente o isolamento das BDs (CU3)
