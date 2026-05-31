package pt.ul.fc.css.tascaeats.dto;

public class EntregaAtribuidaEvent {
    private Long pedidoId;
    private Long entregaId;
    private Long entregadorId;

    // construtor vazio, getters, setters
    public EntregaAtribuidaEvent() {}
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }
    public Long getEntregadorId() { return entregadorId; }
    public void setEntregadorId(Long entregadorId) { this.entregadorId = entregadorId; }
}