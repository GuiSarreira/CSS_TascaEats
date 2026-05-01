# Queries de Negócio (Fase 1) — Arquitetura

## Resumo

As **6 queries de negócio (Fase 1)** foram reorganizadas e distribuídas nos **controllers especializados**, tanto em:
- **REST API** (`/api/negocio/fase1/*`) — para consumo por aplicações client
- **Web Controllers** (Thymeleaf) — para visualização em páginas HTML

> **Nota:** O `NegocioController` continua com as 5 queries de Fase 2 (REST API) para análises avançadas.

---

## Mapeamento de Endpoints

### 1️⃣ **Restaurantes com Maior Volume de Vendas (€)**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/restaurantes/volume-vendas` | `NegocioController` | — |
| Web | `GET /restaurantes/volume-vendas` | `WebRestauranteController` | `restaurantes/volume-vendas.html` |

**Query:** Calcula o montante total (soma de preços × quantidade) para cada restaurante em pedidos DELIVERED

---

### 2️⃣ **Morada do Restaurante com Mais Pedidos**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/restaurantes/mais-pedidos` | `NegocioController` | — |
| Web | `GET /restaurantes/mais-pedidos` | `WebRestauranteController` | `restaurantes/mais-pedidos.html` |

**Query:** Retorna o restaurante com maior número de pedidos completados + sua morada

---

### 3️⃣ **Média de Pedidos por Cliente por Mês**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/pedidos/media-por-cliente-mes` | `NegocioController` | — |
| Web | `GET /pedidos/media-por-cliente-mes` | `WebPedidoController` | `pedidos/media-por-cliente-mes.html` |

**Query:** Agrupa pedidos por cliente, ano e mês, contando quantos por período

---

### 4️⃣ **Produtos Mais Vendidos da Plataforma**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/produtos/mais-vendidos` | `NegocioController` | — |
| Web | `GET /produtos/mais-vendidos` | `WebProdutoController` | `produtos/mais-vendidos.html` |

**Query:** Calcula a quantidade total vendida de cada produto (em pedidos DELIVERED)

---

### 5️⃣ **Método de Pagamento Mais Utilizado**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/pagamentos/metodo-mais-utilizado` | `NegocioController` | — |
| Web | `GET /pagamentos/metodo-mais-utilizado` | `WebPagamentoController` | `pagamentos/metodo-mais-utilizado.html` |

**Query:** Conta quantas vezes cada método de pagamento (MBWAY, MULTIBANCO, DINHEIRO) foi utilizado

---

### 6️⃣ **Clientes Registados Sem Compras**

| Tipo | Endpoint | Controller | Template |
|------|----------|-----------|----------|
| REST | `GET /api/negocio/fase1/clientes/sem-compras` | `NegocioController` | — |
| Web | `GET /cliente/sem-compras` | `WebClienteController` | `cliente/sem-compras.html` |

**Query:** Lista todos os clientes que não realizaram qualquer pedido

---

## Benefícios da Reorganização

### ✅ **Cohesão temática**
- Queries de restaurantes → `WebRestauranteController`
- Queries de produtos → `WebProdutoController`
- Queries de pagamentos → `WebPagamentoController`
- Queries de clientes → `WebClienteController`
- Queries de pedidos → `WebPedidoController`

### ✅ **Sem duplicação**
- Método único no serviço (`ProdutoService`, `RestauranteService`, etc.)
- Reutilizado tanto em REST como Web

### ✅ **Fácil manutenção**
- Cada controller trata seu domínio
- Sem `NegocioController` ou `WebNegocioController` centralizados

### ✅ **Escalabilidade**
- Adicionar queries novas é simples: basta adicionar um endpoint no controller correspondente

---

## Estrutura de Ficheiros

```
src/main/java/pt/ul/fc/css/tascaeats/
├── controllers/
│   └── NegocioController.java          (REST API — Fase 1 + Fase 2)
├── web/
│   ├── WebRestauranteController.java   (Web — Query 1 + Query 2)
│   ├── WebProdutoController.java       (Web — Query 4)
│   ├── WebPagamentoController.java     (Web — Query 5)
│   ├── WebClienteController.java       (Web — Query 6)
│   └── WebPedidoController.java        (Web — Query 3)
└── services/
    ├── RestauranteService.java         (Query 1, 2)
    ├── ProdutoService.java             (Query 4)
    ├── PagamentoService.java           (Query 5)
    ├── PedidoService.java              (Query 3)
    └── UserService.java                (Query 6)
```

---

## Como Usar

### **REST API (JSON)**
```bash
# Exemplo 1: Volume de vendas
curl http://localhost:8080/api/negocio/fase1/restaurantes/volume-vendas

# Exemplo 2: Produtos mais vendidos
curl http://localhost:8080/api/negocio/fase1/produtos/mais-vendidos

# Exemplo 3: Clientes sem compras
curl http://localhost:8080/api/negocio/fase1/clientes/sem-compras
```

### **Web (HTML/Thymeleaf)**
```
http://localhost:8080/restaurantes/volume-vendas
http://localhost:8080/produtos/mais-vendidos
http://localhost:8080/cliente/sem-compras
http://localhost:8080/pagamentos/metodo-mais-utilizado
http://localhost:8080/pedidos/media-por-cliente-mes
```

---

## Status

| Query | REST | Web | Testes |
|-------|------|-----|--------|
| 1 — Volume de Vendas | ✅ | ✅ | ⏳ |
| 2 — Mais Pedidos | ✅ | ✅ | ⏳ |
| 3 — Média Mensal | ✅ | ✅ | ⏳ |
| 4 — Produtos Top | ✅ | ✅ | ✅ |
| 5 — Pagamento Top | ✅ | ✅ | ⏳ |
| 6 — Sem Compras | ✅ | ✅ | ⏳ |

---

## Próximos Passos

- [ ] Criar templates Thymeleaf para cada query
- [ ] Adicionar gráficos/visualizações nos templates
- [ ] Criar testes unitários e de integração
- [ ] Documentar formato de resposta da API
