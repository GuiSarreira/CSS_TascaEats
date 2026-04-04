package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;

/**
 * Classe que representa um item dentro de um {@link Pedido}.
 *
 * Cada {@code ProdutoPedido} corresponde a uma linha do pedido: um
 * {@link Produto}
 * específico com uma quantidade e o preço no momento da compra
 * ({@code precoCompra}).
 *
 * Persistência
 * Mapeada para a tabela {@code produtos_pedido}. O ciclo de vida é gerido em
 * cascata
 * pelo {@link Pedido} pai — ao apagar um pedido, os seus itens são também
 * apagados.
 */
@Entity
@Table(name = "produtos_pedido")
public class ProdutoPedido {

    /**
     * Identificador único do item, gerado automaticamente pela base de dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quantidade encomendada deste produto neste pedido.
     * Deve ser sempre maior que zero.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Preço unitário do produto no momento em que o pedido foi efetuado.
     *
     * Capturado a partir de {@link Produto#getPrice()} no construtor e imutável
     * após criação, garantindo que atualizações futuras ao catálogo não alterem
     * o valor de pedidos históricos.
     */
    @Column(nullable = false)
    private Double precoCompra;

    /**
     * Pedido ao qual este item pertence. Lado N da relação N:1 com {@link Pedido}.
     * A chave estrangeira {@code pedido_id} fica nesta tabela.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /**
     * Produto encomendado. Lado N da relação N:1 com {@link Produto}.
     * A chave estrangeira {@code produto_id} fica nesta tabela.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected ProdutoPedido() {
    }

    /**
     * Cria um novo item de pedido para o produto e quantidade indicados.
     *
     * O {@code precoCompra} é automaticamente capturado a partir do preço
     * atual do produto, garantindo o histórico de preços.
     *
     * @param produto  o produto a encomendar; não pode ser {@code null}
     * @param quantity a quantidade encomendada; deve ser maior que zero
     */
    public ProdutoPedido(Produto produto, int quantity) {
        this.produto = produto;
        this.quantity = quantity;
        this.precoCompra = produto.getPreco();
    }

    /**
     * Devolve o identificador único deste item.
     *
     * @return o id do item
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve a quantidade encomendada.
     *
     * @return quantidade do produto neste item
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Define a quantidade encomendada.
     *
     * @param quantity nova quantidade; deve ser maior que zero
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Devolve o preço unitário capturado no momento da compra.
     *
     * @return preço de compra em euros
     */
    public Double getPrecoCompra() {
        return precoCompra;
    }

    /**
     * Devolve o pedido ao qual este item pertence.
     *
     * @return o pedido pai
     */
    public Pedido getPedido() {
        return pedido;
    }

    /**
     * Define o pedido ao qual este item pertence.
     * Usado internamente por {@link Pedido#adicionarProduto(ProdutoPedido)}.
     *
     * @param pedido o pedido a associar
     */
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    /**
     * Devolve o produto referenciado por este item.
     *
     * @return o produto encomendado
     */
    public Produto getProduto() {
        return produto;
    }

    /**
     * Define o produto referenciado por este item.
     *
     * @param produto o produto a associar
     */
    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    /**
     * Calcula o subtotal deste item (precoCompra × quantity).
     *
     * @return subtotal em euros
     */
    public Double getSubtotal() {
        return this.precoCompra * this.quantity;
    }
}
