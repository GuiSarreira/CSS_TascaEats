package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint {@code POST /api/users/admins}.
 *
 * Contém os dados necessários para registar um novo administrador na plataforma.
 *
 * Exemplo de JSON:
 * {@code
 *  {
 *   "email": "admin@tascaeats.pt",
 *   "nome": "Carlos Admin",
 *   "password": "admin123"
 *  }
 * }
 */
public class RegistarAdminRequest {

    /** Endereço de email do administrador. Deve ser único na plataforma. */
    private String email;

    /** Nome completo do administrador. */
    private String nome;

    /** Password (mock auth — não é validada). */
    private String password;

    /** Construtor vazio exigido para deserialização do JSON. */
    public RegistarAdminRequest() {
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
}
