package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.ProdutoPedido;

/**
 * DTO de response para um item individual dentro de um pedido.
 *
 * Representa uma linha do pedido: produto, quantidade encomendada e o
 * preço unitário no momento da compra (imutável — não reflete alterações
 * posteriores ao catálogo).
 */
public class ItemPedidoResponse {

    /** ID do registo {@code ProdutoPedido}. */
    private Long id;

    /** ID do produto encomendado. */
    private Long produtoId;

    /** Nome do produto no momento da encomenda. */
    private String produtoNome;

    /** Quantidade encomendada. */
    private int quantidade;

    /** Preço unitário capturado no momento do pedido (em euros). */
    private Double precoCompra;

    /** Construtor vazio para uso interno. */
    public ItemPedidoResponse() {
    }

    private ItemPedidoResponse(Long id, Long produtoId, String produtoNome, int quantidade, Double precoCompra) {
        this.id = id;
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.quantidade = quantidade;
        this.precoCompra = precoCompra;
    }

    /**
     * Cria um {@code ItemPedidoResponse} a partir de uma entidade
     * {@link ProdutoPedido}.
     *
     * @param pp entidade item de pedido
     * @return DTO preenchido com os campos do item
     */
    public static ItemPedidoResponse from(ProdutoPedido pp) {
        return new ItemPedidoResponse(
                pp.getId(),
                pp.getProduto().getId(),
                pp.getProduto().getNome(),
                pp.getQuantity(),
                pp.getPrecoCompra());
    }

    public Long getId() {
        return id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public Double getPrecoCompra() {
        return precoCompra;
    }
}
