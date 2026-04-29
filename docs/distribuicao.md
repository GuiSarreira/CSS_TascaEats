### Estratégia: Divisão por Feature (Backend + Frontend + gRPC)

Cada um trabalha numa **feature completa**: entidade → service → repository → controller REST → web UI → gRPC.

**Semana 1**: Todos fazem modelo + repositórios + serviços em paralelo
**Semana 2**: Todos fazem controllers REST + web UI em paralelo
**Semana 3**: Todos fazem gRPC + testes + video + finalização

**Dependência mínima**: Só em Fase I (Docker final).

---

> **Legenda:** ✅ concluído e com commit · **C** ficheiros alterados sem commit · `[ ]` não iniciado

---

## Ze — Feature: Avaliações + Autenticação + Base (Auth User gRPC)

### Semana 1: Modelo + Serviço + Repositório

**Modelo de Domínio:**
- ✅ Criar entidade `Avaliacao`:
  - `id (Long, PK)`, `nota (int, 1-5)`, `comentario (String)`, `dataAvaliacao (LocalDateTime)`
  - `cliente → @ManyToOne com Cliente`, `restaurante → @ManyToOne com Restaurante`
  - `pedido → @OneToOne com Pedido` (garante que só avalia após compra)
  - Uniqueness: `pedido_id unique=true` (1 avaliação por pedido; cliente pode avaliar mesmo restaurante várias vezes via pedidos diferentes)

**Repositório:**
- ✅ `AvaliacaoRepository`:
  - `findByClienteIdAndRestauranteId(clienteId, restauranteId): Optional<Avaliacao>`
  - `findByRestauranteId(restauranteId): List<Avaliacao>`
  - `findByClienteId(clienteId): List<Avaliacao>`
  - `findByPedidoId(pedidoId): Optional<Avaliacao>`

**Service:**
- ✅ `AvaliacaoService`:
  - `criarAvaliacao(clienteId, restauranteId, pedidoId, nota, comentario)` — valida pedido do cliente, status DELIVERED, sem avaliação dupla por pedido
  - `obterAvaliacoesPorRestaurante(restauranteId)` — retorna lista com media de notas
  - `atualizarAvaliacao(avaliacaoId, nota, comentario)` — só pelo cliente criador
  - `removerAvaliacao(avaliacaoId)` — só pelo cliente criador ou admin
  - `mediaNotasRestaurante(restauranteId): Double`

**Testes:**
- [ ] Testes de validação de avaliação (nota range, pedido DELIVERED, etc.)

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- [ ] `AvaliacaoController` (não criado):
  - `POST /api/avaliacoes` — criar
  - `GET /api/avaliacoes?restauranteId=X` — listar por restaurante
  - `GET /api/avaliacoes?clienteId=X` — listar por cliente
  - `PUT /api/avaliacoes/{id}` — atualizar
  - `DELETE /api/avaliacoes/{id}` — remover
  - DTOs: `AvaliacaoRequest`, `AvaliacaoResponse`

**Web UI (Thymeleaf):**
- [ ] `WebAvaliacaoController` (controller MVC, não REST):
  - `GET /avaliacoes/novo?pedidoId=X` — formulário de nova avaliação
  - `POST /avaliacoes` — submeter avaliação (POST para API REST)
  - `GET /avaliacoes` — listar avaliações do utilizador
- [ ] Templates:
  - `avaliacoes/form.html` — formulário (nota select 1-5, comentário textarea, submit)
  - `avaliacoes/lista.html` — lista com filtro por cliente/restaurante

---

### Semana 3: gRPC + Testes

**gRPC (colaboração mínima com outros):**
- [ ] Adicionar ao `.proto`:
  ```protobuf
  rpc CriarAvaliacao (CriarAvaliacaoRequest) returns (AvaliacaoResponse);
  rpc ListarAvaliacoes (ListarAvaliacoesRequest) returns (ListarAvaliacoesResponse);
  rpc AtualizarAvaliacao (AtualizarAvaliacaoRequest) returns (AvaliacaoResponse);
  rpc RemoverAvaliacao (RemoverAvaliacaoRequest) returns (Empty);
  ```
- [ ] Implementar methods no `TascaEatsGrpcService`

