package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;
import pt.ul.fc.css.tascaeats.dto.*;
import java.util.List;

/**
 * Controller REST responsável pela exposição dos endpoints de gestão de pedidos.
 * Permite a criação de novas encomendas, consulta de detalhes e histórico,
 * bem como a gestão do ciclo de vida (avanço de estado e cancelamento).
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Construtor para injeção de dependências do serviço de pedidos.
     * @param pedidoService O serviço que contém a lógica de negócio dos pedidos.
     */
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Cria um novo pedido na plataforma e devolve os dados formatados para resposta.
     * O processo valida se o restaurante está aberto e se os produtos estão disponíveis.
     * * @param request DTO contendo o ID do cliente, ID do restaurante, morada e mapa de itens.
     * @return ResponseEntity contendo o PedidoResponse e o status HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@RequestBody CriarPedidoRequest request) {
        Pedido novoPedido = pedidoService.criarPedido(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.from(novoPedido));
    }

    /**
     * Recupera os detalhes de um pedido específico através do seu identificador.
     * * @param id O identificador único do pedido.
     * @return ResponseEntity contendo o PedidoResponse e o status HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(PedidoResponse.from(pedido));
    }

    /**
     * Lista o histórico de pedidos de um cliente específico, ordenado por data decrescente.
     * * @param clienteId O identificador do cliente.
     * @return ResponseEntity contendo uma lista de PedidoResponse e o status HTTP 200 (OK).
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.buscarPorCliente(clienteId);
        
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::from)
                .toList();
                
        return ResponseEntity.ok(response);
    }

    /**
     * Avança o pedido para o próximo estado no fluxo de trabalho (ex: de PAID para PREPARING).
     * * @param id O identificador do pedido a transitar.
     * @return ResponseEntity contendo o PedidoResponse atualizado e o status HTTP 200 (OK).
     */
    @PatchMapping("/{id}/avancar")
    public ResponseEntity<PedidoResponse> avancarEstado(@PathVariable Long id) {
        Pedido pedidoAtualizado = pedidoService.avancarEstado(id);
        return ResponseEntity.ok(PedidoResponse.from(pedidoAtualizado));
    }

    /**
     * Cancela um pedido se este ainda não tiver entrado em fase de preparação.
     * A operação apenas é permitida se o pedido estiver nos estados CREATED ou PAID.
     * * @param id ID do pedido a cancelar.
     * @return ResponseEntity com status HTTP 204 (No Content) após sucesso.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }
}