package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
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
 * - O NIF deve ser único em toda a plataforma.
 * - Apenas o admin dono do restaurante pode editá-lo.
 * - Não é possível criar pedidos a restaurantes com {@code aberto = false}.
 */
@Entity
public class Restaurante {

    /**
     * Identificador único do restaurante, gerado automaticamente pela base de
     * dados.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do restaurante. Não pode ser nulo. */
    @NotNull(message = "O nome não pode ser nulo")
    @Column(nullable = false)
    private String nome;

    /**
     * Número de Identificação Fiscal do restaurante. Único em toda a plataforma.
     */
    @NotNull(message = "O NIF é obrigatório")
    @Column(unique = true, nullable = false)
    private String nif;

    /** Indica se o restaurante está aberto para receber pedidos. */
    private boolean aberto = false;

    /** Morada física do restaurante (rua, código postal e cidade). */
    @Embedded
    private Endereco morada;

    /** Tipo de cozinha do restaurante (ex: Portuguesa, Italiana). */
    @Column
    private String tipoCozinha;

    /** Horário de abertura do restaurante. */
    @Column
    private LocalTime horarioAbertura;

    /** Horário de fecho do restaurante. */
    @Column
    private LocalTime horarioFecho;

    /**
     * Menu exclusivo deste restaurante.
     * Relação 1:1 com {@link Menu}. Em cascata: ao eliminar o restaurante,
     * o seu menu também é eliminado ({@code orphanRemoval = true}).
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "menu_exclusivo_id")
    private Menu menu;

    /**
     * Menu partilhado associado a este restaurante.
     * Lado inverso da relação 1:N — o dono é {@link Menu#getRestaurantes()}.
     */
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menuPartilhado;

    /**
     * Avaliações recebidas por este restaurante.
     * Lado inverso da relação — o dono é {@link Avaliacao#getRestaurante()}.
     */
    @OneToMany(mappedBy = "restaurante")
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    /**
     * Administrador dono deste restaurante. Lado N da relação N:1 com
     * {@link Admin}.
     * A chave estrangeira {@code admin_id} fica nesta tabela.
     */
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    /** Construtor protegido exigido pelo JPA. */
    protected Restaurante() {
    }

    /**
     * Cria um novo restaurante, fechado por defeito.
     *
     * @param nome   nome do restaurante
     * @param morada morada física
     * @param nif    NIF único do restaurante
     */
    public Restaurante(String nome, Endereco morada, String nif) {
        this.nome = nome;
        this.morada = morada;
        this.nif = nif;
        this.aberto = false;
    }

    /**
     * Cria um novo restaurante com informação de horários e tipo de cozinha,
     * fechado por defeito.
     *
     * @param nome            nome do restaurante
     * @param morada          morada física
     * @param nif             NIF único do restaurante
     * @param tipoCozinha     tipo de cozinha (ex: Portuguesa, Italiana)
     * @param horarioAbertura horário de abertura
     * @param horarioFecho    horário de fecho
     */
    public Restaurante(String nome, Endereco morada, String nif,
            String tipoCozinha, LocalTime horarioAbertura, LocalTime horarioFecho) {
        this.nome = nome;
        this.morada = morada;
        this.nif = nif;
        this.tipoCozinha = tipoCozinha;
        this.horarioAbertura = horarioAbertura;
        this.horarioFecho = horarioFecho;
        this.aberto = false;
    }

    /**
     * Adiciona um produto ao menu exclusivo deste restaurante.
     * Se o menu ainda não existir, cria um novo.
     *
     * @param item o produto a adicionar
     */
    public void addMenuItem(Produto item) {
        if (this.menu != null) {
            this.menu.addProduto(item);
        }
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

    public Endereco getMorada() {
        return morada;
    }

    public void setMorada(Endereco morada) {
        this.morada = morada;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getTipoCozinha() {
        return tipoCozinha;
    }

    public void setTipoCozinha(String tipoCozinha) {
        this.tipoCozinha = tipoCozinha;
    }

    public LocalTime getHorarioAbertura() {
        return horarioAbertura;
    }

    public void setHorarioAbertura(LocalTime horarioAbertura) {
        this.horarioAbertura = horarioAbertura;
    }

    public LocalTime getHorarioFecho() {
        return horarioFecho;
    }

    public void setHorarioFecho(LocalTime horarioFecho) {
        this.horarioFecho = horarioFecho;
    }

    public Menu getMenuPartilhado() {
        return menuPartilhado;
    }

    public void setMenuPartilhado(Menu menuPartilhado) {
        this.menuPartilhado = menuPartilhado;
    }

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

}