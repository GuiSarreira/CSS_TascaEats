package pt.ul.fc.css.entrega.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PedidoPagoEvent {
    @JsonProperty("pedidoId")
    private Long pedidoId;
    @JsonProperty("moradaEntrega")
    private String moradaEntrega;
    @JsonProperty("cidade")
    private String cidade;
    @JsonProperty("valorTotal")
    private Double valorTotal;

    // construtor vazio, getters e setters
    public PedidoPagoEvent() {}
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getMoradaEntrega() { return moradaEntrega; }
    public void setMoradaEntrega(String moradaEntrega) { this.moradaEntrega = moradaEntrega; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }
}