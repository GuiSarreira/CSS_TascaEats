package pt.ul.fc.css.tascaeats.entities;

/**
 * Enumeração que representa os possíveis estados do ciclo de vida de uma {@link Entrega}.
 *
 * <p>Uma entrega é criada no estado {@code ATRIBUIDA} quando um {@link Entregador}
 * é associado a um {@link Pedido} no estado {@code READY}. O fluxo normal é:
 * <pre>
 *   ATRIBUIDA → A_CAMINHO → CONCLUIDA
 *            ↘ CANCELADA
 * </pre>
 */
public enum EntregaStatus {

    /**
     * Entrega criada e entregador atribuído. Ainda não saiu do restaurante.
     * Estado inicial de qualquer entrega.
     */
    ATRIBUIDA,

    /**
     * O entregador recolheu o pedido no restaurante e está a caminho do cliente.
     */
    A_CAMINHO,

    /**
     * Pedido entregue ao cliente com sucesso. Estado terminal positivo.
     * O entregador fica novamente {@code disponivel = true}.
     */
    CONCLUIDA,

    /**
     * Entrega cancelada antes de sair do restaurante. Estado terminal negativo.
     * O entregador fica novamente {@code disponivel = true}.
     */
    CANCELADA
}
