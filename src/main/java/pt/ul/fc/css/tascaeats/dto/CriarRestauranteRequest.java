package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;

/**
 * DTO de request para o endpoint {@code POST /api/restaurantes}.
 *
 * Contém os dados necessários para criar um novo restaurante.
 * O {@code adminId} identifica o administrador responsável — o serviço
 * valida que o utilizador tem papel {@code ADMIN} antes de criar.
 */
public class CriarRestauranteRequest {

    /** Nome do restaurante. */
    private String nome;

    /** NIF do restaurante (único na plataforma). */
    private String nif;

    /** Morada do restaurante (rua, código postal e cidade). */
    private Endereco morada;

    /** ID do administrador que cria o restaurante. */
    private Long adminId;

    /** Construtor vazio exigido para deserialização do JSON. */
    public CriarRestauranteRequest() {
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

    public void setNif(String nif) {
        this.nif = nif;
    }

    public Endereco getMorada() {
        return morada;
    }

    public void setMorada(Endereco morada) {
        this.morada = morada;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
