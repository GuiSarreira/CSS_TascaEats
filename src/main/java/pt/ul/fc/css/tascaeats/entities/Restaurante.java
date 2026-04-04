package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome não pode ser nulo")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "O NIF é obrigatório")
    @Column(unique = true, nullable = false)
    private String nif;

    private boolean aberto = false;

    @Column(nullable = false)
    private String morada;

    @Column(nullable = false)
    private String cidade;

    // Relação 1-N: Um restaurante tem vários itens no menu
    // mappedBy indica que o campo 'restaurant' na classe MenuItem é o dono da
    // relação
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> menu = new ArrayList<>();

    // O 'JoinColumn' indica que a chave estrangeira (FK) estará na tabela
    // RESTAURANT
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "restaurante")
    private List<Pedido> pedidos = new ArrayList<>();

    // Construtor protegido para o JPA
    protected Restaurante() {
    }

    public Restaurante(String nome, String morada, String cidade, String nif) {
        this.nome = nome;
        this.morada = morada;
        this.cidade = cidade;
        this.nif = nif;
        this.aberto = false;
    }

    public void addMenuItem(Produto item) {
        this.menu.add(item);
        item.setRestaurante(this);
    }

    public void addPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        pedido.setRestaurante(this);
    }

    public List<Pedido> getPedidos() {
        return pedidos;
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

    public String getNif() {
        return nif;
    }

    public boolean isAberto() {
        return aberto;
    }

    public void setAberto(boolean aberto) {
        this.aberto = aberto;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public List<Produto> getMenu() {
        return menu;
    }

    public void setMenu(List<Produto> menu) {
        this.menu = menu;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

}