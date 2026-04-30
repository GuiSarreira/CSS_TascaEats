# TascaEats — Fase 2: Planeamento

> Projeto Prático #2 — Construção de Sistemas de Software (CSS) 2025/2026
> Entrega: **03/05/2026** às 23:59 | Tag: `fase2`
> Peso: **35%** da nota do projeto

---

## 0. Resumo das Alterações face à Fase 1

| Tema                          | Fase 1                        | Fase 2                                     |
|-------------------------------|-------------------------------|--------------------------------------------|
| Avaliação de restaurantes     | ❌                            | ✅ Cliente avalia restaurante após compra |
| Pedido multi-restaurante      | Pedido → 1 Restaurante        | Pedido pode conter produtos de **vários** restaurantes |
| Moradas do cliente            | 1 morada (Endereco embeddable)| **Várias** moradas; escolhe uma ao fazer pedido ou insere nova |
| Atribuição de entregador      | Manual (POST)                 | **Automática** pelo sistema                |
| Pagamento — Multibanco        | `referencia`                  | + `bandeira` (provedor do serviço: Mastercard, Visa, American Express, …) |
| Pagamento — Dinheiro          | Sem campos extra              | + `troco` (informação sobre troco)         |
| Menus partilhados             | ❌ (produtos pertencem a 1 restaurante) | ✅ Menu partilhado entre restaurantes (franchising) |
| Restaurante — tipo de cozinha | ❌                            | ✅ `tipoCozinha` (italiana, chinesa, etc.)|
| Restaurante — horário         | `aberto` (boolean)            | + Horário de funcionamento                 |
| Produto — categoria           | ❌                            | ✅ `categoria` (entrada, prato principal, sobremesa, …) |
| Filtros avançados             | Busca simples (nome, cidade)  | Filtros completos por entidade             |
| Interface Web (Thymeleaf)     | ❌ (apenas REST + Swagger)    | ✅ Server-side rendering (consultas)      |
| Interface Nativa (JavaFX)     | ❌                            | ✅ Via **gRPC** (inserção de dados)       |

---

## 1. Novas Regras de Negócio

### Avaliações
- Um cliente só pode avaliar um restaurante **após concluir um pedido** nesse restaurante
- Uma avaliação tem: nota (ex: 1–5), comentário (opcional), data

### Pedido Multi-Restaurante
- Um pedido pode conter `ProdutoPedido` de restaurantes diferentes
- A relação `Pedido → Restaurante` (N:1) deve ser removida ou tornada opcional
- Cada `ProdutoPedido` já referencia um `Produto` que pertence a um `Restaurante`, logo o restaurante é inferido via produto

### Moradas Múltiplas do Cliente
- `Cliente` passa a ter uma lista de `Endereco` (1:N)
- Ao criar pedido, o cliente pode:
  - Escolher uma morada existente (por ID)
  - Enviar uma nova morada (que fica guardada no perfil)

### Menus Partilhados
- Nova entidade `Menu` (nome, descrição, lista de produtos)
- Um menu pode ser associado a **vários restaurantes** (um menu, vários restaurantes — franchising)
- Um restaurante tem **exatamente um menu** (N:1 — sem histórico de menus anteriores)
- Modificar um produto de um menu partilhado **reflete-se em todos** os restaurantes que o usam

### Atribuição Automática de Entregador
- Ao pedido ficar READY, o sistema atribui automaticamente um entregador disponível
- Critério: entregador com `disponivel=true` (pode-se refinar por `zonaAtuacao`)
- Se não houver entregador disponível, o pedido aguarda

### Pagamentos — Campos Adicionais
- **Multibanco**: + `bandeira` (String — ex: "Visa", "Mastercard")
- **Dinheiro**: + `troco` (Double — valor do troco devolvido)
- **MBWay**: mantém `telemovel`

### Restaurante — Novos Campos
- `tipoCozinha` (String ou Enum — italiana, chinesa, portuguesa, …)
- `horarioAbertura` / `horarioFecho` (LocalTime) — horário de funcionamento

### Produto — Categoria
- `categoria` (String ou Enum — ENTRADA, PRATO_PRINCIPAL, SOBREMESA, BEBIDA, …)

---

## 2. Casos de Uso — Distribuição por Interface

> **Azul = Web (Thymeleaf)** | **Roxo = Nativa (JavaFX + gRPC)** | **Castanho = Ambas**

