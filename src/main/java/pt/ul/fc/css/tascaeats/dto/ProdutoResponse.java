package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Produto;

/**
 * DTO de response para dados de um produto do menu.
 *
 * Usado nos endpoints de listagem e detalhe de produtos
 * ({@code GET /api/restaurantes/{id}/produtos}).
 *
 * Produtos com {@code eliminado = true} não são devolvidos por estes
 * endpoints — o serviço filtra-os antes da conversão para DTO.
 */
public class ProdutoResponse {

    /** ID do produto. */
    private Long id;

    /** ID do restaurante a que o produto pertence. */
    private Long restauranteId;

    /** Nome do produto. */
    private String nome;

    /** Descrição do produto. */
    private String descricao;

    /** Preço unitário em euros. */
    private Double preco;

    /** Se o produto está disponível para encomenda ({@code false} = esgotado). */
    private boolean disponivel;

    /** Construtor vazio para uso interno. */
    public ProdutoResponse() {
    }

    private ProdutoResponse(Long id, Long restauranteId, String nome, String descricao, Double preco,
            boolean disponivel) {
        this.id = id;
        this.restauranteId = restauranteId;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.disponivel = disponivel;
    }

    /**
     * Cria um {@code ProdutoResponse} a partir de uma entidade {@link Produto}.
     *
     * @param p entidade produto (não deve estar eliminado)
     * @return DTO preenchido com os campos do produto
     */
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
                p.getId(),
                p.getRestaurante().getId(),
                p.getNome(),
                p.getDescricao(),
                p.getPreco(),
                p.isDisponivel());
    }

    public Long getId() {
        return id;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
}
