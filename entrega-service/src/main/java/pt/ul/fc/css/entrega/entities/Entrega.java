package pt.ul.fc.css.entrega.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa a entrega física de um pedido ao cliente,
 * no contexto do microserviço de entregas.
 *
 * Ao contrário do monólito, esta entidade não possui relação JPA
 * direta com {@code Pedido}. Em vez disso, guarda apenas o
 * {@code pedidoId} como referência lógica (Long), respeitando o princípio
 * de isolamento de base de dados entre microserviço e monólito.
 *
 * Inclui também o campo {@code moradaEntrega} para que o microserviço
 * tenha a informação necessária sem precisar consultar o monólito.
 *
 * Ciclo de vida:
 * ATRIBUIDA → A_CAMINHO → CONCLUIDA
 * ↘ CANCELADA
 */
@Entity
@Table(name = "entrega")
public class Entrega {

    /**
     * Identificador único da entrega, gerado automaticamente pela base de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referência lógica ao pedido no monólito.
     * Não é uma FK real — o microserviço não acede à BD do monólito.
     */
    @Column(nullable = false, unique = true)
    private Long pedidoId;

    /**
     * Entregador responsável por esta entrega. Lado N da relação N:1 com
     * {@link Entregador}. A chave estrangeira {@code entregador_id} fica nesta
     * tabela.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id", nullable = false)
    private Entregador entregador;

    /** Morada de entrega do cliente, recebida via evento Kafka. */
    @Column(nullable = false)
    private String moradaEntrega;

    /** Estado atual desta entrega no seu ciclo de vida. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntregaStatus status;

    /**
     * Hora em que o entregador recolheu o pedido no restaurante.
     * {@code null} enquanto a entrega estiver no estado {@code ATRIBUIDA}.
     */
    @Column
    private LocalDateTime horaRetirada;

    /**
     * Hora em que o pedido foi entregue ao cliente.
     * {@code null} até a entrega ser concluída.
     */
    @Column
    private LocalDateTime horaEntrega;

    /** Construtor protegido exigido pelo JPA. */
    protected Entrega() {
    }

    /**
     * Cria uma nova entrega no estado {@link EntregaStatus#ATRIBUIDA}.
     *
     * @param pedidoId      referência lógica ao pedido no monólito
     * @param entregador    o entregador atribuído; deve estar disponível
     * @param moradaEntrega morada de destino da entrega
     */
    public Entrega(Long pedidoId, Entregador entregador, String moradaEntrega) {
        this.pedidoId = pedidoId;
        this.entregador = entregador;
        this.moradaEntrega = moradaEntrega;
        this.status = EntregaStatus.ATRIBUIDA;
    }

    // ── Métodos de ciclo de vida ─────────────────────────────────────

    /**
     * Inicia a entrega, transitando para {@link EntregaStatus#A_CAMINHO}.
     * Regista o instante atual como {@code horaRetirada}.
     *
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code ATRIBUIDA}
     */
    public void iniciarEntrega() {
        if (this.status != EntregaStatus.ATRIBUIDA) {
            throw new IllegalStateException("Só é possível iniciar entrega no estado ATRIBUIDA");
        }
        this.horaRetirada = LocalDateTime.now();
        this.status = EntregaStatus.A_CAMINHO;
    }

    /**
     * Conclui a entrega com sucesso, transitando para
     * {@link EntregaStatus#CONCLUIDA}.
     * Regista o instante atual em {@code horaEntrega} e marca o entregador como
     * disponível.
     *
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code A_CAMINHO}
     */
    public void concluir() {
        if (this.status != EntregaStatus.A_CAMINHO) {
            throw new IllegalStateException("Só é possível concluir entrega no estado A_CAMINHO");
        }
        this.horaEntrega = LocalDateTime.now();
        this.status = EntregaStatus.CONCLUIDA;
        this.entregador.setDisponivel(true);
    }

    /**
     * Cancela a entrega, transitando para {@link EntregaStatus#CANCELADA}.
     * Marca o entregador como disponível para novas entregas.
     *
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code ATRIBUIDA}
     */
    public void cancelar() {
        if (this.status != EntregaStatus.ATRIBUIDA) {
            throw new IllegalStateException("Só é possível cancelar entrega no estado ATRIBUIDA");
        }
        this.status = EntregaStatus.CANCELADA;
        this.entregador.setDisponivel(true);
    }

    // ── Getters e Setters ────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Entregador getEntregador() {
        return entregador;
    }

    public void setEntregador(Entregador entregador) {
        this.entregador = entregador;
    }

    public String getMoradaEntrega() {
        return moradaEntrega;
    }

    public void setMoradaEntrega(String moradaEntrega) {
        this.moradaEntrega = moradaEntrega;
    }

    public EntregaStatus getStatus() {
        return status;
    }

    public void setStatus(EntregaStatus status) {
        this.status = status;
    }

    public LocalDateTime getHoraRetirada() {
        return horaRetirada;
    }

    public void setHoraRetirada(LocalDateTime horaRetirada) {
        this.horaRetirada = horaRetirada;
    }

    public LocalDateTime getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(LocalDateTime horaEntrega) {
        this.horaEntrega = horaEntrega;
    }
}
