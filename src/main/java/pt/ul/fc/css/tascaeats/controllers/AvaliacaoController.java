package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoRequest;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoResponse;
import pt.ul.fc.css.tascaeats.entities.Avaliacao;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;

import java.util.List;

/**
 * Controller REST para gestão de Avaliações.
 * Expõe endpoints para criação, listagem, atualização e remoção de avaliações
 * de restaurantes por clientes.
 */
@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    /**
     * Cria uma nova avaliação para um restaurante, validando que o pedido existe,
     * pertence ao cliente e está entregue.
     *
     * @param request DTO com clienteId, restauranteId, pedidoId, nota e comentário.
     * @return A avaliação criada com status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<AvaliacaoResponse> criar(@RequestBody AvaliacaoRequest request) {
        Avaliacao nova = avaliacaoService.criarAvaliacao(
                request.getClienteId(),
                request.getRestauranteId(),
                request.getPedidoId(),
                request.getNota(),
                request.getComentario()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AvaliacaoResponse.from(nova));
    }

    /**
     * Lista avaliações filtradas por restaurante ou por cliente.
     * Um dos parâmetros deve ser fornecido.
     *
     * @param restauranteId ID do restaurante (opcional).
     * @param clienteId     ID do cliente (opcional).
     * @return Lista de avaliações que satisfazem o critério.
     */
    @GetMapping
    public ResponseEntity<List<AvaliacaoResponse>> listar(
            @RequestParam(required = false) Long restauranteId,
            @RequestParam(required = false) Long clienteId) {
        List<Avaliacao> avaliacoes;
        if (restauranteId != null) {
            avaliacoes = avaliacaoService.obterAvaliacoesPorRestaurante(restauranteId);
        } else if (clienteId != null) {
            avaliacoes = avaliacaoService.obterAvaliacoesPorCliente(clienteId);
        } else {
            return ResponseEntity.badRequest().build();
        }
        List<AvaliacaoResponse> response = avaliacoes.stream()
                .map(AvaliacaoResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Devolve a média das notas de avaliação de um restaurante.
     *
     * @param restauranteId ID do restaurante.
     * @return Média das notas (0.0 se não existirem avaliações).
     */
    @GetMapping("/restaurante/{restauranteId}/media")
    public ResponseEntity<Double> mediaPorRestaurante(@PathVariable Long restauranteId) {
        Double media = avaliacaoService.mediaNotasRestaurante(restauranteId);
        return ResponseEntity.ok(media != null ? media : 0.0);
    }

    /**
     * Atualiza a nota e/ou comentário de uma avaliação existente.
     * Apenas o cliente criador pode atualizar.
     *
     * @param id        ID da avaliação.
     * @param clienteId ID do cliente que solicita a atualização.
     * @param request   DTO com a nova nota e comentário.
     * @return A avaliação atualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> atualizar(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestBody AvaliacaoRequest request) {
        Avaliacao atualizada = avaliacaoService.atualizarAvaliacao(
                id, request.getNota(), request.getComentario(), clienteId);
        return ResponseEntity.ok(AvaliacaoResponse.from(atualizada));
    }

    /**
     * Remove uma avaliação. Apenas o cliente criador ou um admin podem remover.
     *
     * @param id        ID da avaliação a remover.
     * @param clienteId ID do utilizador que solicita a remoção.
     * @param isAdmin   {@code true} se o utilizador for administrador.
     * @return Status 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        avaliacaoService.removerAvaliacao(id, clienteId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
