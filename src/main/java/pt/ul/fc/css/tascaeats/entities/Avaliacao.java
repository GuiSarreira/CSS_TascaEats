package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe que representa uma avaliação de um restaurante feita por um cliente
 * após a conclusão de um pedido.
 *
 * Cada avaliação está ligada a um pedido específico (relação {@code @OneToOne}
 * com unicidade garantida pela coluna {@code pedido_id unique=true}). Um cliente
 * pode avaliar o mesmo restaurante várias vezes, desde que cada avaliação
 * corresponda a um pedido diferente.
 */
@Entity
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int nota; // 1 a 5

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    protected Avaliacao() {
    }

    /**
     * Construtor com validação da nota (1–5).
     */
    public Avaliacao(int nota, String comentario, Cliente cliente,
            Restaurante restaurante, Pedido pedido) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5.");
        }
        this.nota = nota;
        this.comentario = comentario;
        this.cliente = cliente;
        this.restaurante = restaurante;
        this.pedido = pedido;
        this.dataAvaliacao = LocalDateTime.now();
    }

    /**
     * Atualiza a nota e o comentário (apenas usado pelo cliente criador).
     */
    public void atualizar(int nota, String comentario) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5.");
        }
        this.nota = nota;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public Pedido getPedido() {
        return pedido;
    }
}
