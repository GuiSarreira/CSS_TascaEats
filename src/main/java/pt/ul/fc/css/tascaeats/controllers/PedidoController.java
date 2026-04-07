package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;
import pt.ul.fc.css.tascaeats.dto.*;
import java.util.List;

/**
 * Controller REST para gestão de Pedidos.
 * Fornece endpoints para criação, consulta e controlo do ciclo de vida das encomendas.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Cria um novo pedido na plataforma.
     * @param request Objeto contendo IDs do cliente/restaurante, morada e itens.
     * @return O pedido criado com status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequest request) {
        Pedido novoPedido = pedidoService.criarPedido(
                request.clienteId(),
                request.restauranteId(),
                request.enderecoEntrega(),
                request.itens()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
    }

    /**
     * Obtém os detalhes de um pedido específico.
     * @param id ID do pedido.
     * @return Detalhes do pedido.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    /**
     * Lista o histórico de pedidos de um cliente.
     * @param clienteId ID do cliente.
     * @return Lista de pedidos ordenados por data.
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.buscarPorCliente(clienteId));
    }

    /**
     * Lista todos os pedidos recebidos por um restaurante.
     * @param restauranteId ID do restaurante.
     * @return Lista de pedidos do restaurante.
     */
    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<List<Pedido>> listarPorRestaurante(@PathVariable Long restauranteId) {
        return ResponseEntity.ok(pedidoService.buscarPorRestaurante(restauranteId));
    }

    /**
     * Filtra pedidos por estado.
     * @param status Estado do pedido (CREATED, PAID, PREPARING, READY, etc).
     * @return Lista de pedidos no estado indicado.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pedido>> listarPorStatus(@PathVariable PedidoStatus status) {
        return ResponseEntity.ok(pedidoService.buscarPorStatus(status));
    }

    /**
     * Avança o pedido para o próximo estado no fluxo (PREPARING para READY).
     * @param id ID do pedido a avançar.
     * @return O pedido com o novo estado atualizado.
     */
    @PatchMapping("/{id}/avancar")
    public ResponseEntity<Pedido> avancarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.avancarEstado(id));
    }

    /**
     * Cancela um pedido (apenas permitido se estiver em CREATED ou PAID).
     * @param id ID do pedido a cancelar.
     * @return Status 204 No Content após sucesso.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }
}