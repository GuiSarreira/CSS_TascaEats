package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoRequest;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoResponse;
import pt.ul.fc.css.tascaeats.entities.Avaliacao;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

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

    @GetMapping("/restaurante/{restauranteId}/media")
    public ResponseEntity<Double> mediaPorRestaurante(@PathVariable Long restauranteId) {
        Double media = avaliacaoService.mediaNotasRestaurante(restauranteId);
        return ResponseEntity.ok(media != null ? media : 0.0);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> atualizar(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestBody AvaliacaoRequest request) {
        Avaliacao atualizada = avaliacaoService.atualizarAvaliacao(
                id, request.getNota(), request.getComentario(), clienteId);
        return ResponseEntity.ok(AvaliacaoResponse.from(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        avaliacaoService.removerAvaliacao(id, clienteId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}