Com base no enunciado: a interface web serve para **consultas**; a nativa para **inserção de dados**. Casos partilhados aparecem em ambas.

| # | Caso de Uso                        | Web (Thymeleaf) | Nativa (JavaFX) | Notas                                    |
|---|------------------------------------| --------------- |-----------------|------------------------------------------|
| A | Login                              | 🟤             | 🟤              | Mock auth — ambas as interfaces          |
| B | Registo de Clientes                | 🟣             | 🟣              | Inserção → nativa (ou ambas se castanho) |
| C | Registo de Admins/Entregadores     | 🟣             | 🟣              | Inserção → nativa                        |
| D | Ver/Remover/Atualizar utilizadores | 🟤             | 🟤              | Consulta + edição → ambas                |
| E | Buscar restaurantes (com filtros)  | 🔵             | —               | Consulta → web                           |
| F | Gerir restaurantes                 | 🟣             | 🟣              | Inserção → nativa                        |
| G | Gerir menus                        | 🟣             | 🟣              | **Novo** — nativa                        |
| H | Gerir produtos                     | 🟣             | 🟣              | Inserção → nativa                        |
| I | Buscar produtos (com filtros)      | 🔵             | —               | Consulta → web                           |
| J | Criação de pedido                  | 🟣             | 🟣              | Inserção → nativa                        |
| K | Pagamento                          | 🟣             | 🟣              | Inserção → nativa                        |
| L | Atualizar estado do pedido         | 🟤             | 🟤              | Ambas                                    |
| M | Atribuir entregador                | —              | —                | **Automático** (sistema)                 |
| N | Cancelar pedido                    | 🟤             | 🟤              | Ambas                                    |

> **Nota:** Azul - Web Thymeleaf, Roxo - Nativa JavaFX e Castanho - Ambas

---

## 3. Filtros a Implementar

### 3.1 Filtros de Utilizador

> **Controlo de acesso:** Só o **administrador** pode realizar a busca e filtrar Clientes (e outros utilizadores). Cada utilizador pode consultar o seu próprio perfil.

| Filtro                | Tipo          | Notas               |
|-----------------------|---------------|---------------------|
| Nome                  | String (LIKE) |                     |
| Tipo de utilizador    | Enum (CLIENTE, ADMIN, ENTREGADOR) | |
| Nº pedidos realizados | int (min/max) | Apenas clientes     |
| Nº entregas realizadas| int (min/max) | Apenas entregadores |

### 3.2 Filtros de Restaurante
| Filtro                   | Tipo                | Notas                            |
|--------------------------|---------------------|----------------------------------|
| Nome                     | String (LIKE)       |                                  |
| Nº pedidos               | int (min/max)       |                                  |
| Nº avaliações            | int (min/max)       | Novo: precisa de `Avaliacao`     |
| Morada                   | String (cidade/rua) |                                  |
| Tipo de cozinha          | String/Enum         | Novo campo                       |
| Horário (aberto/fechado) | boolean ou LocalTime range | Novo campo                |
| Preço médio dos pratos   | Double (min/max)    | Calculado via AVG(produto.preco) |

### 3.3 Filtros de Produto

> **Controlo de acesso:** A disponibilidade de um produto é verificada dentro de um restaurante específico. A popularidade é medida dentro de um determinado restaurante (não globalmente). Filtros de disponibilidade só visíveis para admins.

| Filtro          | Tipo             | Notas                                 |
|-----------------|------------------|---------------------------------------|
| Nome            | String (LIKE)    |                                       |
| Preço           | Double (min/max) |                                       |
| Categoria       | String/Enum      | Novo campo                            |
| Disponibilidade | boolean          | Verificada por restaurante; só visível para admins |
| Popularidade (nº vezes pedido) | int (min/max) + intervalo de tempo | Medida dentro de um restaurante específico; requer COUNT em ProdutoPedido |

### 3.4 Filtros de Menu
| Filtro                   | Tipo             |
|--------------------------|------------------|
| Nome                     | String (LIKE)    |
| Nº produtos associados   | int (min/max)    |
| Preço médio dos produtos | Double (min/max) |

---

## 4. Modelo de Domínio — Alterações

### 4.1 Novas Entidades

#### Avaliacao
```
- id (Long, PK)
- nota (int, 1–5, não nulo)
- comentario (String, opcional)
- dataAvaliacao (LocalDateTime)
- cliente → N:1 com Cliente
- restaurante → N:1 com Restaurante
- pedido → 1:1 com Pedido (garante que só avalia após compra)
```

