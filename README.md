# TascaEats — Delivery de Comida

> Projeto Prático #1 — Construção de Sistemas de Software (CSS) 2025/2026
> Entrega: **10/04/2026** | Tag: `fase1`

## Equipa

| Nome | Número | Email |
|------|--------|-------|
| TODO | TODO   | TODO  |

## Testes Unitários
Para correr os testes unitários, na pasta raiz do projeto:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
& "$env:ComSpec" /c "mvnw.cmd test"
```

Para correr os testes com verificação de cobertura (≥80%):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
& "$env:ComSpec" /c "mvnw.cmd verify"
```

## Vídeos Demonstrativos

- [ ] Link para vídeo(s): TODO

---

## 1. Modelo de Domínio

### 1.1 Entidades e Atributos

#### User (classe base — herança JPA)
- `id` (Long, PK, gerado)
- `nome` (String, não nulo)
- `email` (String, único, não nulo)
- `password` (String, não nulo — mock auth)
- `telemovel` (String)
- `type` (UserTypes: CLIENTE, ADMIN, ENTREGADOR) — discriminador
- `registoData` (LocalDateTime)

**Estratégia de herança: JOINED** (cada subtipo tem a sua tabela, juntam-se por FK)

#### Cliente (extends User)
- `morada` (String — por agora só uma morada, futuramente várias)
- `pedidos` → relação 1:N com Pedido

#### Admin (extends User)
- `restaurantes` → relação 1:N com Restaurante

#### Entregador (extends User)
- `tipoVeiculo` (String — ex: moto, carro, bicicleta)
- `disponivel` (boolean — se está disponível para entregas)
- `entregas` → relação 1:N com Entrega

#### Restaurante
- `id` (Long, PK)
- `nome` (String, não nulo)
- `nif` (String, único, não nulo)
- `morada` (String, não nulo)
- `cidade` (String, não nulo — para buscas)
- `aberto` (boolean — aberto/fechado para pedidos)
- `admin` → relação N:1 com Admin
- `produtos` → relação 1:N com Produto

#### Produto
- `id` (Long, PK)
- `nome` (String, não nulo)
- `descrição` (String)
- `price` (Double, não nulo)
- `disponivel` (boolean — disponível/esgotado)
- `eliminado` (boolean — soft-delete, default false)
- `restaurante` → relação N:1 com Restaurante

#### Pedido
- `id` (Long, PK)
- `dataHora` (LocalDateTime)
- `precoTotal` (Double)
- `endereçoEntrega` (String)
- `status` (PedidoStatus: CREATED, PAID, PREPARING, READY, IN_DELIVERY, DELIVERED, CANCELLED)
- `cliente` → relação N:1 com Cliente
- `restaurante` → relação N:1 com Restaurante
- `produtosPedido` → relação 1:N com ProdutoPedido
- `pagamento` → relação 1:1 com Pagamento
- `entrega` → relação 1:1 com Entrega
- `version` (Long — optimistic locking para concorrência)

#### ProdutoPedido
- `id` (Long, PK)
- `quantity` (int)
- `precoCompra` (Double — preço no momento da compra)
- `pedido` → relação N:1 com Pedido
- `produto` → relação N:1 com Produto

#### Pagamento (herança JPA)
- `id` (Long, PK)
- `preco` (Double)
- `dataPagamento` (LocalDateTime)
- `status` (PagamentoStatus: PENDING, COMPLETED, FAILED)
- `pedido` → relação 1:1 com Pedido

**Estratégia de herança: SINGLE_TABLE** (poucos campos diferenciadores)

Subtipos:
- **Multibanco**: `referencia` (String)
- **MBWay**: `telemovel` (String)
- **Dinheiro**: sem campos extra

#### Entrega
- `id` (Long, PK)
- `horaSaída` (LocalDateTime — hora de retirada no restaurante)
- `horaChegada` (LocalDateTime — hora de entrega ao cliente)
- `entregador` → relação N:1 com Entregador
- `pedido` → relação 1:1 com Pedido

### 1.2 Diagrama de Relações (resumo)

