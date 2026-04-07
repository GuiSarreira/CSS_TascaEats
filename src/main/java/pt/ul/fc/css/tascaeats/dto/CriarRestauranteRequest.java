package pt.ul.fc.css.tascaeats.dto;

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

    /** Morada do restaurante. */
    private String morada;

    /** Cidade onde o restaurante está localizado. */
    private String cidade;

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

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
