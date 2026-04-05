package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe abstrata que representa um pagamento na plataforma TascaEats.
 *
 * Um {@code Pagamento} está sempre associado a exatamente um {@link Pedido} e
 * é criado no estado {@link PagamentoStatus#PENDING}. Após processamento,
 * transita
 * para {@link PagamentoStatus#COMPLETED} (e o pedido avança para {@code PAID})
 * ou para {@link PagamentoStatus#FAILED}.
 *
 * Herança — SINGLE_TABLE
 * Todos os subtipos ({@link Multibanco}, {@link MBWay}, {@link Dinheiro})
 * partilham
 * a mesma tabela {@code pagamento}. O tipo concreto é identificado pela coluna
 * discriminadora {@code tipo_pagamento}. Esta estratégia foi escolhida porque
 * os subtipos
 * têm poucos campos diferenciadores, o que minimiza colunas {@code NULL} e
 * favorece
 * a performance em queries polimórficas.
 *
 * Regras de negócio
 * - Um pedido só pode ter um pagamento.
 * - Ao processar ({@link #processar()}), o campo {@code dataPagamento} é
 * preenchido
 * automaticamente com o instante atual.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_pagamento", discriminatorType = DiscriminatorType.STRING)
public abstract class Pagamento {

    /**
     * Identificador único do pagamento, gerado automaticamente pela base de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Montante do pagamento em euros. Deve corresponder ao {@code precoTotal} do
     * pedido.
     */
    @Column(nullable = false)
    private Double preco;

    /**
     * Data e hora em que o pagamento foi processado com sucesso.
     * {@code null} enquanto o pagamento estiver no estado {@code PENDING} ou
     * {@code FAILED}.
     */
    private LocalDateTime dataPagamento;

    /**
     * Estado atual do pagamento.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PagamentoStatus status;

    /**
     * Pedido ao qual este pagamento está associado. Lado dono da relação 1:1.
     * A chave estrangeira {@code pedido_id} fica nesta tabela.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected Pagamento() {
    }

    /**
     * Cria um novo pagamento no estado {@link PagamentoStatus#PENDING}.
     *
     * @param pedido o pedido que este pagamento liquida; não pode ser {@code null}
     * @param preco  o montante a pagar em euros; deve ser maior que zero
     */
    protected Pagamento(Pedido pedido, Double preco) {
        this.pedido = pedido;
        this.preco = preco;
        this.status = PagamentoStatus.PENDING;
    }

    /**
     * Processa o pagamento, transitando para o estado
     * {@link PagamentoStatus#COMPLETED}
     * e registando o instante de processamento em {@code dataPagamento}.
     *
     * @throws IllegalStateException se o pagamento não estiver no estado
     *                               {@code PENDING}
     */
    public void processar() {
        if (this.status != PagamentoStatus.PENDING) {
            throw new IllegalStateException(
                    "Só é possível processar um pagamento no estado PENDING. Estado atual: " + this.status);
        }
        this.status = PagamentoStatus.COMPLETED;
        this.dataPagamento = LocalDateTime.now();
    }

    /**
     * Marca o pagamento como falhado, transitando para
     * {@link PagamentoStatus#FAILED}.
     *
     * @throws IllegalStateException se o pagamento não estiver no estado
     *                               {@code PENDING}
     */
    public void falhar() {
        if (this.status != PagamentoStatus.PENDING) {
            throw new IllegalStateException(
                    "Só é possível marcar como falhado um pagamento no estado PENDING. Estado atual: " + this.status);
        }
        this.status = PagamentoStatus.FAILED;
    }

    /**
     * Indica se o pagamento foi concluído com sucesso.
     *
     * @return {@code true} se o estado for {@code COMPLETED}
     */
    public boolean isCompleto() {
        return this.status == PagamentoStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public PagamentoStatus getStatus() {
        return status;
    }

    /**
     * Devolve o pedido associado a este pagamento.
     *
     * @return o pedido que este pagamento liquida
     */
    public Pedido getPedido() {
        return pedido;
    }

    /**
     * Define o pedido associado a este pagamento.
     *
     * @param pedido o pedido a associar
     */
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
