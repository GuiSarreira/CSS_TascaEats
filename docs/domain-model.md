# TascaEats — Modelo de Domínio

## Diagrama de Classes

```mermaid
classDiagram
    direction TB

    class User {
        <<Abstract>>
        Long id
        String name
        String email
        String password
        String phone
        UserRole role
        LocalDateTime registrationDate
    }

    class Client {
        String address
    }

    class Admin {
    }

    class DeliveryPerson {
        String vehicleType
        boolean available
    }

    class Restaurant {
        Long id
        String name
        String nif
        String address
        String city
        boolean open
    }

    class Product {
        Long id
        String name
        String description
        BigDecimal price
        boolean available
        boolean deleted
    }

    class Order {
        Long id
        LocalDateTime createdAt
        BigDecimal totalPrice
        String deliveryAddress
        OrderStatus status
        Long version
    }

    class OrderItem {
        Long id
        int quantity
        BigDecimal priceAtPurchase
    }

    class Payment {
        <<Abstract>>
        Long id
        BigDecimal amount
        LocalDateTime paymentDate
        PaymentStatus status
    }

    class MultibancoPagamento {
        String entityReference
    }

    class MBWayPagamento {
        String phoneNumber
    }

    class DinheiroPagamento {
    }

    class Delivery {
        Long id
        LocalDateTime pickupTime
        LocalDateTime deliveryTime
    }

    class OrderStatus {
        <<Enumeration>>
        CREATED
        PAID
        PREPARING
        READY
        IN_DELIVERY
        DELIVERED
        CANCELLED
    }

    class PaymentStatus {
        <<Enumeration>>
        PENDING
        COMPLETED
        FAILED
    }

    class UserRole {
        <<Enumeration>>
        CLIENT
        ADMIN
        DELIVERY_PERSON
    }

    User <|-- Client : JOINED
    User <|-- Admin : JOINED
    User <|-- DeliveryPerson : JOINED

    Payment <|-- MultibancoPagamento : SINGLE_TABLE
    Payment <|-- MBWayPagamento : SINGLE_TABLE
    Payment <|-- DinheiroPagamento : SINGLE_TABLE

    Client "1" --> "*" Order : places
    Admin "1" --> "*" Restaurant : manages
    DeliveryPerson "1" --> "*" Delivery : performs

    Restaurant "1" --> "*" Product : has
    Order "*" --> "1" Restaurant : from
    Order "1" --> "*" OrderItem : contains
    Order "1" --> "0..1" Payment : paid by
    Order "1" --> "0..1" Delivery : delivered by

    OrderItem "*" --> "1" Product : references
    Delivery "*" --> "1" DeliveryPerson : assigned to
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
| `User` → Client, Admin, DeliveryPerson | **JOINED** | Subtipos com atributos distintos; queries frequentes por subtipo; melhor normalização |
| `Payment` → Multibanco, MBWay, Dinheiro | **SINGLE_TABLE** | Poucos campos diferenciadores; queries polimórficas frequentes; melhor performance |

## Relações Principais

| De | Para | Cardinalidade | Notas |
|----|------|--------------|-------|
| Client | Order | 1:N | Cliente faz vários pedidos |
| Admin | Restaurant | 1:N | Admin gere vários restaurantes |
| DeliveryPerson | Delivery | 1:N | Entregador faz várias entregas |
| Restaurant | Product | 1:N | Restaurante tem vários produtos |
| Order | OrderItem | 1:N | Pedido contém vários items |
| OrderItem | Product | N:1 | Item referencia um produto |
| Order | Payment | 1:1 | Pedido tem um pagamento |
| Order | Delivery | 1:1 | Pedido tem uma entrega |
| Order | Restaurant | N:1 | Pedido é de um restaurante |

## Regras de Integridade

- **Soft-delete** em `Product`: se tem pedidos associados, marca `deleted=true` em vez de apagar
- **Optimistic locking** em `Order`: campo `@Version` para controlo de concorrência
- **`priceAtPurchase`** em `OrderItem`: captura o preço no momento da compra (imutável)
- **`available`** em `DeliveryPerson`: controla disponibilidade para novas entregas
