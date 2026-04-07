package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.entities.User;
import pt.ul.fc.css.tascaeats.repositories.UserRepository;

/**
 * Serviço de autenticação mock da plataforma TascaEats.
 *
 * Implementa um login simplificado (mock auth): verifica se o utilizador
 * com o email fornecido existe e está ativo. A password não é validada
 * — qualquer valor é aceite para utilizadores existentes, conforme especificado
 * nos requisitos do projeto.
 *
 * O objeto {@link User} é devolvido diretamente para ser usado pelo controller.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Construtor para injeção de dependência do repositório de utilizadores.
     *
     * @param userRepository repositório de utilizadores
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Login mock: aceita qualquer password desde que o utilizador exista e esteja
     * ativo.
     *
     * Regras:
     * - O utilizador deve existir com o email fornecido.
     * - O utilizador deve estar ativo ({@code ativo = true}).
     * - A password é ignorada.
     *
     * @param email    email do utilizador
     * @param password password fornecida (não validada)
     * @return o utilizador autenticado
     * @throws RuntimeException se o utilizador não existir ou estiver inativo
     */
    public User login(String email, String password) {
        return userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas ou utilizador inativo."));
    }
}
