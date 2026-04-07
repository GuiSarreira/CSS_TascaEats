package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.PagamentoRequest;
import pt.ul.fc.css.tascaeats.dto.PagamentoResponse;
import pt.ul.fc.css.tascaeats.entities.Pagamento;
import pt.ul.fc.css.tascaeats.services.PagamentoService;

/**
 * Controller REST para gestão de Pagamentos.
 * Fornece endpoints para processamento e consulta de pagamentos associados a pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    /**
     * Processa o pagamento de um pedido.
     * @param pedidoId ID do pedido a pagar.
     * @param request Dados do pagamento (tipo e dados extra).
     * @return O pagamento processado com status 200 (OK).
     */
    @PostMapping("/{pedidoId}/pagamento")
    public ResponseEntity<PagamentoResponse> processarPagamento(
            @PathVariable Long pedidoId,
            @RequestBody PagamentoRequest request) {
        Pagamento pagamento = pagamentoService.processarPagamento(
                pedidoId,
                request.getTipoPagamento(),
                request.getDadosExtra()
        );
        return ResponseEntity.ok(PagamentoResponse.from(pagamento));
    }

    /**
     * Consulta o pagamento associado a um pedido.
     * @param pedidoId ID do pedido.
     * @return O pagamento do pedido, se existir.
     */
    @GetMapping("/{pedidoId}/pagamento")
    public ResponseEntity<PagamentoResponse> buscarPagamentoPorPedido(@PathVariable Long pedidoId) {
        Pagamento pagamento = pagamentoService.buscarPorPedido(pedidoId)
                .orElseThrow(() -> new RuntimeException("Não existe pagamento para o pedido: " + pedidoId));
        return ResponseEntity.ok(PagamentoResponse.from(pagamento));
    }
}