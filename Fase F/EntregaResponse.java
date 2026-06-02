package pt.ul.fc.css.entrega.dto;

import pt.ul.fc.css.entrega.entities.Entrega;

public class EntregaResponse {
    private Long id;
    private Long pedidoId;
    private Long entregadorId;
    private String entregadorNome;
    private String moradaEntrega;
    private String status;
    private String horaRetirada;
    private String horaEntrega;

    public static EntregaResponse from(Entrega e) {
        EntregaResponse dto = new EntregaResponse();
        dto.setId(e.getId());
        dto.setPedidoId(e.getPedidoId());
        dto.setEntregadorId(e.getEntregador().getId());
        dto.setEntregadorNome(e.getEntregador().getNome());
        dto.setMoradaEntrega(e.getMoradaEntrega());
        dto.setStatus(e.getStatus().name());
        if (e.getHoraRetirada() != null) dto.setHoraRetirada(e.getHoraRetirada().toString());
        if (e.getHoraEntrega() != null) dto.setHoraEntrega(e.getHoraEntrega().toString());
        return dto;
    }

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getEntregadorId() { return entregadorId; }
    public void setEntregadorId(Long entregadorId) { this.entregadorId = entregadorId; }
    public String getEntregadorNome() { return entregadorNome; }
    public void setEntregadorNome(String entregadorNome) { this.entregadorNome = entregadorNome; }
    public String getMoradaEntrega() { return moradaEntrega; }
    public void setMoradaEntrega(String moradaEntrega) { this.moradaEntrega = moradaEntrega; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHoraRetirada() { return horaRetirada; }
    public void setHoraRetirada(String horaRetirada) { this.horaRetirada = horaRetirada; }
    public String getHoraEntrega() { return horaEntrega; }
    public void setHoraEntrega(String horaEntrega) { this.horaEntrega = horaEntrega; }
}