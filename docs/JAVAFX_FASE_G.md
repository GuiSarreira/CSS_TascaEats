# Fase G — Interface JavaFX com gRPC Client

## Status: ✅ FRAMEWORK PRONTO | DESENVOLVIMENTO EM ANDAMENTO

### O que foi feito (Iteração 1):

#### 1. **Dependências JavaFX Adicionadas**
- **Arquivo**: `pom.xml`
- **Versão**: JavaFX 21.0.2
- **Componentes**:
  - `javafx-controls` — Botões, labels, layouts
  - `javafx-fxml` — XML-based UI markup
  - `javafx-graphics` — Rendering engine
  - `grpc-client-spring-boot-starter` — Cliente gRPC

#### 2. **Entry Point JavaFX**
- **Classe**: `src/main/java/pt/ul/fc/css/tascaeats/javafx/TascaEatsFXApp.java`
- **Responsabilidades**:
  - Iniciar Spring Boot em background thread
  - Lançar aplicação JavaFX
  - Gerenciar ciclo de vida da aplicação
- **Porta gRPC**: localhost:9090

#### 3. **Cliente gRPC**
- **Classe**: `src/main/java/pt/ul/fc/css/tascaeats/javafx/grpc/TascaEatsGrpcClient.java`
- **Funcionalidades**:
  - Gerencia conexão ManagedChannel com servidor gRPC
  - Fornece stubs síncronos (blocking) para chamadas
  - Métodos wrapper para Pessoa 1 (Avaliações) e Pessoa 2 (Menus)
  - Error handling com `StatusRuntimeException`
  - Connection pooling com keep-alive

### Build Status:

```
✅ BUILD SUCCESS
✅ 151 source files + 62 gRPC stubs
✅ JavaFX 21.0.2 integrado
✅ Cliente gRPC compilado
✅ Pronto para UI development
```

---

## Próximas Etapas (Iteração 2+):

### 1. **Criar Controladores FXML**
```
src/main/java/pt/ul/fc/css/tascaeats/javafx/controllers/
├── MainController.java          — Controlador principal (navigator)
├── RestaurantesController.java  — Listagem de restaurantes
├── MenusController.java         — CRUD de menus
├── AvaliacoesController.java    — Avaliações
└── LoginController.java         — Autenticação
```

### 2. **Criar Ficheiros FXML**
```
src/main/resources/fxml/
├── main.fxml           — Layout principal com navegação
├── login.fxml          — Ecrã de login
├── restaurantes.fxml   — Listagem de restaurantes (Pessoa 2)
├── menus.fxml          — Gestão de menus (Pessoa 2)
├── avaliacoes.fxml     — Avaliações (Pessoa 1)
├── pedidos.fxml        — Pedidos (Pessoa 3 — v1.1)
└── estilos.css         — Stylesheets
```

### 3. **Implementar Controladores**

#### **RestaurantesController** (Pessoa 2)
```java
@FXML
private TableView<RestauranteInfo> tabela;

@FXML
private void initialize() {
    client = new TascaEatsGrpcClient("localhost", 9090);
    client.connect();
    carregarRestaurantes();
}

private void carregarRestaurantes() {
    ListarRestaurantesRequest req = ListarRestaurantesRequest.newBuilder().build();
    ListarRestaurantesResponse res = client.listarRestaurantes(req);
    tabela.getItems().addAll(res.getRestaurantesList());
}
```

#### **MenusController** (Pessoa 2)
```java
// CRUD de menus com gRPC
criarMenu(request) → client.criarMenu(request)
listarMenus(request) → client.listarMenus(request)
atualizarMenu(request) → client.atualizarMenu(request)
removerMenu(request) → client.removerMenu(request)
```

#### **AvaliacoesController** (Pessoa 1)
```java
// CRUD de avaliações com gRPC
criarAvaliacao(request) → client.criarAvaliacao(request)
listarAvaliacoes(request) → client.listarAvaliacoes(request)
atualizarAvaliacao(request) → client.atualizarAvaliacao(request)
removerAvaliacao(request) → client.removerAvaliacao(request)
```

#### **PedidosController** (Pessoa 3 — v1.1)
```java
// Será implementado em v1.1 quando backend Pessoa 3 estiver pronto
// Por enquanto, retorna UNIMPLEMENTED status
```

### 4. **Layout Principal (Scene Graph)**

```
Stage (Janela)
├── MenuBar
│   ├── File
│   │   ├── Exit
│   ├── View
│   │   ├── Restaurantes
│   │   ├── Menus
│   │   ├── Avaliações
│   │   └── Pedidos (desabilitado para v1.0)
│   └── Help
│       └── About
│
└── VBox (Root)
    ├── ToolBar
    │   ├── Btn: Conectar
    │   ├── Btn: Atualizar
    │   └── Label: Status
    │
    ├── BorderPane (Content)
    │   ├── Left: NavigationPanel
    │   │   └── ListView: Opções (Restaurantes, Menus, Avaliações)
    │   │
    │   └── Center: FXMLLoader
    │       └── Carrega controller dinamicamente conforme navegação
    │
    └── StatusBar
        └── Label: Status da conexão gRPC
```

