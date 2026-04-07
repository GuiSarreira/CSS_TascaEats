package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.PedidoStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de response para dados completos de um pedido.
 *
 * Usado nos endpoints de criação ({@code POST /api/pedidos}),
 * detalhe e listagem de pedidos.
 *
 * Inclui os itens do pedido como lista de {@link ItemPedidoResponse},
 * sem expor grafos inteiros de entidades JPA (evita serialização infinita
 * e dados desnecessários).
 */
public class PedidoResponse {

    /** ID do pedido. */
    private Long id;

    /** Data e hora de criação do pedido. */
    private LocalDateTime dataHora;

    /** Valor total calculado (soma de precoCompra × quantidade de cada item). */
    private Double precoTotal;

    /** Morada de entrega para este pedido. */
    private String enderecoEntrega;

    /** Estado atual do pedido no seu ciclo de vida. */
    private PedidoStatus status;

    /** ID do cliente que efetuou o pedido. */
    private Long clienteId;

    /** Nome do cliente que efetuou o pedido. */
    private String clienteNome;

    /** ID do restaurante ao qual o pedido foi feito. */
    private Long restauranteId;

    /** Nome do restaurante ao qual o pedido foi feito. */
    private String restauranteNome;

    /** Lista de itens que compõem o pedido. */
    private List<ItemPedidoResponse> itens;

    /** Construtor vazio para uso interno. */
    public PedidoResponse() {
    }

    private PedidoResponse(Long id, LocalDateTime dataHora, Double precoTotal, String enderecoEntrega,
            PedidoStatus status, Long clienteId, String clienteNome,
            Long restauranteId, String restauranteNome, List<ItemPedidoResponse> itens) {
        this.id = id;
        this.dataHora = dataHora;
        this.precoTotal = precoTotal;
        this.enderecoEntrega = enderecoEntrega;
        this.status = status;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.restauranteId = restauranteId;
        this.restauranteNome = restauranteNome;
        this.itens = itens;
    }

    /**
     * Cria um {@code PedidoResponse} a partir de uma entidade {@link Pedido}.
     *
     * @param p entidade pedido (com clientes, restaurante e itens carregados)
     * @return DTO preenchido com os campos do pedido
     */
    public static PedidoResponse from(Pedido p) {
        List<ItemPedidoResponse> itens = p.getProdutosPedido().stream()
                .map(ItemPedidoResponse::from)
                .toList();

        return new PedidoResponse(
                p.getId(),
                p.getDataHora(),
                p.getPrecoTotal(),
                p.getEnderecoEntrega(),
                p.getStatus(),
                p.getCliente().getId(),
                p.getCliente().getNome(),
                p.getRestaurante().getId(),
                p.getRestaurante().getNome(),
                itens);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Double getPrecoTotal() {
        return precoTotal;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public PedidoStatus getStatus() {
        return status;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public String getRestauranteNome() {
        return restauranteNome;
    }

    public List<ItemPedidoResponse> getItens() {
        return itens;
    }
}
