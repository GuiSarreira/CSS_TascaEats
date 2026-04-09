package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.UserService;
import java.util.List;

/**
 * Controller REST para gestão de Utilizadores.
 * Expõe os endpoints da API para registo, consulta, atualização e remoção
 * de Clientes, Administradores e Entregadores.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Regista um novo Cliente na plataforma.
     *
     * @param request DTO com os dados do cliente
     * @return O cliente registado com status 201 (Created)
     */
    @PostMapping("/clientes")
    public ResponseEntity<UserResponse> registarCliente(@RequestBody RegistarClienteRequest request) {
        Cliente cliente = userService.registarCliente(
                request.getEmail(), request.getNome(), request.getPassword(), request.getMorada());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(cliente));
    }

    /**
     * Regista um novo Administrador na plataforma.
     *
     * @param request DTO com os dados do administrador
     * @return O administrador registado com status 201 (Created)
     */
    @PostMapping("/admins")
    public ResponseEntity<UserResponse> registarAdmin(@RequestBody RegistarAdminRequest request) {
        Admin admin = userService.registarAdmin(request.getEmail(), request.getNome(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(admin));
    }

    /**
     * Regista um novo Entregador na plataforma.
     *
     * @param request DTO com os dados do entregador
     * @return O entregador registado com status 201 (Created)
     */
    @PostMapping("/entregadores")
    public ResponseEntity<UserResponse> registarEntregador(@RequestBody RegistarEntregadorRequest request) {
        Entregador entregador = userService.registarEntregador(
                request.getEmail(), request.getNome(), request.getPassword(),
                request.getVeiculo(), request.getZonaAtuacao());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(entregador));
    }

    /**
     * Retorna a lista de todos os utilizadores ativos.
     *
     * @return Lista de utilizadores ativos
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listarTodos() {
        return ResponseEntity.ok(userService.listarTodosAtivos().stream().map(UserResponse::from).toList());
    }

    /**
     * Procura um utilizador pelo seu ID.
     *
     * @param id O ID do utilizador
     * @return O utilizador encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.buscarPorId(id)));
    }

    /**
     * Procura um utilizador pelo seu email.
     *
     * @param email O email do utilizador
     * @return O utilizador encontrado
     */
    @GetMapping("/email")
    public ResponseEntity<UserResponse> buscarPorEmail(@RequestParam String email) {
        User user = userService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado: " + email));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Atualiza os dados de um utilizador existente.
     *
     * @param id      ID do utilizador a editar
     * @param request DTO com os novos dados
     * @return O utilizador atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> atualizar(@PathVariable Long id, @RequestBody AtualizarUserRequest request) {
        User user = userService.atualizarUser(id, request.getNome(), request.getPassword(), request.getMorada());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Remove (soft-delete) um utilizador do sistema.
     *
     * @param id ID do utilizador a remover
     * @return Status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        userService.removerUser(id);
        return ResponseEntity.noContent().build();
    }
}