### 5. **Threading e Async gRPC**

**Atualmente**: Usando `BlockingStub` (síncrono) com UI thread bloqueante.

**Futuro**: Usar `AsyncStub` com `StreamObserver` para operações não-bloqueantes:

```java
// Async call (não bloqueia UI)
client.getAsyncStub().listarRestaurantes(req, new StreamObserver<>() {
    @Override
    public void onNext(ListarRestaurantesResponse response) {
        Platform.runLater(() -> {
            // Update UI in JavaFX thread
            tabela.getItems().addAll(response.getRestaurantesList());
        });
    }

    @Override
    public void onError(Throwable t) {
        Platform.runLater(() -> showAlert("Erro: " + t.getMessage()));
    }

    @Override
    public void onCompleted() {
        // Done
    }
});
```

---

## Como Executar (Desenvolvimento):

### **Pré-requisitos:**
- Java 21+ instalado
- Maven 3.10.1+
- PostgreSQL 18+ (ou usar perfil test com H2)
- Backend gRPC rodando na porta 9090

### **Opção 1: Debug via Maven**
```bash
# Terminal 1 — Backend Spring Boot com gRPC
.\mvnw.cmd spring-boot:run

# Terminal 2 — Frontend JavaFX
.\mvnw.cmd javafx:run
```

### **Opção 2: Via IDE (VS Code + Debugger Extension)**
```bash
1. Abrir projeto em VS Code
2. Run → Start Debugging (backend Spring Boot)
3. Run → Start Debugging (JavaFX App)
4. Ambas correm com debugger ativo
```

### **Opção 3: Via Linha de Comando**
```bash
# Build
.\mvnw.cmd clean package -DskipTests

# Executar backend
java -jar target/tascaeats-1.0.jar

# Executar frontend (em outra janela)
java -module-path "C:\path\to\javafx-sdk\lib" \
  --add-modules javafx.controls,javafx.fxml \
  -cp target/tascaeats-1.0.jar \
  pt.ul.fc.css.tascaeats.javafx.TascaEatsFXApp
```

---

## Ficheiros Criados/Modificados:

| Ficheiro | Tipo | Status |
|----------|------|--------|
| `pom.xml` | Modificado | ✅ Dependências JavaFX + grpc-client |
| `src/main/java/.../TascaEatsFXApp.java` | Criado | ✅ Entry point |
| `src/main/java/.../grpc/TascaEatsGrpcClient.java` | Criado | ✅ Cliente gRPC |
| `src/main/resources/fxml/` | Ainda criar | ⏳ FXML layouts |
| `src/main/java/.../controllers/` | Ainda criar | ⏳ Controladores |

---

## Roadmap Fase G:

| Iteração | Tarefas | Duração |
|----------|---------|---------|
| **v1.0** | JavaFX framework + gRPC client | ✅ Completo |
| **v1.1** | Listagem Restaurantes (Pessoa 2) | ~2h |
| **v1.2** | CRUD Menus (Pessoa 2) | ~2h |
| **v1.3** | Avaliações (Pessoa 1) | ~1h |
| **v1.4** | Login + Autenticação | ~1h |
| **v2.0** | Pedidos multi-restaurante (Pessoa 3) | ~3h (depende backend) |
| **v2.1** | Testes de integração | ~1h |
| **v2.2** | Empacotamento + Deploy | ~1h |

---

## Checklist para Próximo Sprint:

- [ ] Criar controller `RestaurantesController` com TableView
- [ ] Criar ficheiro `restaurantes.fxml`
- [ ] Integrar com `TascaEatsGrpcClient.listarRestaurantes()`
- [ ] Teste manual: listar restaurantes do servidor gRPC
- [ ] Criar controller `MenusController`
- [ ] Criar ficheiro `menus.fxml`
- [ ] Integrar CRUD de menus
- [ ] Teste manual: CRUD de menus
- [ ] Criar `AvaliacoesController`
- [ ] Teste manual: avaliações
- [ ] Login + autenticação (mock ou real)
- [ ] Refactoring: padrão MVC/MVVM
- [ ] Threading: converter para AsyncStub (não-bloqueante)

---

## Problemas Conhecidos / Notas:

1. **Threading**: Atualmente usa `BlockingStub` que pode congelar a UI em chamadas lentas. Usar `AsyncStub` + `Platform.runLater()` para operations longas.

2. **Serialização**: Os message types do gRPC já estão importados do `pt.ul.fc.css.tascaeats.grpc` package (gerados pelo protobuf compiler).

3. **Erro de Tipo**: Se receber "cannot find symbol" para tipos gRPC, fazer `.\mvnw clean compile` para refrescar os stubs gerados.

4. **Pessoa 3 (Pedidos/Entregas)**: Estes métodos retornam `UNIMPLEMENTED` até v1.1. Usar os stubs de Pessoa 1+2 para testar primeiro.

5. **Context Spring**: `TascaEatsFXApp.getApplicationContext()` pode ser usado para aceder a beans Spring se necessário, mas prefere-se usar injeção de dependências via gRPC client.

---

**Status Final**: ✅ Fase G framework pronto. Pronto para começar UI development com JavaFX + gRPC.
