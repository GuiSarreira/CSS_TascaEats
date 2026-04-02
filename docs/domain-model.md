# TascaEats — Modelo de Domínio

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
        String telemovel
        UserTypes type
        LocalDateTime registoData
    }

    class Cliente {
        String morada
    }

    class Admin {
    }

    class Entregador {
        String tipoVeiculo
        boolean disponivel
    }

    class Restaurante {
        Long id
        String nome
        String nif
        String morada
        String cidade
        boolean aberto
    }

    class Produto {
        Long id
        String nome
        String descricao
        Double price
        boolean disponivel
        boolean eliminado
    }

    class Pedido {
        Long id
        LocalDateTime dataHora
        Double precoTotal
        String enderecoEntrega
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
    }

    class MBWay {
        String telemovel
    }

    class Dinheiro {
    }

    class Entrega {
        Long id
        LocalDateTime horaSaida
        LocalDateTime horaChegada
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

    class UserTypes {
        <<Enumeration>>
        CLIENTE
        ADMIN
        ENTREGADOR
    }

    User <|-- Cliente : JOINED
    User <|-- Admin : JOINED
    User <|-- Entregador : JOINED

    Pagamento <|-- Multibanco : SINGLE_TABLE
    Pagamento <|-- MBWay : SINGLE_TABLE
    Pagamento <|-- Dinheiro : SINGLE_TABLE

    Cliente "1" --> "*" Pedido : faz
    Admin "1" --> "*" Restaurante : gere
    Entregador "1" --> "*" Entrega : faz

    Restaurante "1" --> "*" Produto : tem
    Pedido "*" --> "1" Restaurante : do
    Pedido "1" --> "*" ProdutoPedido : contem
    Pedido "1" --> "0..1" Pagamento : pago por
    Pedido "1" --> "0..1" Entrega : entregue por

    ProdutoPedido "*" --> "1" Produto : associado
    Entrega "*" --> "1" Entregador : para
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
    READY --> IN_DELIVERY : Entregador atribuído
    IN_DELIVERY --> DELIVERED : Entrega concluída
    DELIVERED --> [*]
    CANCELLED --> [*]
```

## Estratégias de Herança JPA

| Hierarquia | Estratégia | Justificação |
|-----------|------------|--------------|
| `User` → Cliente, Admin, Entregador | **JOINED** | Subtipos com atributos distintos; queries frequentes por subtipo; melhor normalização |
| `Pagamento` → Multibanco, MBWay, Dinheiro | **SINGLE_TABLE** | Poucos campos diferenciadores; queries polimórficas frequentes; melhor performance |

## Relações Principais

| De | Para | Cardinalidade | Notas |
|----|------|--------------|-------|
| Cliente | Pedido | 1:N | Cliente faz vários pedidos |
| Admin | Restaurante | 1:N | Admin gere vários restaurantes |
| Entregador | Entrega | 1:N | Entregador faz várias entregas |
| Restaurante | Produto | 1:N | Restaurante tem vários produtos |
| Pedido | ProdutoPedido | 1:N | Pedido contém vários items |
| ProdutoPedido | Produto | N:1 | Item referencia um produto |
| Pedido | Pagamento | 1:1 | Pedido tem um pagamento |
| Pedido | Entrega | 1:1 | Pedido tem uma entrega |
| Pedido | Restaurante | N:1 | Pedido é de um restaurante |

## Regras de Integridade

- **Soft-delete** em `Produto`: se tem pedidos associados, marca `eliminado=true` em vez de apagar
- **Optimistic locking** em `Pedido`: campo `@Version` para controlo de concorrência
- **`precoCompra`** em `ProdutoPedido`: captura o preço no momento da compra (imutável)
- **`disponivel`** em `Entregador`: controla disponibilidade para novas entregas
