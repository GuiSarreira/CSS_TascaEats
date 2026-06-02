package pt.ul.fc.css.tascaeats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntregaAtribuidaEvent {

    @JsonProperty("pedidoId")
    private Long pedidoId;

    @JsonProperty("entregaId")
    private Long entregaId;

    @JsonProperty("entregadorId")
    private Long entregadorId;

    public EntregaAtribuidaEvent() {}

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }

    public Long getEntregadorId() { return entregadorId; }
    public void setEntregadorId(Long entregadorId) { this.entregadorId = entregadorId; }
}
