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
| Pagamento — Multibanco        | `referencia`                  | + `bandeira` (bandeira do cartão)          |
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
- Um menu pode ser associado a **vários restaurantes** (N:N)
- Um restaurante pode ter **vários menus**
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
| Filtro          | Tipo             | Notas                                 |
|-----------------|------------------|---------------------------------------|
| Nome            | String (LIKE)    |                                       |
| Preço           | Double (min/max) |                                       |
| Categoria       | String/Enum      | Novo campo                            |
| Disponibilidade | boolean          | Só visível para admins e entregadores |
| Popularidade (nº vezes pedido) | int (min/max) + intervalo de tempo | Requer COUNT em ProdutoPedido |

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
+ menus → N:N com Menu
```

#### Produto
```diff
+ categoria (String — ENTRADA, PRATO_PRINCIPAL, SOBREMESA, BEBIDA, ...)
+ menus → N:N com Menu (lado inverso)
```

#### Pedido
```diff
- restaurante → N:1 com Restaurante   (REMOVER — pedido multi-restaurante)
+ enderecoEntrega → pode vir de morada existente do cliente ou nova
```
> O restaurante de cada item é inferido via `ProdutoPedido → Produto → Restaurante`.

#### Multibanco (extends Pagamento)
```diff
+ bandeira (String — "Visa", "Mastercard", etc.)
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

Restaurante ──(1:N)──> Produto
Restaurante ──(N:N)──> Menu ──(N:N)──> Produto
Restaurante ──(1:N)──> Avaliacao

Pedido ──(1:N)──> ProdutoPedido ──(N:1)──> Produto
Pedido ──(1:1)──> Pagamento (SINGLE_TABLE: Multibanco, MBWay, Dinheiro)
Pedido ──(1:1)──> Entrega
Pedido ──(1:1)──> Avaliacao
```

---

## 5. Arquitetura — Novos Componentes

### 5.1 Backend (revisão)
```
pt.ul.fc.css.tascaeats/
├── config/           → DataInitializer, OpenApiConfig, GrpcServerConfig
├── entities/         → + Avaliacao, Menu; alterações nas existentes (Cliente.moradas)
├── repositories/     → + AvaliacaoRepository, MenuRepository
├── services/         → + AvaliacaoService, MenuService; alterações nos filtros
├── controllers/      → + AvaliacaoController, MenuController; filtros nos existentes
├── dto/              → + novos Request/Response DTOs para Avaliacao, Menu, filtros
├── exceptions/       → (sem alterações previstas)
├── grpc/             → ★ NOVO — serviço gRPC (server-side)
└── proto/            → ★ NOVO — definições .proto
```

### 5.2 Interface Web — Thymeleaf
```
src/main/resources/
├── templates/
│   ├── layout.html          → Template base (navbar, footer)
│   ├── login.html           → Página de login
│   ├── home.html            → Dashboard
│   ├── users/               → Listagem/detalhe utilizadores
│   ├── restaurantes/        → Busca com filtros, detalhe
│   ├── produtos/            → Busca com filtros
│   ├── pedidos/             → Estado, cancelamento
│   └── fragments/           → Componentes reutilizáveis
└── static/
    ├── css/
    └── js/
```

Novos controllers Thymeleaf (separados dos REST):
```
pt.ul.fc.css.tascaeats/
└── web/
    ├── WebAuthController.java
    ├── WebUserController.java
    ├── WebRestauranteController.java
    ├── WebProdutoController.java
    ├── WebPedidoController.java
    └── ...
```

### 5.3 Interface Nativa — JavaFX + gRPC

Módulo separado ou package dedicado:
```
pt.ul.fc.css.tascaeats/
└── javafx/
    ├── TascaEatsFXApp.java       → Application entry point
    ├── controllers/              → FX Controllers (FXML)
    ├── views/                    → Ficheiros FXML
    ├── grpc/                     → gRPC client stubs
    └── model/                    → View models
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

O modelo atualizado deve permitir responder a:

1. **No caso de pagamento com numerário, qual é a média do troco?**
   → `SELECT AVG(d.troco) FROM Dinheiro d`
