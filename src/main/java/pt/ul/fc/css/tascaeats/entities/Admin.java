package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um administrador da plataforma TascaEats.
 *
 * Subclasse de {@link User} com papel {@code "ADMIN"}. Um administrador
 * é responsável por criar e gerir restaurantes na plataforma.
 *
 * Herança — JOINED
 * Os atributos comuns ficam na tabela {@code users}. Esta classe não adiciona
 * colunas próprias — a tabela {@code admin} é criada apenas com a FK {@code user_id}.
 */
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    /**
     * Restaurantes geridos por este administrador.
     * Relação 1:N com {@link Restaurante}. Em cascata: ao eliminar o admin,
     * os seus restaurantes também são eliminados.
     */
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Restaurante> restaurantes = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA.*/
    protected Admin() {}

    /**
     * Cria um novo administrador.
     *
     * @param email    endereço de email; deve ser único
     * @param nome     nome completo
     * @param password password em plain text
     */
    public Admin(String email, String nome, String password) {
        super(email, nome, password);
    }

    public List<Restaurante> getRestaurantes() {
        return restaurantes;
    }

    public void setRestaurantes(List<Restaurante> restaurantes) {
        this.restaurantes = restaurantes;
    }

    /**
     * Adiciona um restaurante a este admin, mantendo a consistência bidirecional.
     *
     * @param restaurante o restaurante a associar
     */
    public void addRestaurante(Restaurante restaurante) {
        this.restaurantes.add(restaurante);
        restaurante.setAdmin(this);
    }
}