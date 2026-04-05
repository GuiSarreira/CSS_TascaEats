package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe abstrata que representa um utilizador da plataforma TascaEats.
 *
 * Classe base para a hierarquia de utilizadores. Cada subtipo concreto
 * ({@link Cliente}, {@link Admin}, {@link Entregador}) tem a sua própria tabela
 * na base de dados, ligada a esta por chave estrangeira ({@code user_id}).
 *
 * Herança — JOINED
 * A estrategia JOINED garante normalização: atributos comuns ficam na tabela
 * {@code users}; atributos específicos de cada subtipo ficam nas tabelas
 * {@code clientes}, {@code admins} e {@code entregadores}.
 *
 * Autenticação (mock)
 * A password é guardada em plain text por simplicidade (mock auth).
 * Em produção deveria ser guardada com hash (ex: BCrypt).
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public abstract class User {

    /** Identificador único gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Endereço de email do utilizador. Deve ser único em toda a plataforma. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Nome completo do utilizador. */
    @Column(nullable = false)
    private String nome;

    /** Password do utilizador. */
    @Column(nullable = false)
    private String password;

    /**
     * Data e hora de registo na plataforma.
     * Definida automaticamente no construtor e imutável após criação.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRegisto;

    /** Indica se a conta está ativa. {@code false} quando desativada (soft-delete de utilizador). */
    @Column(nullable = false)
    private boolean ativo = true;

    /** Papel do utilizador no sistema. Imutável após criação. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTypes role;

    /** Construtor protegido exigido pelo JPA. */
    protected User() {}

    /**
     * Cria um novo utilizador com os dados fornecidos.
     * A {@code dataRegisto} é preenchida automaticamente com o instante atual.
     *
     * @param email    endereço de email; deve ser único
     * @param nome     nome completo
     * @param password password em plain text
     * @param role     papel no sistema
     */
    public User(String email, String nome, String password, UserTypes role) {
        this.email = email;
        this.nome = nome;
        this.password = password;
        this.role = role;
        this.dataRegisto = LocalDateTime.now();
        this.ativo = true;
    }

    /** Desativa a conta do utilizador. A conta fica invisível mas o registo é preservado. */
    public void desativar() {
        this.ativo = false;
    }

    /** Reativa a conta do utilizador. */
    public void ativar() {
        this.ativo = true;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getDataRegisto() {
        return dataRegisto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public UserTypes getRole() {
        return role;
    }

    public void setRole(UserTypes role) {
        this.role = role;
    }
}