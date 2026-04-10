package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.*;

/**
 * DTO de response para dados de utilizador.
 *
 * Usado nas respostas de login ({@code POST /api/auth/login}) e nos
 * endpoints de gestão de utilizadores ({@code GET /api/users/{id}},
 * {@code GET /api/users}).
 *
 * Não expõe a password nem campos internos como {@code dataRegisto}.
 * Campos específicos de subtipo (morada para Cliente; veiculo/zonaAtuacao
 * para Entregador) são incluídos apenas quando aplicável — os restantes
 * ficam {@code null} e são omitidos pelo serializador JSON.
 */
public class UserResponse {

    /** ID do utilizador. */
    private Long id;

    /** Nome completo do utilizador. */
    private String nome;

    /** Email do utilizador. */
    private String email;

    /** Papel do utilizador na plataforma (CLIENTE, ADMIN ou ENTREGADOR). */
    private String role;

    /** Se a conta está ativa. */
    private boolean ativo;

    /** Morada principal — presente apenas para utilizadores do tipo CLIENTE. */
    private Endereco morada;

    /** Tipo de veículo — presente apenas para utilizadores do tipo ENTREGADOR. */
    private String veiculo;

    /** Zona geográfica de atuação — presente apenas para utilizadores do tipo ENTREGADOR. */
    private String zonaAtuacao;

    /** Construtor vazio para uso interno. */
    public UserResponse() {
    }

    /**
     * Cria um {@code UserResponse} a partir de uma entidade {@link User}.
     * Preenche automaticamente os campos específicos do subtipo concreto.
     *
     * @param user entidade de utilizador
     * @return DTO preenchido com os campos do utilizador
     */
    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.nome = user.getNome();
        r.email = user.getEmail();
        r.role = user.getClass().getSimpleName().toUpperCase();
        r.ativo = user.isAtivo();

        if (user instanceof Cliente c) {
            r.morada = c.getMorada();
        } else if (user instanceof Entregador e) {
            r.veiculo = e.getVeiculo();
            r.zonaAtuacao = e.getZonaAtuacao();
        }

        return r;
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

    public String getRole() {
        return role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Endereco getMorada() {
        return morada;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public String getZonaAtuacao() {
        return zonaAtuacao;
    }
}

