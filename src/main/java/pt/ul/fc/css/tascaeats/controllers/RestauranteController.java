package pt.ul.fc.css.tascaeats.controllers;

import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;
//import org.springframework.http.HttpStatus;
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
     * Retorna a lista de todos os restaurantes registados.
     * @return Lista de todos os restaurantes e status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Restaurante>> listarTodos() {
        return ResponseEntity.ok(restauranteService.listarTodos());
    }

    /**
     * Procura um restaurante pelo seu identificador único (ID).
     * @param id O ID do restaurante.
     * @return O restaurante encontrado ou status 404 caso ocorra erro no serviço.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurante> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(restauranteService.buscarPorId(id));
    }

    /**
     * Pesquisa restaurantes pelo nome.
     * @param nome Parte do nome a pesquisar.
     * @return Lista de restaurantes correspondentes.
     */
    @GetMapping("/nome")
    public ResponseEntity<List<Restaurante>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(restauranteService.buscarPorNome(nome));
    }

    /**
     * Pesquisa restaurantes por cidade.
     * @param cidade Nome da cidade.
     * @return Lista de restaurantes na cidade indicada.
     */
    @GetMapping("/cidade")
    public ResponseEntity<List<Restaurante>> buscarPorCidade(@RequestParam String cidade) {
        return ResponseEntity.ok(restauranteService.buscarPorCidade(cidade));
    }

    /**
     * Atualiza os dados de um restaurante existente através do seu NIF.
     * @param nif O NIF do restaurante a editar.
     * @param restaurante Objeto com os novos dados.
     * @return O restaurante atualizado.
     */
    @PutMapping("/{id")
    public ResponseEntity<Restaurante> atualizar(@PathVariable Long id, @RequestBody Restaurante restaurante) {
        return ResponseEntity.ok(restauranteService.atualizarRestaurante(id, restaurante));
    }

    /**
     * Altera o estado de abertura (Aberto/Fechado) do restaurante.
     * @param nif O NIF do restaurante.
     * @param aberto Boolean indicando o novo estado.
     * @return Status 204 (No Content) após sucesso.
     */
    @PatchMapping("/{id/estado")
    public ResponseEntity<Void> alterarEstado(@PathVariable Long id, @RequestParam boolean aberto) {
        restauranteService.alterarEstadoAbertura(id, aberto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove um restaurante do sistema através do NIF.
     * @param nif O NIF do restaurante.
     * @return Status 204 (No Content).
     */
    @DeleteMapping("/{id")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        restauranteService.removerRestaurante(id);
        return ResponseEntity.noContent().build();
    }
}