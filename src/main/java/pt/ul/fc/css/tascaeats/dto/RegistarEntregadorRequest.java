package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint {@code POST /api/users/entregadores}.
 *
 * Contém os dados necessários para registar um novo entregador na plataforma.
 *
 * Exemplo de JSON:
 * {@code
 *  {
 *   "email": "joao@email.pt",
 *   "nome": "João Entregador",
 *   "password": "pass123",
 *   "veiculo": "moto",
 *   "zonaAtuacao": "Lisboa"
 *  }
 * }
 */
public class RegistarEntregadorRequest {

    /** Endereço de email do entregador. Deve ser único na plataforma. */
    private String email;

    /** Nome completo do entregador. */
    private String nome;

    /** Password (mock auth — não é validada). */
    private String password;

    /** Tipo de veículo utilizado pelo entregador (ex: moto, bicicleta, carro). */
    private String veiculo;

    /** Zona geográfica onde o entregador opera (deve coincidir com a cidade dos restaurantes). */
    private String zonaAtuacao;

    /** Construtor vazio exigido para deserialização do JSON. */
    public RegistarEntregadorRequest() {
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

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    public String getZonaAtuacao() {
        return zonaAtuacao;
    }

    public void setZonaAtuacao(String zonaAtuacao) {
        this.zonaAtuacao = zonaAtuacao;
    }
}
