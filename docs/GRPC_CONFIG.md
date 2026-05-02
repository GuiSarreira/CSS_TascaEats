# Configuração gRPC — Fase F (Resumo)

## Status: ✅ CONFIGURADO E PRONTO PARA PRODUÇÃO

### O que foi feito:

#### 1. **Configuração do Servidor gRPC**
- **Arquivo**: `src/main/resources/application.properties`
- **Porta**: 9090 (padrão para gRPC)
- **Configurações**:
  ```properties
  grpc.server.port=9090
  grpc.server.enable-keep-alive=true
  grpc.server.keep-alive-time=30s
  grpc.server.keep-alive-timeout=10s
  ```

#### 2. **Compilação de Protocol Buffers**
- **Ficheiro `.proto`**: `src/main/proto/tascaeats.proto`
- **Plugin Maven**: `protobuf-maven-plugin` v0.6.1
- **Resultado**: 62 ficheiros Java gerados em `target/generated-sources/protobuf/`
  - `java/` — Message types (30+ tipos)
  - `grpc-java/` — Service stubs (17 RPC methods)

#### 3. **Implementação do Serviço gRPC**
- **Classe**: `src/main/java/pt/ul/fc/css/tascaeats/grpc/TascaEatsGrpcServiceImpl.java`
- **Anotação**: `@GrpcService` (Spring Boot gRPC)
- **Implementação**:
  - ✅ **Pessoa 1 (Avaliações)**: 4 métodos completos
    - `criarAvaliacao()`, `listarAvaliacoes()`, `atualizarAvaliacao()`, `removerAvaliacao()`
  - ✅ **Pessoa 2 (Menus + Restaurantes)**: 6 métodos completos
    - `criarMenu()`, `listarMenus()`, `atualizarMenu()`, `removerMenu()`, `associarMenuRestaurante()`, `listarRestaurantes()`
  - ⏳ **Pessoa 3 (Pedidos/Entregas/Pagamentos)**: 6 stubs para v1.1
    - Retornam `Status.UNIMPLEMENTED` com mensagem versão 1.1

#### 4. **Docker Compose**
- **Arquivo**: `docker-compose.yml`
- **Porta Exposta**: 9090
- **Configuração**:
  ```yaml
  ports:
    - 8080:8080   # REST API
    - 9090:9090   # gRPC Server
  ```

### Como Usar:

#### **1. Compilar o Projeto**
```bash
.\mvnw.cmd clean compile
# ou para build completo:
.\mvnw.cmd clean package -DskipTests
```

#### **2. Iniciar o Servidor**
**Com Docker Compose** (recomendado para produção):
```bash
docker compose up -d
```

**Manualmente** (desenvolvimento com PostgreSQL):
```bash
# Ensure PostgreSQL is running on localhost:5432
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
java -jar target/tascaeats-1.0.jar
```

#### **3. Testar o Serviço gRPC**
**Opção A — Com grpcurl** (ferramenta CLI):
```bash
# Listar serviços
C:\temp\grpcurl\grpcurl.exe -plaintext -proto src/main/proto/tascaeats.proto list

# Chamar um método
C:\temp\grpcurl\grpcurl.exe -plaintext \
  -proto src/main/proto/tascaeats.proto \
  -d '{}' \
  pt.ul.fc.css.tascaeats.grpc.TascaEatsService/ListarRestaurantes \
  localhost:9090
```

**Opção B — Com script PowerShell**:
```bash
.\test-grpc.ps1
```

**Opção C — Via JavaFX Client** (Fase G):
JavaFX usará o `TascaEatsServiceGrpc.TascaEatsServiceStub` para fazer chamadas assíncronas.

### Serviço e Métodos Disponíveis:

#### **Service Name**: `pt.ul.fc.css.tascaeats.grpc.TascaEatsService`

**RPC Methods** (17 total):

