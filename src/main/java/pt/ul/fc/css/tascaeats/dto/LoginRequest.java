package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint de autenticação {@code POST /api/auth/login}.
 *
 * Contém as credenciais fornecidas pelo cliente. Num mock auth, a
 * password não é validada — apenas o email e o estado {@code ativo} do
 * utilizador são verificados.
 */
public class LoginRequest {

    /** Email do utilizador. */
    private String email;

    /** Password fornecida (não validada no mock auth). */
    private String password;

    /** Construtor vazio exigido para deserialização do JSON. */
    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