#### Menu
```
- id (Long, PK)
- nome (String, não nulo)
- descricao (String)
- produtos → N:N com Produto
- restaurantes → N:N com Restaurante
```

### 4.2 Entidades Modificadas

#### Cliente
```diff
- morada (Endereco, @Embedded)
+ moradas (List<Endereco>, @ElementCollection) — múltiplas moradas via coleção
```

#### Restaurante
```diff
+ tipoCozinha (String)
+ horarioAbertura (LocalTime)
+ horarioFecho (LocalTime)
+ avaliacoes → 1:N com Avaliacao
+ menu → N:1 com Menu  (sem ligação direta a Produto)
- menu → 1:N com Produto  (REMOVIDO — catálogo agora acessível via menus)
```

#### Produto
```diff
+ categoria (String — ENTRADA, PRATO_PRINCIPAL, SOBREMESA, BEBIDA, ...)
+ menus → N:N com Menu
- restaurante → N:1 com Restaurante  (REMOVIDO — produto pertence a menus, não diretamente a restaurante)
```
> **Nota:** `disponivel` e popularidade são atributos verificados no contexto do restaurante ao qual o produto pertence (via menus). O catálogo de um restaurante é o conjunto dos produtos dos seus menus.

#### Pedido
```diff
- restaurante → N:1 com Restaurante   (REMOVER — pedido multi-restaurante)
+ enderecoEntrega → pode vir de morada existente do cliente ou nova
```
> O restaurante de cada item é inferido via `ProdutoPedido → Produto → Restaurante`.

#### Multibanco (extends Pagamento)
```diff
+ bandeira (String — provedor do serviço de cartão: "Visa", "Mastercard", "American Express", etc.)
```

#### Dinheiro (extends Pagamento)
```diff
+ troco (Double)
```

### 4.3 Diagrama de Relações Atualizado

```
User (JOINED)
  ├── Cliente ──(@ElementCollection)──> Endereco (moradas)
  │           ──(1:N)──> Pedido
  │           ──(1:N)──> Avaliacao
  ├── Admin ──(1:N)──> Restaurante
  └── Entregador ──(1:N)──> Entrega

Restaurante ──(N:1)──> Menu ──(N:N)──> Produto
Restaurante ──(1:N)──> Avaliacao

Pedido ──(1:N)──> ProdutoPedido ──(N:1)──> Produto
Pedido ──(1:1)──> Pagamento (SINGLE_TABLE: Multibanco, MBWay, Dinheiro)
Pedido ──(1:1)──> Entrega
Pedido ──(1:1)──> Avaliacao
```
> Para encontrar o restaurante de um produto em `PedidoService`: `produto.getMenus() → menu.getRestaurantes() → restaurante.isAberto()`.

---

## 5. Arquitetura — Novos Componentes

### 5.1 Backend (revisão)
```
pt.ul.fc.css.tascaeats/
├── config/      ✅  DataInitializer, OpenApiConfig
│                ❌  GrpcServerConfig (fase F)
├── entities/    ✅  Avaliacao, Menu + alterações (Cliente.moradas, Restaurante, Produto, Pagamento)
├── repositories/✅  AvaliacaoRepository, MenuRepository + specs/ (RestauranteSpecifications, MenuSpecifications)
│                ❌  UserSpecifications, ProdutoSpecifications
├── services/    ✅  AvaliacaoService, MenuService, PedidoService, EntregaService, PagamentoService
│                ✅  RestauranteService (7 filtros), UserService (adicionarMorada, removerMorada)
├── controllers/ ✅  REST: AvaliacaoController, MenuController, EntregaController, PedidoController
│                ❌  UserController (filtros), ProdutoController (filtros avançados)
├── web/         ✅  Web: WebClienteController, WebPedidoController, WebPagamentoController
│                ✅  Web: WebMenuController, WebAvaliacaoController, WebRestauranteController
├── dto/         ✅  AvaliacaoRequest/Response, MenuRequest/Response, CriarPedidoRequest (moradaIndex)
│                ✅  EntregaResponse, PagamentoRequest/Response, RestauranteResponse + restantes
├── exceptions/  ✅  GlobalExceptionHandler, ErrorResponse
├── grpc/        ❌  ★ NOVO — serviço gRPC server-side (fase F)
└── proto/       ❌  ★ NOVO — definições .proto (fase F)
```

