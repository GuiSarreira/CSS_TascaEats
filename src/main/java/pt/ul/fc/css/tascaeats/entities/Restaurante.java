package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um restaurante parceiro na plataforma TascaEats.
 *
 * Um restaurante é criado e gerido por um {@link Admin}. Tem um catálogo
 * de {@link Produto produtos} e pode receber {@link Pedido pedidos} quando
 * está {@code aberto = true}.
 *
 * Regras de negócio
 *   - O NIF deve ser único em toda a plataforma.
 *   - Apenas o admin dono do restaurante pode editá-lo.
 *   - Não é possível criar pedidos a restaurantes com {@code aberto = false}.
 */
@Entity
public class Restaurante {

    /** Identificador único do restaurante, gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do restaurante. Não pode ser nulo. */
    @NotNull(message = "O nome não pode ser nulo")
    @Column(nullable = false)
    private String nome;

    /** Número de Identificação Fiscal do restaurante. Único em toda a plataforma. */
    @NotNull(message = "O NIF é obrigatório")
    @Column(unique = true, nullable = false)
    private String nif;

    /** Indica se o restaurante está aberto para receber pedidos. */
    private boolean aberto = false;

    /** Morada física do restaurante. */
    @Column(nullable = false)
    private String morada;

    /** Cidade onde o restaurante está localizado. Usado para pesquisas por localização. */
    @Column(nullable = false)
    private String cidade;

    /**
     * Catálogo de produtos do restaurante.
     * Relação 1:N com {@link Produto}. Em cascata: ao eliminar o restaurante,
     * os seus produtos também são eliminados ({@code orphanRemoval = true}).
     */
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> menu = new ArrayList<>();

    /**
     * Administrador dono deste restaurante. Lado N da relação N:1 com {@link Admin}.
     * A chave estrangeira {@code admin_id} fica nesta tabela.
     */
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /**
     * Pedidos recebidos por este restaurante.
     * Lado inverso da relação — o dono é {@link Pedido#getRestaurante()}.
     */
    @OneToMany(mappedBy = "restaurante")
    private List<Pedido> pedidos = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA.*/
    protected Restaurante() {
    }

    /**
     * Cria um novo restaurante, fechado por defeito.
     *
     * @param nome    nome do restaurante
     * @param morada  morada física
     * @param cidade  cidade onde está localizado
     * @param nif     NIF único do restaurante
     */
    public Restaurante(String nome, String morada, String cidade, String nif) {
        this.nome = nome;
        this.morada = morada;
        this.cidade = cidade;
        this.nif = nif;
        this.aberto = false;
    }

    /**
     * Adiciona um produto ao catálogo deste restaurante, mantendo a consistência bidirecional.
     *
     * @param item o produto a adicionar
     */
    public void addMenuItem(Produto item) {
        this.menu.add(item);
        item.setRestaurante(this);
    }

    /**
     * Adiciona um pedido a este restaurante, mantendo a consistência bidirecional.
     *
     * @param pedido o pedido a associar
     */
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