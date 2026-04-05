package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe que representa um pedido de comida na plataforma TascaEats.
 *
 * Um pedido é criado por um {@link Cliente} a um {@link Restaurante}
 * específico.
 * Cada pedido contém uma lista de {@link ProdutoPedido} (os itens
 * encomendados),
 * pode ter um {@link Pagamento} associado e, após estar pronto, uma
 * {@link Entrega}
 * atribuída a um {@link Entregador}.
 *
 *
 * Regras de negócio
 * - Só é possível criar pedidos a restaurantes {@code aberto = true}.
 * - Só é possível adicionar produtos com {@code disponivel = true} e
 * {@code eliminado = false}.
 * - O preço total é calculado automaticamente a partir dos
 * {@link ProdutoPedido}.
 * - Um pedido só pode ser cancelado nos estados {@code CREATED} ou
 * {@code PAID}.
 * - O campo {@code version} garante controlo de concorrência otimista via
 * {@code @Version},
 * prevenindo race conditions quando dois entregadores tentam aceitar o mesmo
 * pedido.
 *
 * Persistência
 * Mapeada para a tabela {@code pedido}. Os itens do pedido são mapeados em
 * cascata
 * (se o pedido for eliminado, os seus {@link ProdutoPedido} também o são).
 */
@Entity
public class Pedido {

    /**
     * Identificador único do pedido, gerado automaticamente pela base de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data e hora em que o pedido foi criado.
     * Definido automaticamente no construtor e não pode ser alterado após criação.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    /**
     * Valor total do pedido em euros.
     * Calculado automaticamente como a soma de {@code precoCompra * quantity}
     * de cada {@link ProdutoPedido}. Atualizado sempre que se adiciona um item.
     */
    @Column(nullable = false)
    private Double precoTotal = 0.0;

    /**
     * Morada de entrega fornecida pelo cliente para este pedido específico.
     */
    @Column(nullable = false)
    private String enderecoEntrega;

    /**
     * Estado atual do pedido no seu ciclo de vida.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PedidoStatus status;

    /**
     * Campo de controlo de concorrência otimista gerido pelo JPA.
     * Incrementado automaticamente em cada {@code UPDATE}. Se dois processos
     * tentarem atualizar o mesmo pedido em simultâneo, o segundo receberá
     * uma {@code OptimisticLockException}, evitando estados inconsistentes.
     */
    @Version
    private Long version;

    /**
     * Cliente que efetuou este pedido. Lado N da relação N:1 com {@link Cliente}.
     * A chave estrangeira {@code cliente_id} fica na tabela {@code pedido}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Restaurante ao qual o pedido foi feito. Lado N da relação N:1 com
     * {@link Restaurante}.
     * A chave estrangeira {@code restaurante_id} fica na tabela {@code pedido}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    /**
     * Lista dos itens que compõem este pedido.
     * Relação 1:N com {@link ProdutoPedido}. Em cascata: ao eliminar o pedido,
     * os seus itens são também eliminados ({@code orphanRemoval = true}).
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoPedido> produtosPedido = new ArrayList<>();

    /**
     * Pagamento associado a este pedido. Pode ser {@code null} se o pedido ainda
     * não foi pago. A FK {@code pedido_id} fica na tabela do pagamento.
     */
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Pagamento pagamento;

    /**
     * Entrega associada a este pedido. Pode ser {@code null} enquanto o pedido
     * não estiver no estado {@code READY}. A FK {@code pedido_id} fica na tabela
     * das entregas.
     */
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Entrega entrega;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected Pedido() {
    }

    /**
     * Cria um novo pedido no estado {@code CREATED}.
     *
     * @param cliente         o cliente que está a fazer o pedido; não pode ser
     *                        {@code null}
     * @param restaurante     o restaurante ao qual o pedido é feito; não pode ser
     *                        {@code null}
     * @param enderecoEntrega a morada de entrega para este pedido; não pode ser
     *                        {@code null}
     */
    public Pedido(Cliente cliente, Restaurante restaurante, String enderecoEntrega) {
        this.cliente = cliente;
        this.restaurante = restaurante;
        this.enderecoEntrega = enderecoEntrega;
        this.dataHora = LocalDateTime.now();
        this.status = PedidoStatus.CREATED;
        this.precoTotal = 0.0;
    }

    /**
     * Adiciona um item ao pedido e recalcula o preço total.
     *
     * O item fica associado a este pedido.
     *
     * @param item o item a adicionar; não pode ser {@code null}
     */
    public void adicionarProduto(ProdutoPedido item) {
        this.produtosPedido.add(item);
        item.setPedido(this);
        recalcularTotal();
    }

    /**
     * Recalcula o preço total do pedido como a soma de
     * {@code precoCompra * quantity} de cada {@link ProdutoPedido}.
     */
    public void recalcularTotal() {
        this.precoTotal = produtosPedido.stream()
                .mapToDouble(pp -> pp.getPrecoCompra() * pp.getQuantity())
                .sum();
    }

    /**
     * Avança o pedido para o estado seguinte no fluxo normal.
     *
     * Fluxo: {@code CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED}.
     *
     * @throws IllegalStateException se o estado atual não permite avançar
     *                               (ex: {@code DELIVERED} ou {@code CANCELLED})
     */
    public void avancarEstado() {
        this.status = switch (this.status) {
            case CREATED -> PedidoStatus.PAID;
            case PAID -> PedidoStatus.PREPARING;
            case PREPARING -> PedidoStatus.READY;
            case READY -> PedidoStatus.IN_DELIVERY;
            case IN_DELIVERY -> PedidoStatus.DELIVERED;
            default -> throw new IllegalStateException(
                    "Não é possível avançar o pedido a partir do estado: " + this.status);
        };
    }

    /**
     * Cancela o pedido, transitando para o estado {@code CANCELLED}.
     *
     * @throws IllegalStateException se o pedido não estiver no estado
     *                               {@code CREATED} ou {@code PAID}
     */
    public void cancelar() {
        if (!podeSerCancelado()) {
            throw new IllegalStateException(
                    "Pedido só pode ser cancelado nos estados CREATED ou PAID. Estado atual: " + this.status);
        }
        this.status = PedidoStatus.CANCELLED;
    }

    /**
     * Indica se o pedido pode ser cancelado no estado atual.
     *
     * @return {@code true} se o estado for {@code CREATED} ou {@code PAID}
     */
    public boolean podeSerCancelado() {
        return this.status == PedidoStatus.CREATED || this.status == PedidoStatus.PAID;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Double getPrecoTotal() {
        return precoTotal;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public PedidoStatus getStatus() {
        return status;
    }

    public void setStatus(PedidoStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public List<ProdutoPedido> getProdutosPedido() {
        return Collections.unmodifiableList(produtosPedido);
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }
}
