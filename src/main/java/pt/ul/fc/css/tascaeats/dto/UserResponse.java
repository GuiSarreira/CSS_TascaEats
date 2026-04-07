package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.User;
import pt.ul.fc.css.tascaeats.entities.UserTypes;

/**
 * DTO de response para dados de utilizador.
 *
 * Usado nas respostas de login ({@code POST /api/auth/login}) e nos
 * endpoints de gestão de utilizadores ({@code GET /api/users/{id}},
 * {@code GET /api/users}).
 *
 * Não expõe a password nem campos internos como {@code dataRegisto}.
 */
public class UserResponse {

    /** ID do utilizador. */
    private Long id;

    /** Nome completo do utilizador. */
    private String nome;

    /** Email do utilizador. */
    private String email;

    /** Papel do utilizador na plataforma. */
    private UserTypes role;

    /** Se a conta está ativa. */
    private boolean ativo;

    /** Construtor vazio para uso interno. */
    public UserResponse() {
    }

    private UserResponse(Long id, String nome, String email, UserTypes role, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = ativo;
    }

    /**
     * Cria um {@code UserResponse} a partir de uma entidade {@link User}.
     *
     * @param user entidade de utilizador
     * @return DTO preenchido com os campos do utilizador
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                user.isAtivo());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public UserTypes getRole() {
        return role;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
