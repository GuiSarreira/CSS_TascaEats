package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pt.ul.fc.css.tascaeats.dto.*;

/**
 * Controller REST para gestão de Entregas.
 * 
 * AGORA: Delega todas as operações no microserviço de entregas via REST.
 * O monólito actua como um proxy, mantendo a mesma interface REST para os clientes.
 */
@RestController
@RequestMapping("/api")
public class EntregaController {

    private final RestTemplate restTemplate;
    private final String entregaServiceUrl;

    public EntregaController(RestTemplate restTemplate,
                             @Value("${entrega.service.url:http://entrega-service:8081}") String entregaServiceUrl) {
        this.restTemplate = restTemplate;
        this.entregaServiceUrl = entregaServiceUrl;
    }

    /**
     * Consulta uma entrega pelo seu ID.
     * Delega no microserviço via REST.
     */
    @GetMapping("/entregas/{entregaId}")
    public ResponseEntity<EntregaResponse> buscarPorId(@PathVariable Long entregaId) {
        String url = entregaServiceUrl + "/api/entregas/" + entregaId;
        EntregaResponse response = restTemplate.getForObject(url, EntregaResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Atribui um entregador a um pedido pronto para entrega.
     * Delega no microserviço via REST.
     */
    @PostMapping("/pedidos/{pedidoId}/entregar")
    public ResponseEntity<EntregaResponse> atribuirEntregador(
            @PathVariable Long pedidoId,
            @RequestBody AtribuirEntregadorRequest request) {
        String url = entregaServiceUrl + "/api/pedidos/" + pedidoId + "/entregar";
        EntregaResponse response = restTemplate.postForObject(url, request, EntregaResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Inicia uma entrega (muda de ATRIBUIDA para A_CAMINHO).
     * Delega no microserviço via REST.
     */
    @PatchMapping("/entregas/{entregaId}/iniciar")
    public ResponseEntity<EntregaResponse> iniciarEntrega(@PathVariable Long entregaId) {
        String url = entregaServiceUrl + "/api/entregas/" + entregaId + "/iniciar";
        EntregaResponse response = restTemplate.postForObject(url, null, EntregaResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Conclui uma entrega com sucesso.
     * Delega no microserviço via REST.
     */
    @PatchMapping("/entregas/{entregaId}/concluir")
    public ResponseEntity<EntregaResponse> concluirEntrega(@PathVariable Long entregaId) {
        String url = entregaServiceUrl + "/api/entregas/" + entregaId + "/concluir";
        EntregaResponse response = restTemplate.postForObject(url, null, EntregaResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancela uma entrega que ainda não foi iniciada.
     * Delega no microserviço via REST.
     */
    @PatchMapping("/entregas/{entregaId}/cancelar")
    public ResponseEntity<Void> cancelarEntrega(@PathVariable Long entregaId) {
        String url = entregaServiceUrl + "/api/entregas/" + entregaId + "/cancelar";
        restTemplate.postForEntity(url, null, Void.class);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta a entrega associada a um pedido.
     * Delega no microserviço via REST.
     */
    @GetMapping({ "/entregas/pedido/{pedidoId}", "/pedidos/{pedidoId}/entrega" })
    public ResponseEntity<EntregaResponse> buscarEntregaPorPedidoId(@PathVariable Long pedidoId) {
        String url = entregaServiceUrl + "/api/entregas/pedido/" + pedidoId;
        EntregaResponse response = restTemplate.getForObject(url, EntregaResponse.class);
        return ResponseEntity.ok(response);
    }
}