```
User (JOINED)
  ├── Cliente ──(1:N)──> Pedido
  ├── Admin ──(1:N)──> Restaurante
  └── Entregador ──(1:N)──> Entrega

Restaurante ──(1:N)──> Produto
Pedido ──(1:N)──> ProdutoPedido ──(N:1)──> Produto
Pedido ──(1:1)──> Pagamento (SINGLE_TABLE)
Pedido ──(1:1)──> Entrega
```

---

## 2. Casos de Uso — Endpoints REST

### A. Login (Mock Auth)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Login (aceita qualquer password se user existir) |

### B. Registo de Utilizadores
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users/clientes`   | Registar cliente |
| POST | `/api/users/admins`    | Registar administrador |
| POST | `/api/users/entregadores` | Registar entregador |

### C. Verificar, Remover e Atualizar Utilizadores
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET   | `/api/users/{id}`     | Ver utilizador por ID |
| GET   | `/api/users`          | Listar utilizadores |
| PUT   | `/api/users/{id}`     | Atualizar utilizador |
| DELETE | `/api/users/{id}`    | Remover utilizador |

### D. Buscar Restaurantes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/restaurantes?nome=X` | Buscar por nome |
| GET | `/api/restaurantes?cidade=X` | Buscar por cidade |

### E. Criar Restaurante e Gerir Estado
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/restaurantes`         | Criar restaurante (admin) |
| PATCH | `/api/restaurantes/{id}/aberto`  | Abrir restaurante |
| PATCH | `/api/restaurantes/{id}/fechado` | Fechar restaurante |

### F. CRUD de Produtos no Cardápio
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/restaurantes/{id}/produtos` | Adicionar produto |
| PUT | `/api/restaurantes/{id}/produtos/{pid}` | Atualizar produto |
| DELETE | `/api/restaurantes/{id}/produtos/{pid}` | Remover produto (soft-delete) |

### G. Buscar Produtos de um Restaurante
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/restaurantes/{id}/produtos` | Listar produtos (ativos) |

### H. Criação de Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/pedidos` | Criar pedido (com items) |

### I. Pagamento
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/pedidos/{id}/pagamento` | Registar e processar pagamento |

### J. Atualização de Estado do Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| PATCH | `/api/pedidos/{id}/status` | Atualizar estado do pedido |

### K. Atribuição de Entregador
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/pedidos/{id}/entregar` | Atribuir entregador ao pedido |

### L. Cancelamento de Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| PATCH | `/api/pedidos/{id}/cancelar` | Cancelar pedido |

---

## 3. Regras de Negócio

### Utilizadores
- Email deve ser único
- Um utilizador tem apenas um papel (CLIENT / ADMIN / DELIVERY_PERSON)
- Não se pode remover um utilizador com pedidos/entregas associados (ou soft-delete)

### Restaurantes
- Apenas administradores podem criar/gerir restaurantes
- NIF deve ser único
- Apenas o admin dono do restaurante pode editá-lo

### Produtos
- Produto com pedidos associados → **soft-delete** (campo `eliminado=true`)
- Produto sem pedidos pode ser removido fisicamente
- Preço deve ser > 0

### Pedidos
- ❌ Não é possível criar pedido a restaurante **fechado**
- ❌ Não é possível adicionar produto **esgotado** ao pedido
- ❌ Não é possível cancelar pedido se estado ≥ PREPARING
- Estado segue fluxo: CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED
- Pedido cancelável apenas em: CREATED ou PAID
- Valor total = soma(precoCompra × quantity) de todos os items
- `precoCompra` captura o preço no momento (não muda se produto for atualizado depois)

### Pagamento
- Obrigatório para avançar o pedido para PAID
- Um pedido só pode ter um pagamento
- Tipos: MULTIBANCO, MBWAY, CASH

### Entregas
- ❌ Não é possível atribuir entregador **indisponível** (disponivel=false)
- ❌ Entregador não pode ter duas entregas ativas ao mesmo tempo
- Pedido deve estar em estado READY para atribuir entregador
- Ao atribuir: entregador fica `disponivel=false`, pedido passa a IN_DELIVERY
- Ao concluir: entregador volta a `disponivel=true`, pedido passa a DELIVERED

