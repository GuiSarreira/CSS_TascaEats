package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um menu partilhado entre restaurantes.
 *
 * Um menu agrupa produtos e pode ser associado a múltiplos restaurantes,
 * permitindo menus partilhados. Um produto pode também pertencer a múltiplos
 * menus.
 */
@Entity
public class Menu {

    /** Identificador único do menu, gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do menu. */
    @Column(nullable = false)
    private String nome;

    /** Descrição do menu. */
    @Column
    private String descricao;

    /**
     * Produtos associados a este menu.
     * Menu é o lado owner da relação N:N com Produto.
     */
    @ManyToMany
    @JoinTable(
        name = "menu_produto",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private List<Produto> produtos = new ArrayList<>();

    /**
     * Restaurantes que usam este menu.
     * Lado inverso da relação N:1 — o dono é {@link Restaurante#getMenu()}.
     */
    @OneToMany(mappedBy = "menu")
    private List<Restaurante> restaurantes = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA. */
    protected Menu() {
    }

    /**
     * Cria um novo menu.
     *
     * @param nome         nome do menu
     * @param descricao    descrição do menu
     * @param produtos     lista inicial de produtos (pode ser vazia)
     * @param restaurantes lista inicial de restaurantes (pode ser vazia)
     */
    public Menu(String nome, String descricao, List<Produto> produtos, List<Restaurante> restaurantes) {
        this.nome = nome;
        this.descricao = descricao;
        this.produtos = produtos != null ? new ArrayList<>(produtos) : new ArrayList<>();
        this.restaurantes = restaurantes != null ? new ArrayList<>(restaurantes) : new ArrayList<>();
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

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public List<Restaurante> getRestaurantes() {
        return restaurantes;
    }

    public void setRestaurantes(List<Restaurante> restaurantes) {
        this.restaurantes = restaurantes;
    }

    /**
     * Adiciona um produto a este menu, mantendo a consistência bidirecional.
     *
     * @param produto o produto a adicionar
     */
    public void addProduto(Produto produto) {
        this.produtos.add(produto);
        produto.getMenus().add(this);
    }

    /**
     * Remove um produto deste menu, mantendo a consistência bidirecional.
     *
     * @param produto o produto a remover
     */
    public void removeProduto(Produto produto) {
        this.produtos.remove(produto);
        produto.getMenus().remove(this);
    }
}