### 5.2 Interface Web — Thymeleaf
```
src/main/resources/
├── templates/
│   ├── layout.html             ❌ Template base (navbar, footer)
│   ├── login.html              ❌ Página de login
│   ├── home.html               ❌ Dashboard
│   ├── cliente/
│   │   └── moradas.html        ✅ Gestão de moradas do cliente
│   ├── pedidos/
│   │   ├── novo.html           ✅ Carrinho multi-restaurante
│   │   ├── lista.html          ✅ Lista de pedidos com filtro de estado
│   │   └── detalhe.html        ✅ Detalhe (itens, entrega, pagamento, ações)
│   ├── pagamentos/
│   │   └── form.html           ✅ Formulário de pagamento (MBWay, Multibanco, Dinheiro)
│   ├── menus/
│   │   ├── index.html          ✅ Lista com filtros (nome, min/max produtos, min/max preço)
│   │   ├── form.html           ✅ Criar/editar menu (checkboxes produtos e restaurantes)
│   │   └── detalhe.html        ✅ Detalhe (produtos, restaurantes associados, associar)
│   ├── restaurantes/
│   │   ├── index.html          ✅ Busca com filtros avançados (card grid)
│   │   └── detalhe.html        ✅ Detalhe (info, produtos, avaliações)
│   ├── avaliacoes/
│   │   ├── form.html           ❌ Formulário de avaliação
│   │   └── lista.html          ❌ Lista de avaliações
│   ├── users/                  ❌ Listagem/detalhe utilizadores
│   ├── produtos/               ❌ Busca com filtros
│   └── fragments/              ❌ Componentes reutilizáveis
└── static/
    ├── css/                    ❌
    └── js/                     ❌
```

Controllers Thymeleaf — package `pt.ul.fc.css.tascaeats.web`:
```
pt.ul.fc.css.tascaeats/web/
├── WebAuthController.java        ❌ por criar
├── WebUserController.java        ❌ por criar
├── WebProdutoController.java     ❌ por criar
├── WebClienteController.java     ✅ GET/POST moradas
├── WebPedidoController.java      ✅ novo, lista, detalhe, cancelar
├── WebPagamentoController.java   ✅ formulário de pagamento
├── WebMenuController.java        ✅ CRUD de menus
├── WebAvaliacaoController.java   ✅ criar/listar avaliações
└── WebRestauranteController.java ✅ listar com filtros, detalhe
```

### 5.3 Interface Nativa — JavaFX + gRPC

Módulo separado ou package dedicado (❌ ainda não iniciado — fase G):
```
pt.ul.fc.css.tascaeats/
└── javafx/                      ❌
    ├── TascaEatsFXApp.java       ❌ Application entry point
    ├── controllers/              ❌ FX Controllers (FXML)
    ├── views/                    ❌ Ficheiros FXML
    ├── grpc/                     ❌ gRPC client stubs
    └── model/                    ❌ View models
```

Ficheiros FXML em `src/main/resources/fxml/` ou em módulo separado.

---

## 6. Stack Tecnológica — Novas Dependências

### pom.xml — Adicionar
```xml
<!-- gRPC -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
</dependency>

<!-- Protobuf compiler plugin -->
<plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
</plugin>

<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
</dependency>
```

> **Nota:** Thymeleaf já está no pom.xml. Apenas faltam gRPC e JavaFX.

---

## 7. Definições gRPC (.proto)

Ficheiro `src/main/proto/tascaeats.proto` com serviços para os casos de uso da interface nativa:

```protobuf
service TascaEatsService {
  // Auth
  rpc Login (LoginRequest) returns (LoginResponse);

  // Users
  rpc RegistarCliente (RegistarClienteRequest) returns (UserResponse);
  rpc RegistarAdmin (RegistarAdminRequest) returns (UserResponse);
  rpc RegistarEntregador (RegistarEntregadorRequest) returns (UserResponse);
  rpc AtualizarUser (AtualizarUserRequest) returns (UserResponse);
  rpc RemoverUser (RemoverUserRequest) returns (Empty);

  // Restaurantes
  rpc CriarRestaurante (CriarRestauranteRequest) returns (RestauranteResponse);
  rpc AtualizarRestaurante (...) returns (...);

  // Menus
  rpc CriarMenu (CriarMenuRequest) returns (MenuResponse);
  rpc AssociarMenuRestaurante (...) returns (...);
  rpc AdicionarProdutoMenu (...) returns (...);

  // Produtos
  rpc CriarProduto (CriarProdutoRequest) returns (ProdutoResponse);
  rpc AtualizarProduto (...) returns (...);
  rpc RemoverProduto (...) returns (...);

  // Pedidos
  rpc CriarPedido (CriarPedidoRequest) returns (PedidoResponse);
  rpc CancelarPedido (...) returns (...);
  rpc AtualizarEstadoPedido (...) returns (...);

  // Pagamento
  rpc RegistarPagamento (PagamentoRequest) returns (PagamentoResponse);
}
```