2. **Qual é o item mais pedido de um restaurante?**
   → `SELECT pp.produto, SUM(pp.quantity) ... GROUP BY pp.produto ORDER BY ... DESC`
3. **Qual o entregador com mais entregas para um restaurante específico?**
   → `JOIN Entrega → Pedido → ProdutoPedido → Produto → Restaurante` + `GROUP BY entregador`
4. **Qual o restaurante mais popular de uma franquia (menu partilhado)?**
   → Via `Menu → Restaurantes` + contagem de pedidos
5. **Qual é o cliente que mais pedidos fez num intervalo de tempo?**
   → `SELECT p.cliente, COUNT(*) FROM Pedido p WHERE p.dataHora BETWEEN ... GROUP BY ...`

> **Nota:** Podemos e devemos fazer mais queries 

---

## 9. Plano de Implementação — Fases

### Fase A — Revisão do Modelo de Domínio
- [ ] Criar entidade `Avaliacao`
- [ ] Criar entidade `Menu` (N:N com Produto e Restaurante)
- [ ] Atualizar `Cliente.morada` → `Cliente.moradas` (@ElementCollection<Endereco>)
- [ ] Adicionar `tipoCozinha`, `horarioAbertura`, `horarioFecho` ao `Restaurante`
- [ ] Adicionar `categoria` ao `Produto`
- [ ] Adicionar `bandeira` ao `Multibanco`
- [ ] Adicionar `troco` ao `Dinheiro`
- [ ] Remover/tornar opcional relação `Pedido → Restaurante` (pedido multi-restaurante)
- [ ] Atualizar `Pedido` para aceitar morada de lista do cliente ou nova
- [ ] Validar schema gerado pelo Hibernate

### Fase B — Repositórios e Filtros
- [ ] Criar `AvaliacaoRepository`
- [ ] Criar `MenuRepository`
- [ ] Implementar filtros de utilizador (nome, tipo, nº pedidos, nº entregas) — Specifications ou queries custom
- [ ] Implementar filtros de restaurante (nome, nº pedidos, nº avaliações, morada, cozinha, horário, preço médio)
- [ ] Implementar filtros de produto (nome, preço, categoria, disponibilidade, popularidade)
- [ ] Implementar filtros de menu (nome, nº produtos, preço médio)

### Fase C — Serviços (lógica de negócio)
- [ ] `AvaliacaoService` — criar avaliação (validar que cliente tem pedido concluído)
- [ ] `MenuService` — CRUD de menus, associar a restaurantes, gerir produtos no menu
- [ ] Atualizar `PedidoService` — pedido multi-restaurante, morada flexível
- [ ] Atualizar `EntregaService` — atribuição automática de entregador
- [ ] Atualizar `PagamentoService` — novos campos (bandeira, troco)
- [ ] Atualizar serviços existentes com suporte a filtros

### Fase D — Controllers REST (atualização)
- [ ] `AvaliacaoController` — endpoints REST
- [ ] `MenuController` — CRUD + associação a restaurantes
- [ ] Atualizar `UserController`, `RestauranteController`, `ProdutoController` com filtros
- [ ] Atualizar `PedidoController` — pedido multi-restaurante
- [ ] Atualizar DTOs (Request/Response) para novas entidades e campos

### Fase E — Interface Web (Thymeleaf)
- [ ] Criar template base (`layout.html`) com navbar e estilos
- [ ] Página de login (`login.html`)
- [ ] Listagem/busca de restaurantes com filtros
- [ ] Listagem/busca de produtos com filtros
- [ ] Ver/editar utilizadores
- [ ] Ver estado de pedidos, cancelar pedido
- [ ] Criar controllers web (`@Controller` que retornam views, não JSON)
- [ ] Testar toda a navegação no browser

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
- [ ] Atualizar testes existentes (modelo alterado)
- [ ] Testes unitários para novos serviços (Avaliacao, Menu)
- [ ] Testes para filtros
- [ ] Testes para atribuição automática de entregador
- [ ] Testes para pedido multi-restaurante
- [ ] Manter cobertura ≥ 80%

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