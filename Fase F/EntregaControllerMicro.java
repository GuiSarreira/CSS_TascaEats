package pt.ul.fc.css.entrega.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.entrega.dto.*;
import pt.ul.fc.css.entrega.entities.*;
import pt.ul.fc.css.entrega.services.EntregaService;

@RestController
@RequestMapping("/api")
public class EntregaControllerMicro {

    private final EntregaService entregaService;

    public EntregaControllerMicro(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping("/entregas/{entregaId}")
    public ResponseEntity<EntregaResponse> buscarPorId(@PathVariable Long entregaId) {
        Entrega entrega = entregaService.buscarPorId(entregaId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    @GetMapping("/entregas/pedido/{pedidoId}")
    public ResponseEntity<EntregaResponse> buscarPorPedidoId(@PathVariable Long pedidoId) {
        Entrega entrega = entregaService.buscarPorPedidoId(pedidoId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    @PostMapping("/pedidos/{pedidoId}/entregar")
    public ResponseEntity<EntregaResponse> atribuirEntregador(
            @PathVariable Long pedidoId,
            @RequestBody(required = false) Long entregadorId) {
        Entrega entrega;
        if (entregadorId == null) {
            entrega = entregaService.atribuirEntregadorAutomatico(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Não há entregadores disponíveis"));
        } else {
            entrega = entregaService.atribuirEntregador(pedidoId, entregadorId);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(EntregaResponse.from(entrega));
    }

    @PostMapping("/entregas/{entregaId}/iniciar")
    public ResponseEntity<EntregaResponse> iniciarEntrega(@PathVariable Long entregaId) {
        Entrega entrega = entregaService.iniciarEntrega(entregaId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    @PostMapping("/entregas/{entregaId}/concluir")
    public ResponseEntity<EntregaResponse> concluirEntrega(@PathVariable Long entregaId) {
        Entrega entrega = entregaService.concluirEntrega(entregaId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    @PostMapping("/entregas/{entregaId}/cancelar")
    public ResponseEntity<Void> cancelarEntrega(@PathVariable Long entregaId) {
        entregaService.cancelarEntrega(entregaId);
        return ResponseEntity.noContent().build();
    }
}