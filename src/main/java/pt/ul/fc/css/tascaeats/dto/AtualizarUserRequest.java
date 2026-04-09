package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;

/**
 * DTO de request para o endpoint {@code PUT /api/users/{id}}.
 *
 * Contém os campos atualizáveis de um utilizador. Todos os campos são
 * opcionais — apenas os campos não nulos e não em branco são aplicados.
 * O campo {@code morada} só é aplicado a utilizadores do tipo {@code Cliente}.</p>
 *
 * Exemplo de JSON:
 *  {@code
 * {
 *   "nome": "Ana Silva Santos",
 *   "password": "novapass",
 *   "morada": "Avenida da Liberdade, 50, Lisboa"
 *  }
 * }
 */
public class AtualizarUserRequest {

    /**
     * Novo nome completo. Ignorado se {@code null} ou em branco.
     */
    private String nome;

    /**
     * Nova password. Ignorada se {@code null} ou em branco.
     */
    private String password;

    /**
     * Nova morada. Apenas aplicada a utilizadores do tipo {@code Cliente}.
     * Ignorada se {@code null}.
     */
    private Endereco morada;

    /** Construtor vazio exigido para deserialização do JSON. */
    public AtualizarUserRequest() {
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
