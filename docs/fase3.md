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
- [ ] Criar `entrega-service/` com `pom.xml` Spring Boot mínimo
- [ ] Criar `entrega-service/Dockerfile`
- [ ] Adicionar `entrega-db` ao `docker-compose.yml`
- [ ] Adicionar `zookeeper` + `kafka` ao `docker-compose.yml`
- [ ] Criar `nginx.conf` com proxy REST (http) e gRPC (stream/TCP)
- [ ] Adicionar serviço `nginx` ao `docker-compose.yml`
- [ ] Ajustar `springbootapp` no compose (remover portas expostas externamente, adicionar env Kafka)
- [ ] Testar `docker compose up` — todos os serviços sobem sem erros

### Fase B — Microserviço: Domínio e Persistência
- [ ] Criar entidades `Entregador` e `Entrega` no microserviço (sem herança User)
- [ ] Criar `EntregadorRepository` e `EntregaRepository`
- [ ] Configurar `application.properties` do microserviço (porta 8081, datasource entrega-db)
- [ ] Testar que o microserviço inicia e cria as tabelas na `entrega-db`

### Fase C — Kafka no Monólito (Produtor)
- [ ] Adicionar dependência `spring-kafka` ao `pom.xml` do monólito
- [ ] Criar `KafkaProducerConfig` no monólito
- [ ] Criar DTO/evento `PedidoPagoEvent` (pedidoId, moradaEntrega, cidade, valorTotal)
- [ ] Em `PagamentoService.confirmarPagamento()`: publicar evento `pedido.pago`
- [ ] Testar publicação via Kafka UI ou logs

### Fase D — Kafka no Microserviço (Consumidor + Lógica)
- [ ] Adicionar dependência `spring-kafka` ao `pom.xml` do microserviço
- [ ] Criar `KafkaConsumerConfig` no microserviço
- [ ] Criar `PedidoPagoConsumer`: consome `pedido.pago`, chama `EntregaService.atribuirEntregadorAutomatico()`
- [ ] Implementar `EntregaService` no microserviço (lógica vinda do monólito)
- [ ] Criar `EntregaEventProducer`: publica `entrega.atribuida` e `entrega.status.atualizada`
- [ ] Testar fluxo completo: pagamento → kafka → atribuição → evento de resposta

### Fase E — Kafka no Monólito (Consumidor — Consistência Eventual)
- [ ] Criar `KafkaConsumerConfig` no monólito
- [ ] Criar `EntregaAtribuidaConsumer`: consome `entrega.atribuida` → actualiza `Pedido.status = IN_DELIVERY`
- [ ] Criar `EntregaStatusConsumer`: consome `entrega.status.atualizada` → actualiza projeção local de `Entrega`
- [ ] Garantir que as UIs (Web + JavaFX) refletem o estado actualizado via monólito

### Fase F — Migração de Endpoints de Entrega no Monólito
- [ ] `EntregaController`: endpoints de actualização de estado → publicar evento Kafka em vez de chamar `EntregaService` directamente
- [ ] `EntregaController`: endpoints de leitura → continuam a ler da projeção local (ou redirecionar para microserviço via REST interno)
- [ ] Endpoint `POST /api/pedidos/{id}/entregar` → publica pedido para Kafka (atribuição no microserviço)
- [ ] Endpoint `PATCH /api/entregas/{id}/iniciar|concluir|cancelar` → publicar evento ou redirecionar

### Fase G — Microserviço: Endpoints de Entregadores (REST)
- [ ] `EntregadorController` no microserviço: CRUD de entregadores
- [ ] Filtros de busca (zona, disponibilidade)
- [ ] Verificar que as UIs que gerem entregadores funcionam (via Nginx → monólito → Kafka → microserviço ou via redirect directo se necessário)

### Fase H — Validação e Ajustes Finais
- [ ] `docker compose up --build` arranca tudo sem erros
- [ ] Verificar que JavaFX fala com Nginx:9090 (gRPC passthrough)
- [ ] Verificar que Web fala com Nginx:80 (REST proxy)
- [ ] Testar fluxo completo de ponta a ponta: registo → pedido → pagamento → entrega
- [ ] Verificar isolamento das BDs (monólito não acede a `entrega-db`)
- [ ] Rever `README.md` com novos comandos Docker

---

## 8. Dependências Maven a Adicionar

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