---

## 8. Queries de Negócio (validação do modelo)

> **Legenda:** ✅ implementada como `@Query` num repositório · **C** query relacionada existe mas não cobre o caso exacto · `[ ]` não implementada

O modelo atualizado deve permitir responder a:

1. `✅` **No caso de pagamento com numerário, qual é a média do troco?**
   → `SELECT AVG(d.troco) FROM Dinheiro d WHERE d.status = COMPLETED AND d.troco IS NOT NULL`
   → `PagamentoRepository.findMediaTroco(): Double`
2. `[ ]` **Qual é o item mais pedido de um restaurante?**
   → `SELECT pp.produto, SUM(pp.quantity) ... GROUP BY pp.produto ORDER BY ... DESC`
   → Estrutura `ProdutoPedido` suporta a query; não implementada
3. `✅` **Qual o entregador com mais entregas para um restaurante específico?**
   → `JOIN Entrega → Pedido → ProdutoPedido → Produto → Menu → Restaurante` + `GROUP BY entregador`
   → `EntregaRepository.findEntregadorComMaisEntregasParaRestaurante(restauranteId): List<Object[]>`
4. `✅` **Qual o restaurante mais popular de uma franquia (menu partilhado)?**
   → `JOIN Menu → Restaurantes + JOIN Menu → Produtos → itensPedido` + `COUNT(DISTINCT pedido)` por restaurante
   → `MenuRepository.findRestauranteMaisPopularDoMenu(menuId): List<Object[]>`
5. **C** **Qual é o cliente que mais pedidos fez num intervalo de tempo?**
   → `SELECT p.cliente, COUNT(*) FROM Pedido p WHERE p.dataHora BETWEEN ... GROUP BY ...`
   → `ClienteRepository.findClienteComMaisPedidos(Pageable)` existe **sem filtro temporal**
   → `PedidoRepository.findPedidosComFiltros(clienteId, status, dataMin, dataMax)` filtra por datas mas não agrega por cliente
   → `PedidoRepository.findMediaPedidosPorClientePorMes()` agrega por cliente/mês mas não aceita intervalo livre

> **Nota:** Podemos e devemos fazer mais queries 

---

## 9. Plano de Implementação — Fases

> **Legenda:** ✅ concluído e com commit · **C** ficheiros alterados sem commit · `[ ]` não iniciado

### Fase A — Revisão do Modelo de Domínio
- ✅ Criar entidade `Avaliacao`
- ✅ Criar entidade `Menu` (N:N com Produto, N:1 com Restaurante — FK `menu_id` em `Restaurante`)
- ✅ Atualizar `Cliente.morada` → `Cliente.moradas` (@ElementCollection<Endereco>)
- ✅ Adicionar `tipoCozinha`, `horarioAbertura`, `horarioFecho` ao `Restaurante`
- ✅ Adicionar `categoria` ao `Produto`
- ✅ Adicionar `bandeira` ao `Multibanco`
- ✅ Adicionar `troco` ao `Dinheiro`
- ✅ Remover/tornar opcional relação `Pedido → Restaurante` (pedido multi-restaurante)
- ✅ Atualizar `Pedido` para aceitar morada de lista do cliente ou nova
- ✅ Validar schema gerado pelo Hibernate

### Fase B — Repositórios e Filtros
- ✅ Criar `AvaliacaoRepository`
- ✅ Criar `MenuRepository`
- [ ] Implementar filtros de utilizador (nome, tipo, nº pedidos, nº entregas) — Specifications ou queries custom
- ✅ Implementar filtros de restaurante (nome, nº pedidos, nº avaliações, morada, cozinha, horário, preço médio) — `RestauranteSpecifications` (7 filtros)
- [ ] Implementar filtros de produto (nome, preço, categoria, disponibilidade, popularidade) — `ProdutoSpecifications` não criado
- ✅ Implementar filtros de menu (nome, nº produtos, preço médio) — `MenuSpecifications` (3 filtros)