**Testes:**
- [ ] Unit tests AvaliacaoService
- [ ] Integration tests AvaliacaoController
- [ ] Testes gRPC

**Video:**
- [ ] Demonstrar: criar pedido DELIVERED → avaliar restaurante → ver avaliações

---

## Rafa — Feature: Menus Partilhados + Filtros Avançados

### Semana 1: Modelo + Serviço + Repositório

**Modelo de Domínio:**
- ✅ Criar entidade `Menu`:
  - `id (Long, PK)`, `nome (String, not null)`, `descricao (String)`
  - `@OneToMany(mappedBy="menu") restaurantes` (inverso do N:1) — lado inverso
  - `@ManyToMany com Produto` (N:N via tabela `menu_produto`) — lado owner
- ✅ Atualizar `Restaurante`: `@ManyToOne menu` (N:1 — FK `menu_id`); removida ligação direta a `Produto`
- ✅ Atualizar `Produto`: `@ManyToMany(mappedBy="produtos") menus`; removido `@ManyToOne Restaurante restaurante`
- ✅ Adicionar campos ao `Restaurante`:
  - `tipoCozinha (String)`, `horarioAbertura (LocalTime)`, `horarioFecho (LocalTime)`

**Repositório + Specifications:**
- ✅ `MenuRepository` com `JpaSpecificationExecutor<Menu>`
- ✅ `MenuSpecifications`: `comNome`, `quantidadeProdutosEntre`, `precoMedioEntre` (3 filtros)
- ✅ `RestauranteSpecifications`: `comNome`, `comTipoCozinha`, `abertoNoHorario`, `precoMedioEntre`, `comMinimoAvaliacoes`, `comCidade`, `comMinimoPedidos` (7 filtros)

**Service:**
- ✅ `MenuService`:
  - `criarMenu(nome, descricao, produtos, restaurantes): Menu`
  - `atualizarMenu(menuId, nome, descricao, produtos, restaurantes): Menu`
  - `removerMenu(menuId): void`
  - `associarMenuRestaurante(menuId, restauranteId): void` — relação N:1
  - `removerMenuRestaurante(menuId, restauranteId): void`
  - `adicionarProdutoMenu(menuId, produtoId): void`
  - `removerProdutoMenu(menuId, produtoId): void`
  - `listarMenusComFiltros(nome, minProd, maxProd, minPreco, maxPreco): List<Menu>`
- ✅ `RestauranteService` — `listarRestaurantesComFiltros(nome, tipoCozinha, horario, minPreco, maxPreco, minAvaliacoes, cidade, minPedidos): List<Restaurante>`

**Testes:**
- ✅ Tests de Menu (CRUD, relações N:1 e N:N)
- ✅ Tests de Specifications para filtros

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- ✅ `MenuController`:
  - `POST /api/menus` — criar menu
  - `PUT /api/menus/{id}` — atualizar menu
  - `DELETE /api/menus/{id}` — remover menu
  - `GET /api/menus?nome=&minProdutos=&maxProdutos=&minPreco=&maxPreco=` — listar com filtros
  - `POST /api/menus/{menuId}/restaurantes/{restauranteId}` — associar
  - `DELETE /api/menus/{menuId}/restaurantes/{restauranteId}` — desassociar
  - `POST /api/menus/{menuId}/produtos/{produtoId}` — adicionar produto
  - `DELETE /api/menus/{menuId}/produtos/{produtoId}` — remover produto
  - DTOs: `MenuRequest`, `MenuResponse`
- ✅ Atualizar `RestauranteController`:
  - `GET /api/restaurantes/filtros?nome=&tipoCozinha=&horario=&minPreco=&maxPreco=&minAvaliacoes=&cidade=&minPedidos=`
  - DTOs atualizados com novos campos

**Web UI (Thymeleaf):**
- ✅ `WebMenuController`:
  - `GET /menus` — listar com filtros
  - `GET /menus/novo` — formulário novo menu
  - `POST /menus` — submeter novo menu
  - `GET /menus/{id}/editar` — formulário edição
  - `POST /menus/{id}` — submeter edição
  - `GET /menus/{id}` — detalhe (produtos, restaurantes)
  - `POST /menus/{id}/restaurantes/{rid}` — associar restaurante
  - `POST /menus/{id}/restaurantes/{rid}/remover` — desassociar restaurante
  - `POST /menus/{id}/remover` — apagar menu
