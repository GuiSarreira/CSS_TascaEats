package pt.ul.fc.css.tascaeats.entities;

/**
 * Enumeração que representa os possíveis estados de um {@link Pagamento}.
 *
 * Um pagamento é criado no estado {@code PENDING} e transita para
 * {@code COMPLETED} após ser processado com sucesso, ou para {@code FAILED}
 * em caso de erro no processamento.
 */
public enum PagamentoStatus {

    /**
     * Pagamento registado mas ainda não processado.
     * Estado inicial de qualquer pagamento.
     */
    PENDING,

    /**
     * Pagamento processado e confirmado com sucesso.
     * O pedido associado avança para o estado {@code PAID}.
     */
    COMPLETED,

    /**
     * Processamento do pagamento falhou.
     * O pedido permanece no estado {@code CREATED}.
     */
    FAILED
}
