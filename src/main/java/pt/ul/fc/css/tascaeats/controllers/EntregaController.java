package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.EntregaService;

/**
 * Controller REST para gestão de Entregas.
 * Expõe os endpoints da API para atribuição e controlo do ciclo de vida das entregas.
 */
@RestController
@RequestMapping("/api")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    /**
     * Atribui um entregador a um pedido pronto para entrega.
     * Se entregadorId for null, a atribuição é automática.
     
     * @param pedidoId ID do pedido
     * @param request DTO com o ID do entregador 
     * @return A entrega criada
     */
    @PostMapping("/pedidos/{pedidoId}/entregar")
    public ResponseEntity<EntregaResponse> atribuirEntregador(
            @PathVariable Long pedidoId,
            @RequestBody AtribuirEntregadorRequest request) {
        Entrega entrega;
        if (request.getEntregadorId() == null) {
            entrega = entregaService.atribuirEntregadorAutomatico(pedidoId);
        } else {
            entrega = entregaService.atribuirEntregador(pedidoId, request.getEntregadorId());
        }
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    /**
     * Inicia uma entrega (muda de ATRIBUIDA para A_CAMINHO).
     *
     * @param entregaId ID da entrega
     * @return A entrega iniciada
     */
    @PatchMapping("/entregas/{entregaId}/iniciar")
    public ResponseEntity<EntregaResponse> iniciarEntrega(@PathVariable Long entregaId) {
        Entrega entrega = entregaService.iniciarEntrega(entregaId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    /**
     * Conclui uma entrega com sucesso.
     *
     * @param entregaId ID da entrega
     * @return A entrega concluída
     */
    @PatchMapping("/entregas/{entregaId}/concluir")
    public ResponseEntity<EntregaResponse> concluirEntrega(@PathVariable Long entregaId) {
        Entrega entrega = entregaService.concluirEntrega(entregaId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }

    /**
     * Cancela uma entrega que ainda não foi iniciada.
     *
     * @param entregaId ID da entrega
     * @return Status 204 (No Content)
     */
    @PatchMapping("/entregas/{entregaId}/cancelar")
    public ResponseEntity<Void> cancelarEntrega(@PathVariable Long entregaId) {
        entregaService.cancelarEntrega(entregaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta a entrega associada a um pedido.
     *
     * @param pedidoId ID do pedido
     * @return A entrega do pedido
     */
    @GetMapping("/pedidos/{pedidoId}/entrega")
    public ResponseEntity<EntregaResponse> buscarPorPedido(@PathVariable Long pedidoId) {
        Entrega entrega = entregaService.buscarPorPedidoId(pedidoId);
        return ResponseEntity.ok(EntregaResponse.from(entrega));
    }
}