- [ ] `WebRestauranteController` (não criado):
  - `GET /restaurantes?...filtros` — listar com filtros avançados (cozinha, horário, preço)
  - `GET /restaurantes/{id}` — detalhe com menus e produtos
- [ ] Templates:
  - `menus/index.html` — lista com filtros
  - `menus/form.html` — formulário
  - `menus/detalhe.html` — detalhe (produtos, restaurantes associados)
  - `restaurantes/index.html` — lista com filtros avançados
  - `restaurantes/detalhe.html` — detalhe com menus

---

### Semana 3: gRPC + Testes

**gRPC:**
- [ ] Adicionar ao `.proto`:
  ```protobuf
  rpc CriarMenu (CriarMenuRequest) returns (MenuResponse);
  rpc ListarMenus (ListarMenusRequest) returns (ListarMenusResponse);
  rpc AtualizarMenu (AtualizarMenuRequest) returns (MenuResponse);
  rpc RemoverMenu (RemoverMenuRequest) returns (Empty);
  rpc AssociarMenuRestaurante (...) returns (Empty);
  rpc ListarRestaurantes (ListarRestaurantesRequest) returns (ListarRestaurantesResponse);
  ```
- [ ] Implementar no `TascaEatsGrpcService`

**Testes:**
- ✅ Unit tests MenuService
- [ ] Integration tests MenuController
- [ ] Testes gRPC

**Video:**
- [ ] Demonstrar: criar menu → associar a restaurante(s) → modificar produto no menu → reflete em todos restaurantes

---

## Guilherme — Feature: Pedidos Multi-Restaurante + Entrega Automática + Pagamentos

### Semana 1: Modelo + Serviço + Repositório

**Modelo de Domínio:**
- ✅ Atualizar `Cliente`:
  - Substituir `morada (Endereco, @Embedded)` por `@ElementCollection<Endereco> moradas`
  - JPA gera tabela `cliente_moradas` automaticamente
- ✅ Atualizar `Pedido`:
  - **Remover** `@ManyToOne restaurante` — agora é multi-restaurante
  - Manter `enderecoEntrega` (pode ser de morada existente ou nova)
  - Adicionação de `moradaId` ou nova morada ao criar pedido
- ✅ Atualizar `Multibanco`: adicionar `bandeira (String)`
- ✅ Atualizar `Dinheiro`: adicionar `troco (Double)`
- ✅ Atualizar `Entrega`:
  - `horaRetirada (LocalDateTime)` — preenchida em `iniciarEntrega()`
  - Validação: atribuição automática só se `entregador.disponivel == true`

**Repositório:**
- ✅ `ClienteRepository` — atualizado: `findClienteComMaisPedidos`, `findClientesSemCompras`, `findAllClientesComTotalPedidos`
- ✅ `PedidoRepository`:
  - `findByClienteIdAndStatus(clienteId, status): List<Pedido>`
  - `findPedidosComFiltros(clienteId, status, dataMin, dataMax): List<Pedido>`
  - `findPedidosReadyWithoutEntrega(): List<Pedido>` — para atribuição automática
- ✅ `EntregaRepository`:
  - `findByStatusAndEntregadorDisponivel(status, true): List<Entrega>`
  - `findEntregasComFiltros(restauranteId, entregadorId): List<Entrega>`

**Service:**
- ✅ `PedidoService` — atualizado:
  - `criarPedido(clienteId, List<{produtoId, quantity}>, moradaId ou novaMorada)`
  - `avancarEstado(pedidoId, novoStatus)` — transições de estado
  - Após transição para READY: chama `EntregaService.atribuirEntregadorAutomatico(pedido)`
  - `cancelarPedido(pedidoId)` — se CREATED ou PAID
- ✅ `EntregaService` — atualizado:
  - `atribuirEntregadorAutomatico(pedido): Entrega`
  - `iniciarEntrega(entregaId)`
  - `concluirEntrega(entregaId)`
- ✅ `PagamentoService` — atualizado:
  - Se MULTIBANCO: aceita `referencia`, `bandeira`
  - Se MBWAY: aceita `telemovel`
  - Se DINHEIRO: aceita `troco`

