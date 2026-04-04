package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column
    private String descricao;

    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private boolean disponivel = true;

    @Column(nullable = false)
    private boolean eliminado = false;

    // Relação N:1 - Muitos produtos pertencem a um Restaurante
    @ManyToOne
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    // Relação 1:N - Um produto pode estar em várias linhas de pedidos diferentes
    @OneToMany(mappedBy = "produto")
    private List<ProdutoPedido> itensPedido = new ArrayList<>();

    protected Produto() {
    }

    public Produto(String nome, String descricao, Double preco, Restaurante restaurante) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.restaurante = restaurante;
        this.disponivel = true;
        this.eliminado = false;
    }

    /**
     * Implementação do Soft-Delete.
     * Altera o estado mas mantém o registo na Base de Dados.
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