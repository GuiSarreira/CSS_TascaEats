package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id", nullable = false)
    private Entregador entregador;

    @Column(nullable = false)
    private LocalDateTime horaRetirada;

    private LocalDateTime horaEntrega;

    @Column(nullable = false)
    private String status;  // "ATRIBUIDA", "EM_CAMINHO", "CONCLUIDA", "CANCELADA"

    protected Entrega() {}

    public Entrega(Pedido pedido, Entregador entregador) {
        this.pedido = pedido;
        this.entregador = entregador;
        this.horaRetirada = LocalDateTime.now();
        this.status = "ATRIBUIDA";
    }

    public void iniciarEntrega() {
        if ("ATRIBUIDA".equals(this.status)) {
            this.status = "EM_CAMINHO";
        } else {
            throw new IllegalStateException("Só é possível iniciar entrega no estado ATRIBUIDA");
        }
    }

    public void concluir() {
        if ("EM_CAMINHO".equals(this.status)) {
            this.horaEntrega = LocalDateTime.now();
            this.status = "CONCLUIDA";
            this.entregador.setDisponivel(true);
        } else {
            throw new IllegalStateException("Só é possível concluir entrega no estado EM_CAMINHO");
        }
    }

    public void cancelar() {
        if ("ATRIBUIDA".equals(this.status)) {
            this.status = "CANCELADA";
            this.entregador.setDisponivel(true);
        } else {
            throw new IllegalStateException("Só é possível cancelar entrega no estado ATRIBUIDA");
        }
    }

    // Getters e Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}