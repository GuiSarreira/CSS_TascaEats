package pt.ul.fc.css.entrega.dto;

/**
 * DTO de request para o endpoint {@code POST /api/pedidos/{id}/entregar}.
 * Espelha o DTO homónimo do monólito para garantir compatibilidade na
 * deserialização quando o monólito faz proxy desta chamada.
 */
public class AtribuirEntregadorRequest {

    /** ID do entregador a atribuir manualmente. Se null, atribuição automática. */
    private Long entregadorId;

    public AtribuirEntregadorRequest() {}

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }
}
