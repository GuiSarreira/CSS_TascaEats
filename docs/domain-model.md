# TascaEats — Modelo de Domínio (Fase 2)

## Diagrama de Classes

```mermaid
classDiagram
    direction TB

    class User {
        <<Abstract>>
        Long id
        String nome
        String email
        String password
        LocalDateTime dataRegisto
        boolean ativo
    }

    class Endereco {
        <<Embeddable>>
        String rua
        String codigoPostal
        String cidade
    }

    class Cliente {
        List~Endereco~ moradas
    }

    class Admin {
    }

    class Entregador {
        String veiculo
        String zonaAtuacao
        boolean disponivel
    }

    class Restaurante {
        Long id
        String nome
        String nif
        Endereco morada
        boolean aberto
        String tipoCozinha
        LocalTime horarioAbertura
        LocalTime horarioFecho
    }

    class Menu {
        Long id
        String nome
        String descricao
    }

    class Produto {
        Long id
        String nome
        String descricao
        Double preco
        boolean disponivel
        boolean eliminado
        String categoria
    }

    class Pedido {
        Long id
        LocalDateTime dataHora
        Double precoTotal
        Endereco enderecoEntrega
        PedidoStatus status
        Long version
    }

    class ProdutoPedido {
        Long id
        int quantity
        Double precoCompra
    }

    class Pagamento {
        <<Abstract>>
        Long id
        Double preco
        LocalDateTime dataPagamento
        PagamentoStatus status
    }

    class Multibanco {
        String referencia
        String bandeira
    }

    class MBWay {
        String telemovel
    }

    class Dinheiro {
        Double troco
    }

    class Entrega {
        Long id
        LocalDateTime horaRetirada
        LocalDateTime horaEntrega
        EntregaStatus status
    }

    class Avaliacao {
        Long id
        int nota
        String comentario
        LocalDateTime dataAvaliacao
    }

    class PedidoStatus {
        <<Enumeration>>
        CREATED
        PAID
        PREPARING
        READY
        IN_DELIVERY
        DELIVERED
        CANCELLED
    }

    class PagamentoStatus {
        <<Enumeration>>
        PENDING
        COMPLETED
        FAILED
    }

    class EntregaStatus {
        <<Enumeration>>
        ATRIBUIDA
        A_CAMINHO
        CONCLUIDA
        CANCELADA
    }

    User <|-- Cliente : JOINED
    User <|-- Admin : JOINED
    User <|-- Entregador : JOINED

    Pagamento <|-- Multibanco : SINGLE_TABLE
    Pagamento <|-- MBWay : SINGLE_TABLE
    Pagamento <|-- Dinheiro : SINGLE_TABLE

    Cliente ..> Endereco : @ElementCollection moradas
    Cliente "1" --> "*" Pedido : faz
    Cliente "1" --> "*" Avaliacao : avalia
    Admin "1" --> "*" Restaurante : gere
    Entregador "1" --> "*" Entrega : faz

    Restaurante ..> Endereco : embedded
    Pedido ..> Endereco : embedded

    Restaurante "1" --> "*" Produto : tem
    Restaurante "*" --> "*" Menu : usa
    Restaurante "1" --> "*" Avaliacao : recebe

    Menu "*" --> "*" Produto : contem

    Pedido "1" --> "*" ProdutoPedido : contem
    Pedido "1" --> "0..1" Pagamento : pago por
    Pedido "1" --> "0..1" Entrega : entregue por
    Pedido "1" --> "0..1" Avaliacao : avaliado em

    ProdutoPedido "*" --> "1" Produto : referencia
    Entrega "*" --> "1" Entregador : atribuida a
```

## Fluxo de Estados do Pedido

```mermaid
stateDiagram-v2
    [*] --> CREATED : Cliente cria pedido
    CREATED --> PAID : Pagamento processado
    CREATED --> CANCELLED : Cliente cancela
    PAID --> PREPARING : Restaurante aceita
    PAID --> CANCELLED : Cliente cancela
    PREPARING --> READY : Pedido preparado
    READY --> IN_DELIVERY : Entregador atribuído (automático)
    IN_DELIVERY --> DELIVERED : Entrega concluída
    DELIVERED --> [*]
    CANCELLED --> [*]
```

## Fluxo de Estados da Entrega

```mermaid
stateDiagram-v2
    [*] --> ATRIBUIDA : Sistema atribui entregador
    ATRIBUIDA --> A_CAMINHO : Entregador recolhe pedido
    A_CAMINHO --> CONCLUIDA : Entregador entrega ao cliente
    ATRIBUIDA --> CANCELADA : Cancelamento
    A_CAMINHO --> CANCELADA : Cancelamento
    CONCLUIDA --> [*]
    CANCELADA --> [*]
```

## Estratégias de Herança JPA

| Hierarquia | Estratégia | Justificação |
|-----------|------------|----------|
| `User` → Cliente, Admin, Entregador | **JOINED** | Subtipos com atributos distintos; queries frequentes por subtipo; melhor normalização |
| `Pagamento` → Multibanco, MBWay, Dinheiro | **SINGLE_TABLE** | Poucos campos diferenciadores; queries polimórficas frequentes; melhor performance |
| `Endereco` em Restaurante, Pedido | **@Embedded** | Value object reutilizável; sem tabela própria; colunas ficam inline na tabela dona |
| `Endereco` em `Cliente.moradas` | **@ElementCollection** | Múltiplas moradas por cliente; JPA gera tabela `cliente_moradas` automaticamente; `Endereco` mantém-se `@Embeddable` sem necessidade de entidade própria |

