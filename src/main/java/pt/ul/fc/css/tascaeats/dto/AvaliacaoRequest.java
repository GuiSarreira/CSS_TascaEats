package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para os endpoints de criação e atualização de avaliações.
 *
 * Campos obrigatórios para criação: {@code clienteId}, {@code restauranteId},
 * {@code pedidoId}, {@code nota}.
 * Para atualização via {@code PUT /api/avaliacoes/{id}}: apenas {@code nota} e
 * {@code comentario} são usados.
 */
public class AvaliacaoRequest {

    /** ID do cliente que cria a avaliação. */
    private Long clienteId;

    /** ID do restaurante a avaliar. */
    private Long restauranteId;

    /** ID do pedido que fundamenta a avaliação (deve ter status DELIVERED). */
    private Long pedidoId;

    /** Nota da avaliação (1–5). */
    private int nota;

    /** Comentário opcional. */
    private String comentario;

    /** Construtor vazio exigido para deserialização do JSON. */
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