**Testes:**
- ✅ Tests de pedido multi-restaurante (validações, preço)
- ✅ Tests de atribuição automática entregador
- ✅ Tests de pagamento (novos campos)

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- **C** Atualizar `PedidoController`:
  - `POST /api/pedidos` — criar (aceitar items de restaurantes diferentes, morada)
  - `GET /api/pedidos/{id}` — detalhe
  - `GET /api/pedidos/cliente/{clienteId}` — listar do cliente com filtros
  - `PATCH /api/pedidos/{id}/avancar` — avançar estado
  - `PATCH /api/pedidos/{id}/cancelar` — cancelar
  - DTOs: `CriarPedidoRequest` (aceitar `List<{produtoId, quantity}>`, `moradaId ou novaModada`), `PedidoResponse`
- **C** `EntregaController`:
  - `GET /api/entregas/{id}` — detalhe
  - `GET /api/entregas/pedido/{pedidoId}` — entrega do pedido
  - `PATCH /api/entregas/{id}/iniciar` — iniciar
  - `PATCH /api/entregas/{id}/concluir` — concluir
  - DTOs: `EntregaResponse`
- ✅ `PagamentoController` — atualizado:
  - `POST /api/pedidos/{pedidoId}/pagamento` — registar pagamento
  - DTOs: `PagamentoRequest` (bandeira para MULTIBANCO, telemovel para MBWAY, troco para DINHEIRO)

**Web UI (Thymeleaf):**
- [ ] `WebClienteController`:
  - `GET /cliente/moradas` — listar moradas do cliente
  - `POST /cliente/moradas` — adicionar nova morada
  - `DELETE /cliente/moradas/{id}` — remover morada
- [ ] `WebPedidoController`:
  - `GET /pedidos/novo` — carrinho de pedido (multi-restaurante)
  - `POST /pedidos` — submeter pedido (POST para API REST)
  - `GET /pedidos` — listar pedidos do cliente
  - `GET /pedidos/{id}` — detalhe (estado, items, entrega, pagamento)
  - `POST /pedidos/{id}/cancelar` — cancelar
  - `GET /pedidos/{id}/estado` — histórico de estados
- [ ] `WebPagamentoController`:
  - `GET /pagamentos/novo?pedidoId=X` — formulário de pagamento
  - `POST /pagamentos` — submeter pagamento (POST para API REST)
- [ ] Templates:
  - `pedidos/novo.html` — carrinho (buscar produtos, adicionar ao pedido, escolher morada)
  - `pedidos/lista.html` — lista de pedidos
  - `pedidos/detalhe.html` — detalhe (status, items, entrega, pagamento, cancelar)
  - `pagamentos/form.html` — formulário de pagamento (tipo select, campos dinâmicos por tipo)
  - `cliente/moradas.html` — gestão de moradas

---

### Semana 3: gRPC + Testes

**gRPC:**
- [ ] Adicionar ao `.proto`:
  ```protobuf
  rpc CriarPedido (CriarPedidoRequest) returns (PedidoResponse);
  rpc ListarPedidos (ListarPedidosRequest) returns (ListarPedidosResponse);
  rpc AvancarEstadoPedido (AvancarEstadoPedidoRequest) returns (PedidoResponse);
  rpc CancelarPedido (CancelarPedidoRequest) returns (Empty);
  rpc RegistarPagamento (RegistarPagamentoRequest) returns (PagamentoResponse);
  rpc ObterEntrega (ObterEntregaRequest) returns (EntregaResponse);
  ```
- [ ] Implementar no `TascaEatsGrpcService`

**Testes:**
- **C** Unit tests PedidoService (multi-restaurante, validações)
- ✅ Unit tests EntregaService (atribuição automática)
- ✅ Unit tests PagamentoService (novos campos)
- [ ] Integration tests controllers
- [ ] Testes gRPC

**Video:**
- [ ] Demonstrar: criar pedido multi-restaurante (produtos de diferentes restaurantes) → escolher morada → pagamento → atribuição automática entregador → cancelar pedido (ou entregar)

---

**Integração final (Semana 3):**
- Todos colaboram em `.proto` (adicionar serviços em paralelo)
- Todos implementam `TascaEatsGrpcService` (cada um o seu serviço, depois merge)
- Todos testam Docker final
