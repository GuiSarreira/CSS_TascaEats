package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;
import java.util.Map;

/**
 * DTO de request para o endpoint {@code POST /api/pedidos}.
 *
 * Contém toda a informação necessária para criar um pedido:
 * - {@code clienteId} — quem está a fazer o pedido
 * - {@code restauranteId} — a qual restaurante
 * - {@code enderecoEntrega} — onde entregar
 * - {@code itens} — mapa de {@code produtoId → quantidade}
 *
 * Exemplo de JSON:
 * {@code
 * {
 * "clienteId": 1,
 * "restauranteId": 3,
 * "enderecoEntrega": { "rua": "Rua das Flores, 10", "codigoPostal": "1200-123",
 * "cidade": "Lisboa" },
 * "itens": { "7": 2, "12": 1 }
 * }
 * }
 */
public class CriarPedidoRequest {

    /** ID do cliente que efetua o pedido. */
    private Long clienteId;

    /** ID do restaurante ao qual o pedido é dirigido. */
    private Long restauranteId;

    /** Morada de entrega para este pedido específico. */
    private Endereco enderecoEntrega;

    /**
     * Mapa de produtos e quantidades.
     * Chave: ID do produto; Valor: quantidade encomendada (deve ser maior que 0).
     */
    private Map<Long, Integer> itens;

    /** Construtor vazio exigido para deserialização do JSON. */
    public CriarPedidoRequest() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(Endereco enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public Map<Long, Integer> getItens() {
        return itens;
    }

    public void setItens(Map<Long, Integer> itens) {
        this.itens = itens;
    }
}