### Fase C — Serviços (lógica de negócio)
- ✅ `AvaliacaoService` — criar avaliação (validar que cliente tem pedido concluído, pedido-based uniqueness)
- ✅ `MenuService` — CRUD de menus, associar a restaurantes, gerir produtos no menu
- ✅ Atualizar `PedidoService` — pedido multi-restaurante, morada flexível
- ✅ Atualizar `EntregaService` — atribuição automática de entregador
- ✅ Atualizar `PagamentoService` — novos campos (bandeira, troco)
- ✅ Atualizar `RestauranteService` com `listarRestaurantesComFiltros` (7 filtros)
- ✅ Atualizar `MenuService` com `listarMenusComFiltros` (3 filtros)

### Fase D — Controllers REST (atualização)
- ✅ `AvaliacaoController` — endpoints REST (POST, GET, GET media, PUT, DELETE) + DTOs (AvaliacaoRequest, AvaliacaoResponse)
- ✅ `MenuController` — CRUD + associação a restaurantes (8 endpoints)
- ✅ Atualizar `RestauranteController` com filtros (nome, tipoCozinha, horario, preço, avaliações, cidade, minPedidos)
- [ ] Atualizar `UserController` com filtros (nome, tipo, nº pedidos, nº entregas)
- [ ] Atualizar `ProdutoController` com filtros avançados (categoria, disponibilidade, popularidade)
- ✅ Atualizar `PedidoController` — pedido multi-restaurante, moradaIndex, status filter
- ✅ Atualizar `EntregaController` — GET /api/entregas/{id}
- ✅ Atualizar DTOs (MenuRequest, MenuResponse, RestauranteResponse, PedidoRequest com moradaIndex, AvaliacaoRequest, AvaliacaoResponse)

### Fase E — Interface Web (Thymeleaf)
- [ ] Criar template base (`layout.html`) com navbar e estilos
- [ ] Página de login (`login.html`)
- ✅ Listagem/busca de restaurantes com filtros (`WebRestauranteController`)
- [ ] Listagem/busca de produtos com filtros
- [ ] Ver/editar utilizadores
- ✅ `WebMenuController` — CRUD de menus via Thymeleaf (listar, criar, editar, detalhe, associar restaurante)
- ✅ `WebAvaliacaoController` — criar avaliação, listar avaliações por cliente/restaurante
- ✅ `WebClienteController` — gerir moradas (listar, adicionar, remover)
- ✅ `WebPedidoController` — novo pedido (carrinho), listar, detalhe, cancelar
- ✅ `WebPagamentoController` — formulário de pagamento (MBWay, Multibanco, Dinheiro)
- ✅ `WebRestauranteController` — listar restaurantes com filtros avançados, detalhe com produtos e avaliações
- ✅ Todos os Web controllers movidos para package `pt.ul.fc.css.tascaeats.web`
- ✅ Templates: `cliente/moradas.html`, `pedidos/novo.html`, `pedidos/lista.html`, `pedidos/detalhe.html`, `pagamentos/form.html`
- ✅ Templates: `menus/index.html`, `menus/form.html`, `menus/detalhe.html`
- ✅ Templates: `restaurantes/index.html`, `restaurantes/detalhe.html`
- [ ] Templates: `avaliacoes/form.html`, `avaliacoes/lista.html`
- [ ] Testar toda a navegação no browser

