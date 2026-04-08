package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.LoginRequest;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.entities.User;
import pt.ul.fc.css.tascaeats.services.AuthService;

/**
 * Controller REST para autenticação na plataforma TascaEats.
 *
 * Expõe o endpoint de login mock: não valida a password, apenas verifica
 * que o utilizador existe e está ativo na base de dados.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Construtor para injeção de dependência do serviço de autenticação.
     *
     * @param authService serviço de autenticação
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica um utilizador com base no email fornecido.
     *
     * Mock auth: a password não é validada — qualquer valor é aceite
     * desde que o utilizador exista e esteja ativo ({@code ativo = true}).
     *
     * @param request DTO com {@code email} e {@code password}
     * @return {@link UserResponse} com os dados do utilizador autenticado e status 200 (OK)
     * @throws RuntimeException se o utilizador não existir ou estiver inativo (→ 404/500)
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
