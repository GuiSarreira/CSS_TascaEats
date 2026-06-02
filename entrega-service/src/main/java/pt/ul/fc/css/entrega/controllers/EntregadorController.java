package pt.ul.fc.css.entrega.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.entrega.entities.Entregador;
import pt.ul.fc.css.entrega.services.EntregadorService;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller para gestão de entregadores no microserviço.
 *
 * Base path: /api/entregadores
 *
 * Endpoints:
 *   GET    /api/entregadores                       — lista todos; aceita ?zona=X e/ou ?disponivel=true/false
 *   GET    /api/entregadores/{id}                  — obtém um entregador por ID
 *   POST   /api/entregadores                       — cria novo entregador
 *   PUT    /api/entregadores/{id}                  — atualiza nome, veiculo, zona e disponibilidade
 *   PATCH  /api/entregadores/{id}/disponibilidade  — altera apenas disponibilidade (?disponivel=true/false)
 *   DELETE /api/entregadores/{id}                  — remove entregador
 */
@RestController
@RequestMapping("/api/entregadores")
public class EntregadorController {

    private final EntregadorService entregadorService;

    public EntregadorController(EntregadorService entregadorService) {
        this.entregadorService = entregadorService;
    }

    @GetMapping
    public List<Entregador> listar(
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) Boolean disponivel) {
        return entregadorService.listar(zona, disponivel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entregador> obter(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(entregadorService.buscarPorId(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Entregador entregador) {
        try {
            Entregador criado = entregadorService.criar(entregador);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Entregador dados) {
        try {
            return ResponseEntity.ok(entregadorService.atualizar(id, dados));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<?> atualizarDisponibilidade(
            @PathVariable Long id,
            @RequestParam boolean disponivel) {
        try {
            entregadorService.atualizarDisponibilidade(id, disponivel);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        try {
            entregadorService.remover(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não é possível remover: o entregador tem entregas associadas.");
        }
    }
}
