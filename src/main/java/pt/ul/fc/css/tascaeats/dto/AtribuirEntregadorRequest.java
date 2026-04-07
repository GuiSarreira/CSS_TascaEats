package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint {@code POST /api/pedidos/{id}/entregar}.
 *
 * Contém o ID do entregador a atribuir ao pedido.
 * Se {@code entregadorId} for {@code null}, o sistema atribui automaticamente
 * o primeiro entregador disponível na zona do restaurante.
 *
 * Exemplos de JSON:
 * {@code
 *  { "entregadorId": 5 }          // atribuição manual
 *  { }                            // atribuição automática
 * }
 */
public class AtribuirEntregadorRequest {

    /**
     * ID do entregador a atribuir manualmente.
     * Se {@code null}, a atribuição é feita automaticamente.
     */
    private Long entregadorId;

    /** Construtor vazio exigido para deserialização do JSON. */
    public AtribuirEntregadorRequest() {
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }
}