> **Nota:** Para testar na web (Windows):
> 1. Correr: `docker compose up -d pgserver` (apenas PostgreSQL)
> 2. Correr: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-24" ; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH" ; .\mvnw spring-boot:run`
> 3. Abrir: http://localhost:8080

### Fase F — gRPC Server
- [ ] Definir ficheiro `.proto` com todos os serviços e mensagens
- [ ] Configurar `protobuf-maven-plugin` no pom.xml
- [ ] Gerar stubs Java a partir do `.proto`
- [ ] Implementar `TascaEatsGrpcService` (server-side) que delega nos services existentes
- [ ] Configurar servidor gRPC (porta separada, ex: 9090)
- [ ] Atualizar `docker-compose.yml` para expor porta gRPC
- [ ] Testar com `grpcurl` ou similar

### Fase G — Interface Nativa (JavaFX)
- [ ] Adicionar dependências JavaFX ao pom.xml
- [ ] Criar `TascaEatsFXApp.java` (entry point)
- [ ] Criar cliente gRPC (stubs)
- [ ] Ecrã de login
- [ ] Ecrã de registo (clientes, admins, entregadores)
- [ ] Ecrã de gestão de restaurantes
- [ ] Ecrã de gestão de menus
- [ ] Ecrã de gestão de produtos
- [ ] Ecrã de criação de pedido (multi-restaurante, escolha de morada)
- [ ] Ecrã de pagamento
- [ ] Ecrã de estado do pedido / cancelamento
- [ ] Ficheiros FXML para cada ecrã

### Fase H — Testes
- ✅ Atualizar testes existentes (modelo alterado — Menu N:1, Avaliacao)
- ✅ Testes unitários para novos serviços (AvaliacaoService, MenuService)
- ✅ Testes para filtros (MenuSpecifications, RestauranteSpecifications)
- ✅ Testes para atribuição automática de entregador
- ✅ Testes para pedido multi-restaurante
- ✅ 204 testes, 0 falhas (30/04/2026)

### Fase I — Docker + Finalização
- [ ] Atualizar `docker-compose.yml` (porta gRPC, JavaFX se aplicável)
- [ ] Testar `docker compose up --build` completo
- [ ] Testar todos os endpoints REST via Swagger
- [ ] Testar interface web no browser
- [ ] Testar interface JavaFX
- [ ] Gravar **2 vídeos** (um para web, um para JavaFX)
- [ ] Atualizar README.md com links dos vídeos
- [ ] `git tag fase2` + `git push origin fase2`
- [ ] Confirmar acesso da conta CSS000

---

## 10. Vídeos Demonstrativos

Dois vídeos separados:

### Vídeo 1 — Interface Web (Thymeleaf)
Demonstrar os casos de uso atribuídos à web:
- Login
- Buscar restaurantes (com filtros)
- Buscar produtos (com filtros)
- Ver/editar utilizadores
- Ver estado de pedidos
- Cancelar pedido

### Vídeo 2 — Interface Nativa (JavaFX)
Demonstrar os casos de uso atribuídos à nativa:
- Login
- Registo de utilizadores
- Gerir restaurantes
- Gerir menus (partilhados)
- Gerir produtos
- Criar pedido (multi-restaurante, escolha de morada)
- Pagamento
- Atualizar estado do pedido
- Cancelar pedido

**Em ambos os vídeos demonstrar:**
- Avaliação de restaurante após compra
- Menu partilhado: modificar produto reflete em todos os restaurantes
- Moradas múltiplas do cliente
- Atribuição automática de entregador

---

## 11. Riscos e Notas

| Risco | Mitigação |
|-------|-----------|
| gRPC + JavaFX são tecnologias novas para o grupo | Começar por protótipos simples; tutoriais oficiais |
| Pedido multi-restaurante altera lógica core | Refactoring cuidadoso; testes primeiro |
| Cobertura pode cair com novos componentes | Excluir classes de config/UI do JaCoCo; testar serviços |
| JavaFX não corre facilmente em Docker | JavaFX é cliente nativo — corre **fora** do Docker, liga-se ao backend via gRPC |
| Tempo limitado (~2 semanas) | Priorizar: modelo → filtros → gRPC → web → JavaFX |

---

## 12. Cronograma Sugerido

| Semana | Tarefas |
|--------|---------|
| **Sem. 1** (14–20 Abr) | Fase A (modelo) + Fase B (repositórios/filtros) + Fase C (serviços) |
| **Sem. 2** (21–27 Abr) | Fase D (controllers) + Fase E (Thymeleaf) + Fase F (gRPC) |
| **Sem. 3** (28 Abr–03 Mai) | Fase G (JavaFX) + Fase H (testes) + Fase I (docker/vídeos/entrega) |

---

## 13. Entrega

```bash
git tag fase2
git push origin fase2
```

Confirmar que:
- [ ] Conta **CSS000** tem acesso ao repositório na tag `fase2`
- [ ] `docker compose up --build` funciona sem erros de compilação
- [ ] Links dos vídeos estão no `README.md`
- [ ] Todos os casos de uso demonstrados nos vídeos