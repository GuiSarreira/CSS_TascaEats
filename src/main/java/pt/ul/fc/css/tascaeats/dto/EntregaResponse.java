package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Entrega;
import pt.ul.fc.css.tascaeats.entities.EntregaStatus;

import java.time.LocalDateTime;

/**
 * DTO de response para dados de uma entrega.
 *
 * Usado nas respostas dos endpoints de atribuição, consulta e gestão
 * do ciclo de vida de entregas ({@code POST /api/pedidos/{id}/entregar},
 * {@code PATCH /api/entregas/{id}/iniciar}, etc.).
 */
public class EntregaResponse {

    /** ID da entrega. */
    private Long id;

    /** ID do pedido associado a esta entrega. */
    private Long pedidoId;

    /** ID do entregador atribuído. */
    private Long entregadorId;

    /** Nome do entregador atribuído. */
    private String entregadorNome;

    /** Hora em que o entregador recolheu o pedido no restaurante. */
    private LocalDateTime horaRetirada;

    /**
     * Hora em que o pedido foi entregue ao cliente.
     * {@code null} até a entrega ser concluída.
     */
    private LocalDateTime horaEntrega;

    /** Estado atual da entrega no seu ciclo de vida. */
    private EntregaStatus status;

    /** Construtor vazio para uso interno. */
    public EntregaResponse() {
    }

    private EntregaResponse(Long id, Long pedidoId, Long entregadorId, String entregadorNome,
                             LocalDateTime horaRetirada, LocalDateTime horaEntrega, EntregaStatus status) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.entregadorId = entregadorId;
        this.entregadorNome = entregadorNome;
        this.horaRetirada = horaRetirada;
        this.horaEntrega = horaEntrega;
        this.status = status;
    }

    /**
     * Cria um {@code EntregaResponse} a partir de uma entidade {@link Entrega}.
     *
     * @param e entidade entrega
     * @return DTO preenchido com os campos da entrega
     */
    public static EntregaResponse from(Entrega e) {
        return new EntregaResponse(
                e.getId(),
                e.getPedido().getId(),
                e.getEntregador().getId(),
                e.getEntregador().getNome(),
                e.getHoraRetirada(),
                e.getHoraEntrega(),
                e.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public String getEntregadorNome() {
        return entregadorNome;
    }

    public LocalDateTime getHoraRetirada() {
        return horaRetirada;
    }

    public LocalDateTime getHoraEntrega() {
        return horaEntrega;
    }

    public EntregaStatus getStatus() {
        return status;
    }
}
