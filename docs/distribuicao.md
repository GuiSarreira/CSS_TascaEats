### Estratégia: Divisão por Feature (Backend + Frontend + gRPC)

Cada um trabalha numa **feature completa**: entidade → service → repository → controller REST → web UI → gRPC.

**Semana 1**: Todos fazem modelo + repositórios + serviços em paralelo
**Semana 2**: Todos fazem controllers REST + web UI em paralelo
**Semana 3**: Todos fazem gRPC + testes + video + finalização

**Dependência mínima**: Só em Fase I (Docker final).

---

## Ze — Feature: Avaliações + Autenticação + Base (Auth User gRPC)

### Semana 1: Modelo + Serviço + Repositório

**Modelo de Domínio:**
- [ ] Criar entidade `Avaliacao`:
  - `id (Long, PK)`, `nota (int, 1-5)`, `comentario (String)`, `dataAvaliacao (LocalDateTime)`
  - `cliente → @ManyToOne com Cliente`, `restaurante → @ManyToOne com Restaurante`
  - `pedido → @OneToOne com Pedido` (garante que só avalia após compra)
  - Validações: nota entre 1-5, só um cliente pode avaliar um restaurante uma vez

**Repositório:**
- [ ] `AvaliacaoRepository`:
  - `findByClienteIdAndRestauranteId(clienteId, restauranteId): Optional<Avaliacao>`
  - `findByRestauranteId(restauranteId): List<Avaliacao>`
  - `findByClienteId(clienteId): List<Avaliacao>`
  - `findByPedidoId(pedidoId): Optional<Avaliacao>`

**Service:**
- [ ] `AvaliacaoService`:
  - `criarAvaliacao(clienteId, restauranteId, pedidoId, nota, comentario)` — validar que pedido é do cliente, status DELIVERED, restaurante do pedido
  - `obterAvaliacoesPorRestaurante(restauranteId)` — retorna lista com media de notas
  - `atualizarAvaliacao(avaliacaoId, nota, comentario)` — só pelo cliente criador
  - `removerAvaliacao(avaliacaoId)` — só pelo cliente criador
  - `mediaNotasRestaurante(restauranteId): Double`

**Testes:**
- [ ] Testes de validação de avaliação (nota range, pedido DELIVERED, etc.)

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- [ ] `AvaliacaoController`:
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
- [ ] Criar entidade `Menu`:
  - `id (Long, PK)`, `nome (String, not null)`, `descricao (String)`
  - `@ManyToMany com Restaurante` (N:N via tabela `menu_restaurante`)
  - `@ManyToMany com Produto` (N:N via tabela `menu_produto`) — lado inverso
- [ ] Atualizar `Restaurante`: adicionar `@ManyToMany(mappedBy="restaurantes") menus`
- [ ] Atualizar `Produto`: adicionar `@ManyToMany(mappedBy="produtos") menus`
- [ ] Adicionar campos ao `Restaurante`:
  - `tipoCozinha (String)` — italiana, chinesa, portuguesa, etc.
  - `horarioAbertura (LocalTime)`
  - `horarioFecho (LocalTime)`

**Repositório + Specifications:**
- [ ] `MenuRepository`:
  - `findByNomeContainingIgnoreCase(nome): List<Menu>`
  - `findByRestaurantesContaining(restaurante): List<Menu>`
  - Custom método: `findMenusWithFilters(nome, minProdutos, maxProdutos, minPreco, maxPreco): List<Menu>`
- [ ] `RestauranteRepository` — adicionar custom methods para filtros:
  - `findRestaurantesWithFilters(nome, tipoCozinha, horarioAbertura, horarioFecho, minPreco, maxPreco): List<Restaurante>`

**Service:**
- [ ] `MenuService`:
  - `criarMenu(nome, descricao): Menu`
  - `atualizarMenu(menuId, nome, descricao): Menu`
  - `removerMenu(menuId): void`
  - `associarMenuRestaurante(menuId, restauranteId): void` — criar relação N:N
  - `removerMenuRestaurante(menuId, restauranteId): void`
  - `adicionarProdutoMenu(menuId, produtoId): void`
  - `removerProdutoMenu(menuId, produtoId): void`
  - `listarMenusComFiltros(filtros): List<Menu>`
