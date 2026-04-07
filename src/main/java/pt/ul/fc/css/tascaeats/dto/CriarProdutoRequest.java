package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint {@code POST /api/restaurantes/{id}/produtos}.
 *
 * Contém os dados iniciais do novo produto a adicionar ao menu
 * de um restaurante. A disponibilidade é {@code true} por padrão.
 */
public class CriarProdutoRequest {

    /** Nome do produto. */
    private String nome;

    /** Descrição do produto (opcional). */
    private String descricao;

    /** Preço unitário em euros. Deve ser maior que zero. */
    private Double preco;

    /**
     * Estado de disponibilidade inicial do produto.
     * {@code true} por omissão — produto disponível para encomenda.
     */
    private boolean disponivel = true;

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
}