### Concorrência
- Optimistic locking via `@Version` no Pedido
- Prevenir race conditions na atribuição de entregadores

---

## 4. Arquitetura em Camadas

```
┌─────────────────────────────────────────┐
│          Controllers (REST API)         │  ← @RestController, DTOs de entrada/saída
├─────────────────────────────────────────┤
│          Services (Negócio)             │  ← @Service, @Transactional, regras de negócio
├─────────────────────────────────────────┤
│          Repositories (Dados)           │  ← @Repository, Spring Data JPA
├─────────────────────────────────────────┤
│          Entities (Domínio JPA)         │  ← @Entity, domain model rico
├─────────────────────────────────────────┤
│          PostgreSQL (Docker)            │
└─────────────────────────────────────────┘
```

### Estrutura de packages
```
pt.ul.fc.css.tascaeats/
├── config/           → OpenApiConfig, etc.
├── entity/           → Entidades JPA (User, Client, Restaurant, Order, ...)
├── repository/       → Interfaces Spring Data JPA
├── service/          → Lógica de negócio
├── controller/       → REST Controllers
├── dto/              → DTOs de request/response
└── exception/        → Exceções custom (ResourceNotFoundException, BusinessRuleException, ...)
```

---

## 5. Plano de Implementação

### Fase A — Modelo de Domínio (Entidades JPA)
- [ ] Classe base `User` + herança JOINED (Cliente, Admin, Entregador)
- [ ] Entidade `Restaurante`
- [ ] Entidade `Produto` (com soft-delete, campo `eliminado`)
- [ ] Entidade `Pedido` + `ProdutoPedido` (com @Version)
- [ ] Entidade `Pagamento` + herança SINGLE_TABLE (Multibanco, MBWay, Dinheiro)
- [ ] Entidade `Entrega`
- [ ] Validar schema gerado pelo Hibernate

### Fase B — Repositórios
- [ ] UserRepository, ClienteRepository, AdminRepository, EntregadorRepository
- [ ] RestauranteRepository (com queries por nome e cidade)
- [ ] ProdutoRepository
- [ ] PedidoRepository
- [ ] PagamentoRepository
- [ ] EntregaRepository

### Fase C — Serviços + Regras de Negócio
- [ ] AuthService (mock login)
- [ ] UserService (CRUD + validações)
- [ ] RestauranteService (criar, abrir/fechar, buscar)
- [ ] ProdutoService (CRUD + soft-delete)
- [ ] PedidoService (criar, atualizar estado, cancelar)
- [ ] PagamentoService (registar e processar)
- [ ] EntregaService (atribuir entregador)

### Fase D — DTOs
- [ ] Request/Response DTOs para cada endpoint
- [ ] Conversão Entity ↔ DTO nos serviços ou controllers

### Fase E — Controllers REST
- [ ] AuthController
- [ ] UserController
- [ ] RestauranteController
- [ ] ProdutoController (nested em restaurant)
- [ ] PedidoController
- [ ] PagamentoController
- [ ] EntregaController

### Fase F — Tratamento de Erros
- [ ] GlobalExceptionHandler (@ControllerAdvice)
- [ ] Exceções: ResourceNotFoundException, BusinessRuleException, etc.
- [ ] Status codes adequados (201, 400, 404, 409, 422)

### Fase G — Testes
- [ ] Testes unitários dos serviços (regras de negócio)
- [ ] Testes de integração dos controllers (MockMvc)
- [ ] Cobertura mínima: 80%
- [ ] Cenários de erro e exceções cobertos

### Fase H — Docker + Finalização
- [ ] Testar `docker-compose up` completo
- [ ] Testar todos os endpoints via Swagger
- [ ] Gravar vídeo(s) demonstrativo(s)
- [ ] Atualizar README com links dos vídeos
- [ ] Tag `fase1`

---

## 6. Queries de Negócio (validação do modelo)

O modelo deve permitir responder a:
1. Restaurantes com maior volume de vendas (€)
2. Morada do restaurante com mais vendas / cliente com mais pedidos
3. Média de pedidos por cliente por mês
4. Produtos mais vendidos da plataforma
5. Método de pagamento mais utilizado
6. Clientes registados sem compras