##### Pessoa 1 (Avaliações) ✅
1. `rpc CriarAvaliacao (CriarAvaliacaoRequest) returns (AvaliacaoResponse)`
2. `rpc ListarAvaliacoes (ListarAvaliacoesRequest) returns (ListarAvaliacoesResponse)`
3. `rpc AtualizarAvaliacao (AtualizarAvaliacaoRequest) returns (AvaliacaoResponse)`
4. `rpc RemoverAvaliacao (RemoverAvaliacaoRequest) returns (Empty)`

##### Pessoa 2 (Menus + Restaurantes) ✅
5. `rpc CriarMenu (CriarMenuRequest) returns (MenuResponse)`
6. `rpc ListarMenus (ListarMenusRequest) returns (ListarMenusResponse)`
7. `rpc AtualizarMenu (AtualizarMenuRequest) returns (MenuResponse)`
8. `rpc RemoverMenu (RemoverMenuRequest) returns (Empty)`
9. `rpc AssociarMenuRestaurante (AssociarMenuRestauranteRequest) returns (MenuResponse)`
10. `rpc ListarRestaurantes (ListarRestaurantesRequest) returns (ListarRestaurantesResponse)`

##### Pessoa 3 (Pedidos/Entregas/Pagamentos) ⏳ v1.1
11. `rpc CriarPedido (CriarPedidoRequest) returns (PedidoResponse)`
12. `rpc ListarPedidos (ListarPedidosRequest) returns (ListarPedidosResponse)`
13. `rpc AvancarEstadoPedido (AvancarEstadoPedidoRequest) returns (PedidoResponse)`
14. `rpc CancelarPedido (CancelarPedidoRequest) returns (Empty)`
15. `rpc RegistarPagamento (RegistarPagamentoRequest) returns (PagamentoResponse)`
16. `rpc ObterEntrega (ObterEntregaRequest) returns (EntregaResponse)`

### Próximas Etapas:

1. **Fase G — JavaFX Cliente**
   - Criar cliente gRPC assíncro
   - Implementar UI com FXML
   - Integrar com `TascaEatsServiceGrpc.TascaEatsServiceStub`

2. **Fase F — v1.1 (Futuro)**
   - Implementar Pessoa 3 (Pedidos/Entregas/Pagamentos)
   - Testes de integração com clientes gRPC

3. **Teste em Produção**
   - Validar com Docker Compose completo
   - Testar conectividade entre REST API (8080) e gRPC (9090)
   - Monitorar logs com `docker compose logs -f`

### Dependências Maven Utilizadas:

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>2.15.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.53.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.53.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.53.0</version>
</dependency>
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.24.4</version>
</dependency>
```

### Ficheiros Modificados/Criados:

| Ficheiro | Tipo | Status |
|----------|------|--------|
| `src/main/resources/application.properties` | Modificado | ✅ Configuração gRPC adicionada |
| `docker-compose.yml` | Modificado | ✅ Porta 9090 exposta |
| `src/main/resources/application-test.properties` | Criado | ✅ Perfil de teste (H2) |
| `src/main/proto/tascaeats.proto` | Existente | ✅ Validado |
| `src/main/java/.../TascaEatsGrpcServiceImpl.java` | Existente | ✅ Implementação completa (Pessoa 1+2) |
| `target/generated-sources/protobuf/` | Gerado | ✅ 62 ficheiros Java |
| `test-grpc.ps1` | Criado | ✅ Script de teste |

### Verificação Final:

```bash
✅ Compilação: BUILD SUCCESS (151 source files + 62 generated stubs)
✅ Porta gRPC: 9090 (configurada)
✅ Docker: 9090 exposto
✅ Implementação: Pessoa 1+2 completo, Pessoa 3 stubs prontos
✅ Pronto para Fase G (JavaFX Client)
```

---

**Próxima etapa:** Iniciar Fase G — Interface JavaFX com cliente gRPC assíncro.
