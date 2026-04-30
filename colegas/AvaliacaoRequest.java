package pt.ul.fc.css.tascaeats.dto;

public class AvaliacaoRequest {
    private Long clienteId;
    private Long restauranteId;
    private Long pedidoId;
    private int nota;
    private String comentario;

    // Construtores, getters e setters
    public AvaliacaoRequest() {}
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}