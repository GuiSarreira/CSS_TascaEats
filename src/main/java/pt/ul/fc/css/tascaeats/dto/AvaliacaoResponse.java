package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Avaliacao;

import java.time.LocalDateTime;

/**
 * DTO de response para dados de uma avaliação.
 *
 * Usado nos endpoints de criação, atualização e listagem de avaliações.
 * Expõe os dados relevantes sem serializar grafos de entidades JPA.
 */
public class AvaliacaoResponse {

    private Long id;
    private int nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;
    private Long clienteId;
    private String clienteNome;
    private Long restauranteId;
    private String restauranteNome;
    private Long pedidoId;

    /** Construtor vazio para uso interno. */
    public AvaliacaoResponse() {}

    /**
     * Cria um {@code AvaliacaoResponse} a partir de uma entidade {@link Avaliacao}.
     *
     * @param a entidade avaliação
     * @return DTO preenchido com os campos da avaliação
     */
    public static AvaliacaoResponse from(Avaliacao a) {
        AvaliacaoResponse dto = new AvaliacaoResponse();
        dto.setId(a.getId());
        dto.setNota(a.getNota());
        dto.setComentario(a.getComentario());
        dto.setDataAvaliacao(a.getDataAvaliacao());
        dto.setClienteId(a.getCliente().getId());
        dto.setClienteNome(a.getCliente().getNome());
        dto.setRestauranteId(a.getRestaurante().getId());
        dto.setRestauranteNome(a.getRestaurante().getNome());
        dto.setPedidoId(a.getPedido().getId());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getRestauranteNome() { return restauranteNome; }
    public void setRestauranteNome(String restauranteNome) { this.restauranteNome = restauranteNome; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
}