- [ ] `RestauranteService` — adicionar métodos de filtro:
  - `listarRestaurantesComFiltros(nome, tipoCozinha, horario, precoMin, precoMax, nAvaliações): List<Restaurante>`

**Testes:**
- [ ] Tests de Menu (CRUD, relações N:N)
- [ ] Tests de Specifications para filtros

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- [ ] `MenuController`:
  - `POST /api/menus` — criar menu
  - `PUT /api/menus/{id}` — atualizar menu
  - `DELETE /api/menus/{id}` — remover menu
  - `GET /api/menus?...filtros` — listar com filtros
  - `POST /api/menus/{menuId}/restaurantes/{restauranteId}` — associar
  - `DELETE /api/menus/{menuId}/restaurantes/{restauranteId}` — desassociar
  - `POST /api/menus/{menuId}/produtos/{produtoId}` — adicionar produto
  - `DELETE /api/menus/{menuId}/produtos/{produtoId}` — remover produto
  - DTOs: `MenuRequest`, `MenuResponse`
- [ ] Atualizar `RestauranteController`:
  - `GET /api/restaurantes?...filtros` (nome, tipoCozinha, horario, preço, nAvaliações)
  - DTOs atualizar com novos campos

**Web UI (Thymeleaf):**
- [ ] `WebMenuController`:
  - `GET /menus` — listar com filtros
  - `GET /menus/novo` — formulário novo menu
  - `POST /menus` — submeter novo menu
  - `GET /menus/{id}/editar` — formulário edição
  - `POST /menus/{id}` — submeter edição
  - `GET /menus/{id}` — detalhe (produtos, restaurantes)
  - `POST /menus/{id}/restaurantes/{rid}` — associar restaurante
- [ ] `WebRestauranteController`:
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
- [ ] Unit tests MenuService
- [ ] Integration tests MenuController
- [ ] Testes gRPC

**Video:**
- [ ] Demonstrar: criar menu → associar a restaurante(s) → modificar produto no menu → reflete em todos restaurantes

---

## Eu — Feature: Pedidos Multi-Restaurante + Entrega Automática + Pagamentos

### Semana 1: Modelo + Serviço + Repositório

**Modelo de Domínio:**
- [ ] Atualizar `Cliente`:
  - Substituir `morada (Endereco, @Embedded)` por `@ElementCollection<Endereco> moradas`
  - JPA gera tabela `cliente_moradas` automaticamente
- [ ] Atualizar `Pedido`:
  - **Remover** `@ManyToOne restaurante` — agora é multi-restaurante
  - Manter `enderecoEntrega` (pode ser de morada existente ou nova)
  - Adicionar validação: ao criar, aceitar `morada` ou `moradaId` (se do cliente)
- [ ] Atualizar `Multibanco`: adicionar `bandeira (String)`
- [ ] Atualizar `Dinheiro`: adicionar `troco (Double)`
- [ ] Atualizar `Entrega`:
  - `horaRetirada (LocalDateTime)` — já está, mas deve ser preenchida em `iniciarEntrega()` (já foi feito na Fase 1)
  - Validar: atribuição automática só se `entregador.disponivel == true`

**Repositório:**
- [ ] `ClienteRepository` — já existe, apenas adicionar métodos para moradas (se necessário)
- [ ] `PedidoRepository`:
  - `findByClienteIdAndStatus(clienteId, status): List<Pedido>`
  - `findPedidosComFiltros(clienteId, status, dataMin, dataMax): List<Pedido>`
  - `findPedidosReadyWithoutEntrega(): List<Pedido>` — para atribuição automática
- [ ] `EntregaRepository`:
  - `findByStatusAndEntregadorDisponivel(status, true): List<Entrega>`
  - `findEntregasComFiltros(restauranteId, entregadorId): List<Entrega>`

