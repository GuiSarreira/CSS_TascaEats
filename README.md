# TascaEats — Delivery de Comida

> Projeto Prático #1 — Construção de Sistemas de Software (CSS) 2025/2026
> Entrega: **10/04/2026** | Tag: `fase1`

## Equipa

| Nome | Número | Email |
|------|--------|-------|
| TODO | TODO   | TODO  |

## Vídeos Demonstrativos

- [ ] Link para vídeo(s): TODO

---

## 1. Modelo de Domínio

### 1.1 Entidades e Atributos

#### Utilizador (classe base — herança JPA)
- `id` (Long, PK, gerado)
- `name` (String, não nulo)
- `email` (String, único, não nulo)
- `password` (String, não nulo — mock auth)
- `phone` (String)
- `role` (enum: CLIENT, ADMIN, DELIVERY_PERSON) — discriminador
- `registrationDate` (LocalDateTime)

**Estratégia de herança: JOINED** (cada subtipo tem a sua tabela, juntam-se por FK)

#### Cliente (extends Utilizador)
- `address` (String — por agora só uma morada, futuramente várias)
- `orders` → relação 1:N com Pedido

#### Administrador (extends Utilizador)
- `restaurants` → relação 1:N com Restaurante

#### Entregador (extends Utilizador)
- `vehicleType` (String — ex: moto, carro, bicicleta)
- `available` (boolean — se está disponível para entregas)
- `deliveries` → relação 1:N com Entrega

#### Restaurante
- `id` (Long, PK)
- `name` (String, não nulo)
- `nif` (String, único, não nulo)
- `address` (String, não nulo)
- `city` (String, não nulo — para buscas)
- `open` (boolean — aberto/fechado para pedidos)
- `admin` → relação N:1 com Administrador
- `products` → relação 1:N com Produto

#### Produto
- `id` (Long, PK)
- `name` (String, não nulo)
- `description` (String)
- `price` (BigDecimal, não nulo)
- `available` (boolean — disponível/esgotado)
- `deleted` (boolean — soft-delete, default false)
- `restaurant` → relação N:1 com Restaurante

#### Pedido (Order)
- `id` (Long, PK)
- `createdAt` (LocalDateTime)
- `totalPrice` (BigDecimal)
- `deliveryAddress` (String)
- `status` (enum: CREATED, PAID, PREPARING, READY, IN_DELIVERY, DELIVERED, CANCELLED)
- `client` → relação N:1 com Cliente
- `restaurant` → relação N:1 com Restaurante
- `items` → relação 1:N com ItemPedido
- `payment` → relação 1:1 com Pagamento
- `delivery` → relação 1:1 com Entrega
- `version` (Long — optimistic locking para concorrência)

#### ItemPedido (OrderItem)
- `id` (Long, PK)
- `quantity` (int)
- `priceAtPurchase` (BigDecimal — preço no momento da compra)
- `order` → relação N:1 com Pedido
- `product` → relação N:1 com Produto

#### Pagamento (Payment — herança JPA)
- `id` (Long, PK)
- `amount` (BigDecimal)
- `paymentDate` (LocalDateTime)
- `status` (enum: PENDING, COMPLETED, FAILED)
- `order` → relação 1:1 com Pedido

**Estratégia de herança: SINGLE_TABLE** (poucos campos diferenciadores)

Subtipos:
- **MultibancoPagamento**: `entityReference` (String)
- **MBWayPagamento**: `phoneNumber` (String)
- **DinheiroPagamento**: sem campos extra

#### Entrega (Delivery)
- `id` (Long, PK)
- `pickupTime` (LocalDateTime — hora de retirada no restaurante)
- `deliveryTime` (LocalDateTime — hora de entrega ao cliente)
- `deliveryPerson` → relação N:1 com Entregador
- `order` → relação 1:1 com Pedido

### 1.2 Diagrama de Relações (resumo)

```
Utilizador (JOINED)
  ├── Cliente ──(1:N)──> Pedido
  ├── Administrador ──(1:N)──> Restaurante
  └── Entregador ──(1:N)──> Entrega

Restaurante ──(1:N)──> Produto
Pedido ──(1:N)──> ItemPedido ──(N:1)──> Produto
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
| POST | `/api/users/clients` | Registar cliente |
| POST | `/api/users/admins` | Registar administrador |
| POST | `/api/users/delivery-persons` | Registar entregador |

### C. Verificar, Remover e Atualizar Utilizadores
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/users/{id}` | Ver utilizador por ID |
| GET | `/api/users` | Listar utilizadores |
| PUT | `/api/users/{id}` | Atualizar utilizador |
| DELETE | `/api/users/{id}` | Remover utilizador |

