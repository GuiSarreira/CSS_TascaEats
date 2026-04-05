package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe que representa a entrega física de um {@link Pedido} ao cliente.
 *
 * Uma {@code Entrega} é criada quando um {@link Entregador} disponível é atribuído
 * a um pedido em estado {@code READY}. No momento de criação, o entregador fica
 * {@code disponivel = false} até concluir ou cancelar a entrega.
 *
 * Ciclo de vida
 *   ATRIBUIDA -> A_CAMINHO -> CONCLUIDA
 *             ou CANCELADA
 */
@Entity
public class Entrega {

    /**
     * Identificador único da entrega, gerado automaticamente pela base de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pedido associado a esta entrega. Lado dono da relação 1:1.
     * A chave estrangeira {@code pedido_id} fica nesta tabela.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    /**
     * Entregador responsável por esta entrega. Lado N da relação N:1 com {@link Entregador}.
     * A chave estrangeira {@code entregador_id} fica nesta tabela.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id", nullable = false)
    private Entregador entregador;

    /**
     * Hora em que o entregador recolheu o pedido no restaurante.
     * Definida no construtor como o instante de criação da entrega.
     */
    @Column(nullable = false)
    private LocalDateTime horaRetirada;

    /**
     * Hora em que o pedido foi entregue ao cliente.
     * {@code null} até a entrega ser concluída via {@link #concluir()}.
     */
    private LocalDateTime horaEntrega;

    /**
     * Estado atual desta entrega no seu ciclo de vida.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntregaStatus status;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected Entrega() {}

    /**
     * Cria uma nova entrega no estado {@link EntregaStatus#ATRIBUIDA}.
     *
     * Regista o instante atual como {@code horaRetirada}.
     *
     * @param pedido      o pedido a entregar; não pode ser {@code null}
     * @param entregador  o entregador atribuído; deve estar {@code disponivel = true}
     */
    public Entrega(Pedido pedido, Entregador entregador) {
        this.pedido = pedido;
        this.entregador = entregador;
        this.horaRetirada = LocalDateTime.now();
        this.status = EntregaStatus.ATRIBUIDA;
    }

    /**
     * Inicia a entrega, transitando para {@link EntregaStatus#A_CAMINHO}.
     * Indica que o entregador já recolheu o pedido e está a caminho do cliente.
     *
     * @throws IllegalStateException se a entrega não estiver no estado {@code ATRIBUIDA}
     */
    public void iniciarEntrega() {
        if (this.status != EntregaStatus.ATRIBUIDA) {
            throw new IllegalStateException("Só é possível iniciar entrega no estado ATRIBUIDA");
        }
        this.status = EntregaStatus.A_CAMINHO;
    }

    /**
     * Conclui a entrega com sucesso, transitando para {@link EntregaStatus#CONCLUIDA}.
     * Regista o instante atual em {@code horaEntrega} e marca o entregador como disponível.
     *
     * @throws IllegalStateException se a entrega não estiver no estado {@code A_CAMINHO}
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
     * @throws IllegalStateException se a entrega não estiver no estado {@code ATRIBUIDA}
     */
    public void cancelar() {
        if (this.status != EntregaStatus.ATRIBUIDA) {
            throw new IllegalStateException("Só é possível cancelar entrega no estado ATRIBUIDA");
        }
        this.status = EntregaStatus.CANCELADA;
        this.entregador.setDisponivel(true);
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Entregador getEntregador() {
        return entregador;
    }

    public void setEntregador(Entregador entregador) {
        this.entregador = entregador;
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

    public EntregaStatus getStatus() {
        return status;
    }

    public void setStatus(EntregaStatus status) {
        this.status = status;
    }
}