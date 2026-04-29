package pt.ul.fc.css.tascaeats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de request para o endpoint {@code POST /api/restaurantes/{id}/produtos}.
 *
 * Contém os dados iniciais do novo produto a adicionar ao menu
 * de um restaurante. A disponibilidade é {@code true} por padrão.
 */
public class CriarProdutoRequest {

    /** Nome do produto. */
    @NotNull
    private String nome;

    /** Descrição do produto (opcional). */
    private String descricao;

    /** Preço unitário em euros. Deve ser maior que zero. */
    @NotNull
    @Positive
    private Double preco;

    /**
     * Estado de disponibilidade inicial do produto.
     * {@code true} por omissão — produto disponível para encomenda.
     */
    private boolean disponivel = true;

    /** Categoria do produto (ex: Entrada, Prato Principal, Sobremesa). */
    private String categoria;

    /** IDs de menus a associar ao produto (opcional). */
    private java.util.List<Long> menuIds;

    /** Construtor vazio exigido para deserialização do JSON. */
    public CriarProdutoRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public java.util.List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(java.util.List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
