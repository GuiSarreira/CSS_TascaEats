package pt.ul.fc.css.tascaeats.controllers;

import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller REST para gestão de Restaurantes.
 * Expõe os endpoints da API para interação com a camada de serviço.
 */
@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    /**
     * Cria um novo restaurante.
     *
     * @param request DTO com os dados do restaurante e o {@code adminId} do
     *                criador.
     * @return O restaurante criado com status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<RestauranteResponse> criar(@RequestBody CriarRestauranteRequest request) {
        Restaurante novoRestaurante = restauranteService.criarRestaurante(
            request.getNome(),
            request.getMorada(),
            request.getNif(),
            request.getTipoCozinha(),
            request.getHorarioAbertura(),
            request.getHorarioFecho(),
            request.getAdminId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(RestauranteResponse.from(novoRestaurante));
    }

    /**
     * Retorna a lista de todos os restaurantes registados.
     *
     * @return Lista de todos os restaurantes e status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<RestauranteResponse>> listarTodos() {
        List<RestauranteResponse> response = restauranteService.listarTodos().stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Procura um restaurante pelo seu identificador único (ID).
     *
     * @param id O ID do restaurante.
     * @return O restaurante encontrado ou status 404 caso não exista.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RestauranteResponse.from(restauranteService.buscarPorId(id)));
    }

    /**
     * Pesquisa restaurantes pelo nome.
     *
     * @param nome Parte do nome a pesquisar.
     * @return Lista de restaurantes correspondentes.
     */
    @GetMapping("/nome")
    public ResponseEntity<List<RestauranteResponse>> buscarPorNome(@RequestParam String nome) {
        List<RestauranteResponse> response = restauranteService.buscarPorNome(nome).stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Pesquisa restaurantes por cidade.
     *
     * @param cidade Nome da cidade.
     * @return Lista de restaurantes na cidade indicada.
     */
    @GetMapping("/cidade")
    public ResponseEntity<List<RestauranteResponse>> buscarPorCidade(@RequestParam String cidade) {
        List<RestauranteResponse> response = restauranteService.buscarPorCidade(cidade).stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Pesquisa restaurantes por tipo de cozinha.
     *
     * @param tipoCozinha Tipo de cozinha a pesquisar (ex: "Portuguesa", "Italiana").
     * @return Lista de restaurantes com o tipo de cozinha indicado.
     */
    @GetMapping("/tipo-cozinha")
    public ResponseEntity<List<RestauranteResponse>> buscarPorTipoCozinha(@RequestParam String tipoCozinha) {
        List<RestauranteResponse> response = restauranteService.buscarPorTipoCozinha(tipoCozinha).stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os restaurantes que estão atualmente abertos.
     *
     * @return Lista de restaurantes com estado aberto.
     */
    @GetMapping("/abertos")
    public ResponseEntity<List<RestauranteResponse>> listarAbertos() {
        List<RestauranteResponse> response = restauranteService.listarAbertos().stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Lista restaurantes aplicando filtros dinâmicos combinados.
     */
    @GetMapping("/filtros")
    public ResponseEntity<List<RestauranteResponse>> listarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoCozinha,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "HH:mm") java.time.LocalTime horario,
            @RequestParam(required = false) Double minPreco,
            @RequestParam(required = false) Double maxPreco,
            @RequestParam(required = false) Integer minAvaliacoes,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Integer minPedidos) {
        List<RestauranteResponse> response = restauranteService
                .listarRestaurantesComFiltros(nome, tipoCozinha, horario, minPreco, maxPreco, minAvaliacoes, cidade, minPedidos)
                .stream()
                .map(RestauranteResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um restaurante existente.
     *
     * @param id      ID do restaurante a editar.
     * @param request DTO com os novos dados (nome, morada, cidade).
     * @param adminId ID do administrador que solicita a edição (deve ser o dono do
     *                restaurante).
     * @return O restaurante atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponse> atualizar(@PathVariable Long id,
            @RequestBody CriarRestauranteRequest request, @RequestParam Long adminId) {
        return ResponseEntity.ok(
            RestauranteResponse.from(
                restauranteService.atualizarRestaurante(
                    id,
                    request.getNome(),
                    request.getMorada(),
                    adminId
                )));
    }

    /**
     * Altera o estado de abertura (Aberto/Fechado) do restaurante.
     *
     * @param id     ID do restaurante.
     * @param aberto Boolean indicando o novo estado.
     * @return Status 204 (No Content) após sucesso.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> alterarEstado(@PathVariable Long id, @RequestParam boolean aberto) {
        restauranteService.alterarEstadoAbertura(id, aberto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove um restaurante do sistema se este não tiver histórico de pedidos.
     *
     * @param id      ID do restaurante a remover.
     * @param adminId ID do administrador que solicita a remoção (deve ser o dono do
     *                restaurante).
     * @return Status 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestParam Long adminId) {
        restauranteService.removerRestaurante(id, adminId);
        return ResponseEntity.noContent().build();
    }
}