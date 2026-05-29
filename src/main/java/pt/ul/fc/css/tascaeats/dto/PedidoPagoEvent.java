package pt.ul.fc.css.tascaeats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evento Kafka publicado no tópico {@code pedido.pago} quando um pagamento
 * é confirmado com sucesso no monólito.
 *
 * <p>Consumido pelo {@code entrega-service} para iniciar o processo de
 * atribuição automática de entregador.
 *
 * <p>Payload JSON:
 * <pre>
 * {
 *   "pedidoId": 42,
 *   "moradaEntrega": "Rua Augusta 10, 1100-053",
 *   "cidade": "Lisboa",
 *   "valorTotal": 25.50
 * }
 * </pre>
 */
public class PedidoPagoEvent {

    /** Identificador do pedido que foi pago. */
    @JsonProperty("pedidoId")
    private Long pedidoId;

    /** Morada completa de entrega (rua + código postal). */
    @JsonProperty("moradaEntrega")
    private String moradaEntrega;

    /** Cidade de entrega — utilizada pelo microserviço para encontrar entregador na zona. */
    @JsonProperty("cidade")
    private String cidade;

    /** Valor total do pedido em euros. */
    @JsonProperty("valorTotal")
    private Double valorTotal;

    /** Construtor vazio para deserialização JSON. */
    public PedidoPagoEvent() {
    }

    /**
     * Cria um evento de pedido pago com todos os campos necessários.
     *
     * @param pedidoId      ID do pedido
     * @param moradaEntrega morada de entrega completa
     * @param cidade        cidade de entrega
     * @param valorTotal    valor total do pedido
     */
    public PedidoPagoEvent(Long pedidoId, String moradaEntrega, String cidade, Double valorTotal) {
        this.pedidoId = pedidoId;
        this.moradaEntrega = moradaEntrega;
        this.cidade = cidade;
        this.valorTotal = valorTotal;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getMoradaEntrega() {
        return moradaEntrega;
    }

    public void setMoradaEntrega(String moradaEntrega) {
        this.moradaEntrega = moradaEntrega;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    @Override
    public String toString() {
        return "PedidoPagoEvent{" +
                "pedidoId=" + pedidoId +
                ", moradaEntrega='" + moradaEntrega + '\'' +
                ", cidade='" + cidade + '\'' +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