**Service:**
- [ ] `PedidoService` — atualizar completamente:
  - `criarPedido(clienteId, List<{produtoId, quantity}>, moradaId ou novaModada)`:
    - Validar que cliente existe
    - Validar que morada é do cliente ou está no request
    - Criar pedido **sem restaurante** (restaurante inferido via produto)
    - Calcular preço total
    - Validar que restaurantes estão abertos
  - `avancarEstado(pedidoId, novoStatus)` — transições de estado (CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED)
  - Após transição para READY: chamar `EntregaService.atribuirEntregadorAutomatico(pedido)`
  - `cancelarPedido(pedidoId)` — se CREATED ou PAID
- [ ] `EntregaService` — criar novo ou atualizar:
  - `atribuirEntregadorAutomatico(pedido): Entrega`
    - Buscar entregador com `disponivel=true`
    - Se encontrar: criar Entrega, marcar entregador como indisponível, retornar
    - Se não encontrar: deixar pedido sem entrega (aguarda)
    - Validar `zonaAtuacao` do entregador
  - `iniciarEntrega(entregaId)` — preenchida em Fase 1
  - `concluirEntrega(entregaId)` — preenchida em Fase 1
- [ ] `PagamentoService` — atualizar:
  - `registarPagamento(pedidoId, tipoPagamento, dados)`:
    - Se MULTIBANCO: accept `referencia`, `bandeira`
    - Se MBWAY: accept `telemovel`
    - Se DINHEIRO: accept `troco`
    - Validar dados, criar Pagamento
    - Atualizar status para COMPLETED (mock)

**Testes:**
- [ ] Tests de pedido multi-restaurante (validações, preço)
- [ ] Tests de atribuição automática entregador
- [ ] Tests de pagamento (novos campos)

---

### Semana 2: Controllers REST + Web UI

**Controller REST:**
- [ ] Atualizar `PedidoController`:
  - `POST /api/pedidos` — criar (aceitar items de restaurantes diferentes, morada)
  - `GET /api/pedidos/{id}` — detalhe
  - `GET /api/pedidos/cliente/{clienteId}` — listar do cliente com filtros
  - `PATCH /api/pedidos/{id}/avancar` — avançar estado
  - `PATCH /api/pedidos/{id}/cancelar` — cancelar
  - DTOs: `CriarPedidoRequest` (aceitar `List<{produtoId, quantity}>`, `moradaId ou novaModada`), `PedidoResponse`
- [ ] `EntregaController`:
  - `GET /api/entregas/{id}` — detalhe
  - `GET /api/entregas/pedido/{pedidoId}` — entrega do pedido
  - `PATCH /api/entregas/{id}/iniciar` — iniciar (entregador recolhe)
  - `PATCH /api/entregas/{id}/concluir` — concluir (entregador entrega)
  - DTOs: `EntregaResponse`
- [ ] `PagamentoController` — atualizar:
  - `POST /api/pedidos/{pedidoId}/pagamento` — registar pagamento (corpo com tipoPagamento, dados)
  - DTOs: `PagamentoRequest` (aceitar bandeira para MULTIBANCO, telemovel para MBWAY, troco para DINHEIRO)

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
- [ ] Unit tests PedidoService (multi-restaurante, validações)
- [ ] Unit tests EntregaService (atribuição automática)
- [ ] Unit tests PagamentoService (novos campos)
- [ ] Integration tests controllers
- [ ] Testes gRPC

**Video:**
- [ ] Demonstrar: criar pedido multi-restaurante (produtos de diferentes restaurantes) → escolher morada → pagamento → atribuição automática entregador → cancelar pedido (ou entregar)

---

**Integração final (Semana 3):**
- Todos colaboram em `.proto` (adicionar serviços em paralelo)
- Todos implementam `TascaEatsGrpcService` (cada um o seu serviço, depois merge)
- Todos testam Docker final
