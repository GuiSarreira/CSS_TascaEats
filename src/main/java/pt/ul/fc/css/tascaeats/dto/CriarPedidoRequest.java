package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;
import java.util.Map;

/**
 * DTO de request para o endpoint {@code POST /api/pedidos}.
 *
 * Contém toda a informação necessária para criar um pedido multi-restaurante.
 * A morada de entrega pode ser fornecida de duas formas mutuamente exclusivas:
 * - {@code moradaIndex} — índice (0-based) na lista de moradas guardadas do
 *   cliente ({@code Cliente.moradas}); se presente, ignora {@code enderecoEntrega}.
 * - {@code enderecoEntrega} — nova morada fornecida diretamente no pedido.
 *
 * Exemplos de JSON:
 * Usando morada existente do cliente (índice 0):
 * { "clienteId": 1, "moradaIndex": 0, "itens": { "7": 2, "12": 1 } }
 *
 * Usando nova morada:
 * { "clienteId": 1,
 *   "enderecoEntrega": { "rua": "Rua das Flores, 10", "codigoPostal": "1200-123", "cidade": "Lisboa" },
 *   "itens": { "7": 2, "12": 1 } }
 * </pre>
 */
public class CriarPedidoRequest {

    /** ID do cliente que efetua o pedido. */
    private Long clienteId;

    /**
     * Índice (0-based) de uma morada já guardada no perfil do cliente.
     * Se fornecido, tem prioridade sobre {@code enderecoEntrega}.
     */
    private Integer moradaIndex;

    /**
     * Nova morada de entrega para este pedido.
     * Usada apenas se {@code moradaIndex} for {@code null}.
     */
    private Endereco enderecoEntrega;

    /**
     * Mapa de produtos e quantidades.
     * Chave: ID do produto; Valor: quantidade encomendada (deve ser maior que 0).
     * Os produtos podem pertencer a restaurantes diferentes (pedido multi-restaurante).
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

    public Integer getMoradaIndex() {
        return moradaIndex;
    }

    public void setMoradaIndex(Integer moradaIndex) {
        this.moradaIndex = moradaIndex;
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
