package pt.ul.fc.css.tascaeats.entities;

/**
 * Enumeração que representa os possíveis estados do ciclo de vida de um {@link Pedido}.
 *
 * O fluxo normal de um pedido é:
 *   CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED
 * Um pedido pode ser cancelado apenas nos estados {@code CREATED} ou {@code PAID}.
 */
public enum PedidoStatus {

    /**
     * Pedido criado.
     * Estado inicial ao criar o pedido.
     */
    CREATED,

    /**
     * Pagamento confirmado.
     */
    PAID,

    /**
     * O restaurante está a preparar o pedido.
     */
    PREPARING,

    /**
     * O pedido está pronto para ser recolhido pelo entregador.
     */
    READY,

    /**
     * O entregador recolheu o pedido e está a caminho do cliente.
     */
    IN_DELIVERY,

    /**
     * Pedido entregue com sucesso ao cliente.
     */
    DELIVERED,

    /**
     * Pedido cancelado.
     * Só é possível atingir este estado a partir de {@code CREATED} ou {@code PAID}.
     */
    CANCELLED
}
