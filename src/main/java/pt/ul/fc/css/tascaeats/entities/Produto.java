package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um produto no catálogo de um {@link Restaurante}.
 *
 * Um produto pode ser encomendado por clientes ao criar um {@link Pedido}.
 * Cada vez que é encomendado, cria-se um {@link ProdutoPedido} que captura
 * o preço no momento da compra, garantindo imutabilidade histórica.
 *
 * Soft-delete
 * Um produto com pedidos associados não pode ser removido fisicamente da BD.
 * Em vez disso, é marcado com {@code eliminado = true} via {@link #deleteLogicamente()},
 * ficando invisível para novos pedidos mas preservado nos registos históricos.
 */
@Entity
public class Produto {

    /** Identificador único do produto, gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do produto. Não pode ser nulo. */
    @Column(nullable = false)
    private String nome;

    /** Descrição do produto. */
    @Column
    private String descricao;

    /** Preço unitário do produto em euros. Deve ser maior que zero. */
    @Column(nullable = false)
    @Positive
    private Double preco;

    /**
     * Indica se o produto está disponível para encomenda.
     * {@code false} quando esgotado; produto com {@code eliminado = true}
     * também fica automaticamente indisponível.
     */
    @Column(nullable = false)
    private boolean disponivel = true;

    /**
     * Indica se o produto foi apagado logicamente (soft-delete).
     * Quando {@code true}, o produto não é apresentado no catálogo mas
     * os registos em {@link ProdutoPedido} históricos são preservados.
     */
    @Column(nullable = false)
    private boolean eliminado = false;

    /**
     * Restaurante ao qual este produto pertence. Lado N da relação N:1.
     * A chave estrangeira {@code restaurante_id} fica nesta tabela.
     */
    @ManyToOne
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    /**
     * Referências deste produto em itens de pedidos.
     * Lado inverso da relação — o dono é {@link ProdutoPedido#getProduto()}.
     */
    @OneToMany(mappedBy = "produto")
    private List<ProdutoPedido> itensPedido = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA.*/
    protected Produto() {
    }

    /**
     * Cria um novo produto, disponível e não eliminado por defeito.
     *
     * @param nome        nome do produto
     * @param descricao   descrição
     * @param preco       preço unitário em euros; deve ser maior que zero
     * @param restaurante restaurante ao qual o produto pertence
     */
    public Produto(String nome, String descricao, Double preco, Restaurante restaurante) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.restaurante = restaurante;
        this.disponivel = true;
        this.eliminado = false;
    }

    /**
     * Implementa o soft-delete deste produto.
     * Marca-o como eliminado e indisponível, mas mantém o registo na BD
     * para preservar a integridade de {@link ProdutoPedido} históricos.
     */
    public void deleteLogicamente() {
        this.eliminado = true;
        this.disponivel = false;
    }

    public Long getId() {
        return id;
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

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public List<ProdutoPedido> getItensPedido() {
        return itensPedido;
    }

    public void setItensPedido(List<ProdutoPedido> itensPedido) {
        this.itensPedido = itensPedido;
    }
}