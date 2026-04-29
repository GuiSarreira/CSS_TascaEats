package pt.ul.fc.css.tascaeats.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
public class Menu {

    /** Identificador único do menu, gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do menu. */
    @Column(name = "nome")
    private String nome;

    /**
     * Produtos do menu.
     */
    @ManyToMany
    @JoinTable(
            name = "menu_produto",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private List<Produto> produtos = new ArrayList<>();

    /**
     * Restaurantes com este menu.
     */
    @OneToMany(mappedBy = "menuPartilhado")
    private List<Restaurante> restaurantes = new ArrayList<>();

    /** Descrição do menu. */
    @Column(name = "descricao")
    private String descricao;

    /** Construtor protegido exigido pelo JPA. */
    protected Menu() {
    }

    /**
     * Cria um novo menu.
     *
     * @param nome         nome do menu
     * @param descricao    descrição do menu
     * @param produtos     lista de produtos do menu
     * @param restaurantes lista de restaurantes com este menu
     */
    public Menu(String nome, String descricao, List<Produto> produtos, List<Restaurante> restaurantes) {
        this.nome = nome;
        this.descricao = descricao;
        this.produtos = produtos;
        this.restaurantes = restaurantes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
     * Adiciona um produto ao menu.
     *
     * @param produto o produto a adicionar
     */
    public void addProduto(Produto produto) {
        this.produtos.add(produto);
    }

    /**
     * Remove um produto do menu.
     *
     * @param produto o produto a remover
     */
    public void removeProduto(Produto produto) {
        this.produtos.remove(produto);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna um produto do menu.
     *
     * @param produto o produto a procurar
     * @return o produto se existir no menu, null caso contrário
     */
    public Produto getProduto(Produto produto) {
        if (produtos.contains(produto)) {
            return produto;
        }
        return null;
    }

}