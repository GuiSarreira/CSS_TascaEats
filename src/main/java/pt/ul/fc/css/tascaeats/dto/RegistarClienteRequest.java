package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;

/**
 * DTO de request para o endpoint {@code POST /api/users/clientes}.
 *
 * Contém os dados necessários para registar um novo cliente na plataforma.
 *
 * Exemplo de JSON:
 * {@code
 *  {
 *   "email": "ana@email.pt",
 *   "nome": "Ana Silva",
 *   "password": "123456",
 *   "morada": "Rua das Flores, 10, Lisboa"
 *  }
 * }
 */
public class RegistarClienteRequest {

    /** Endereço de email do cliente. Deve ser único na plataforma. */
    private String email;

    /** Nome completo do cliente. */
    private String nome;

    /** Password (mock auth — não é validada). */
    private String password;

    /** Morada de entrega por omissão do cliente. */
    private Endereco morada;

    /** Construtor vazio exigido para deserialização do JSON. */
    public RegistarClienteRequest() {
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

    public Endereco getMorada() {
        return morada;
    }

    public void setMorada(Endereco morada) {
        this.morada = morada;
    }
}