## Relações Principais

| De | Para | Cardinalidade | Notas |
|----|------|--------------|-------|
| Cliente | Endereco (moradas) | @ElementCollection | Várias moradas por cliente; tabela `cliente_moradas` gerada pelo JPA |
| Cliente | Pedido | 1:N | Cliente faz vários pedidos |
| Cliente | Avaliacao | 1:N | Cliente faz várias avaliações |
| Admin | Restaurante | 1:N | Admin gere vários restaurantes |
| Entregador | Entrega | 1:N | Entregador faz várias entregas |
| Restaurante | Produto | 1:N | Restaurante tem vários produtos |
| Restaurante | Menu | N:N | Restaurante usa vários menus; menu partilhado por vários restaurantes |
| Restaurante | Avaliacao | 1:N | Restaurante recebe várias avaliações |
| Menu | Produto | N:N | Menu contém vários produtos; produto pode estar em vários menus |
| Pedido | ProdutoPedido | 1:N | Pedido contém vários items (possivelmente de restaurantes diferentes) |
| ProdutoPedido | Produto | N:1 | Item referencia um produto (restaurante inferido via produto) |
| Pedido | Pagamento | 1:1 | Pedido tem um pagamento |
| Pedido | Entrega | 1:1 | Pedido tem uma entrega |
| Pedido | Avaliacao | 1:1 | Pedido pode ter uma avaliação |

## Alterações face à Fase 1

| Alteração | Detalhe |
|-----------|---------|
| ~~`Pedido → Restaurante` (N:1)~~ | **Removida** — pedido pode conter produtos de vários restaurantes; restaurante inferido via `ProdutoPedido → Produto → Restaurante` |
| ~~`Cliente.morada` (Embedded)~~ | **Substituída** por `Cliente.moradas` (`@ElementCollection<Endereco>`) — múltiplas moradas; JPA cria tabela `cliente_moradas`; `Endereco` mantém-se `@Embeddable` |
| `Restaurante` + campos | `tipoCozinha`, `horarioAbertura` (LocalTime), `horarioFecho` (LocalTime) |
| `Produto` + campo | `categoria` (ENTRADA, PRATO_PRINCIPAL, SOBREMESA, BEBIDA, …) |
| `Multibanco` + campo | `bandeira` (bandeira do cartão — "Visa", "Mastercard", …) |
| `Dinheiro` + campo | `troco` (Double — valor do troco devolvido) |
| Nova entidade `Menu` | N:N com Produto e Restaurante — menus partilhados entre restaurantes (franchising) |
| Nova entidade `Avaliacao` | Cliente avalia restaurante após pedido concluído (nota 1–5, comentário) |
| Atribuição de entregador | Passa a ser **automática** — sistema atribui entregador disponível quando pedido fica READY |

## Regras de Integridade

- **Soft-delete** em `Produto`: se tem pedidos associados, marca `eliminado=true` em vez de apagar
- **Optimistic locking** em `Pedido`: campo `@Version` para controlo de concorrência
- **`precoCompra`** em `ProdutoPedido`: captura o preço no momento da compra (imutável)
- **`disponivel`** em `Entregador`: controla disponibilidade para novas entregas
- **`Endereco`** é um `@Embeddable`: usado em `Restaurante` (morada), `Pedido` (endereço de entrega) e `Cliente.moradas` (`@ElementCollection`) — sem tabela própria enquanto embedded; para o `@ElementCollection` o JPA gera automaticamente a tabela `cliente_moradas`
- **Pedido multi-restaurante**: um pedido pode conter `ProdutoPedido` de restaurantes diferentes; não existe relação direta `Pedido → Restaurante`
- **Avaliação**: um cliente só pode avaliar um restaurante se tiver um pedido **DELIVERED** nesse restaurante; a avaliação está ligada ao pedido
- **Menu partilhado**: modificar um produto de um menu partilhado reflete-se em todos os restaurantes que usam esse menu
- **Atribuição automática de entregador**: quando um pedido transita para READY, o sistema escolhe um entregador com `disponivel=true` (pode filtrar por `zonaAtuacao`)
- **`horaRetirada`** em `Entrega`: definida em `iniciarEntrega()` (quando o entregador recolhe o pedido), não na atribuição
- **`horaEntrega`** em `Entrega`: definida em `concluir()` (quando o entregador entrega ao cliente)

## Queries de Negócio (validação do modelo)

O modelo deve permitir responder a:

1. **No caso de pagamento com numerário, qual é a média do troco?**
   → `SELECT AVG(d.troco) FROM Dinheiro d`
2. **Qual é o item mais pedido de um restaurante?**
   → `SELECT pp.produto, SUM(pp.quantity) FROM ProdutoPedido pp WHERE pp.produto.restaurante.id = :rid GROUP BY pp.produto ORDER BY SUM(pp.quantity) DESC`
3. **Qual o entregador com mais entregas para um restaurante específico?**
   → Via `Entrega → Pedido → ProdutoPedido → Produto → Restaurante`, agrupado por entregador
4. **Qual o restaurante mais popular de uma franquia (menu partilhado)?**
   → Via `Menu → Restaurantes` + contagem de pedidos por restaurante
5. **Qual é o cliente que mais pedidos fez num intervalo de tempo?**
   → `SELECT p.cliente, COUNT(*) FROM Pedido p WHERE p.dataHora BETWEEN :inicio AND :fim GROUP BY p.cliente ORDER BY COUNT(*) DESC`
