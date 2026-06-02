package pt.ul.fc.css.tascaeats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntregaStatusEvent {

    @JsonProperty("pedidoId")
    private Long pedidoId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("entregaId")
    private Long entregaId;

    public EntregaStatusEvent() {}

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }
}