### D. Buscar Restaurantes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/restaurants?name=X` | Buscar por nome |
| GET | `/api/restaurants?city=X` | Buscar por cidade |

### E. Criar Restaurante e Gerir Estado
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/restaurants` | Criar restaurante (admin) |
| PATCH | `/api/restaurants/{id}/open` | Abrir restaurante |
| PATCH | `/api/restaurants/{id}/close` | Fechar restaurante |

### F. CRUD de Produtos no Cardápio
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/restaurants/{id}/products` | Adicionar produto |
| PUT | `/api/restaurants/{id}/products/{pid}` | Atualizar produto |
| DELETE | `/api/restaurants/{id}/products/{pid}` | Remover produto (soft-delete) |

### G. Buscar Produtos de um Restaurante
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/restaurants/{id}/products` | Listar produtos (ativos) |

### H. Criação de Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/orders` | Criar pedido (com items) |

### I. Pagamento
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/orders/{id}/payment` | Registar e processar pagamento |

### J. Atualização de Estado do Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| PATCH | `/api/orders/{id}/status` | Atualizar estado do pedido |

### K. Atribuição de Entregador
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/orders/{id}/delivery` | Atribuir entregador ao pedido |

### L. Cancelamento de Pedido
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| PATCH | `/api/orders/{id}/cancel` | Cancelar pedido |

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
- Produto com pedidos associados → **soft-delete** (campo `deleted=true`)
- Produto sem pedidos pode ser removido fisicamente
- Preço deve ser > 0

### Pedidos
- ❌ Não é possível criar pedido a restaurante **fechado**
- ❌ Não é possível adicionar produto **esgotado** ao pedido
- ❌ Não é possível cancelar pedido se estado ≥ PREPARING
- Estado segue fluxo: CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED
- Pedido cancelável apenas em: CREATED ou PAID
- Valor total = soma(priceAtPurchase × quantity) de todos os items
- `priceAtPurchase` captura o preço no momento (não muda se produto for atualizado depois)

### Pagamento
- Obrigatório para avançar o pedido para PAID
- Um pedido só pode ter um pagamento
- Tipos: MULTIBANCO, MBWAY, CASH

### Entregas
- ❌ Não é possível atribuir entregador **indisponível** (available=false)
- ❌ Entregador não pode ter duas entregas ativas ao mesmo tempo
- Pedido deve estar em estado READY para atribuir entregador
- Ao atribuir: entregador fica `available=false`, pedido passa a IN_DELIVERY
- Ao concluir: entregador volta a `available=true`, pedido passa a DELIVERED

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
- [ ] Classe base `User` + herança JOINED (Client, Admin, DeliveryPerson)
- [ ] Entidade `Restaurant`
- [ ] Entidade `Product` (com soft-delete)
- [ ] Entidade `Order` + `OrderItem` (com @Version)
- [ ] Entidade `Payment` + herança SINGLE_TABLE
- [ ] Entidade `Delivery`
- [ ] Validar schema gerado pelo Hibernate

### Fase B — Repositórios
- [ ] UserRepository, ClientRepository, AdminRepository, DeliveryPersonRepository
- [ ] RestaurantRepository (com queries por nome e cidade)
- [ ] ProductRepository
- [ ] OrderRepository
- [ ] PaymentRepository
- [ ] DeliveryRepository

### Fase C — Serviços + Regras de Negócio
- [ ] AuthService (mock login)
- [ ] UserService (CRUD + validações)
- [ ] RestaurantService (criar, abrir/fechar, buscar)
- [ ] ProductService (CRUD + soft-delete)
- [ ] OrderService (criar, atualizar estado, cancelar)
- [ ] PaymentService (registar e processar)
- [ ] DeliveryService (atribuir entregador)

### Fase D — DTOs
- [ ] Request/Response DTOs para cada endpoint
- [ ] Conversão Entity ↔ DTO nos serviços ou controllers

### Fase E — Controllers REST
- [ ] AuthController
- [ ] UserController
- [ ] RestaurantController
- [ ] ProductController (nested em restaurant)
- [ ] OrderController
- [ ] PaymentController
- [ ] DeliveryController

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