package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.*;

import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pela gestão de pedidos na plataforma TascaEats.
 *
 * Coordena a criação de pedidos, as transições de estado e o cancelamento,
 * aplicando as regras de negócio definidas:
 * - Não é possível criar um pedido a um restaurante fechado.
 * - Não é possível adicionar um produto esgotado ou eliminado.
 * - O {@code precoCompra} de cada item é capturado no momento do pedido.
 * - Um pedido só pode ser cancelado nos estados {@code CREATED} ou
 * {@code PAID}.
 * - O controlo de concorrência otimista ({@code @Version}) é gerido pelo JPA.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     *
     * @param pedidoRepository      repositório de pedidos
     * @param clienteRepository     repositório de clientes
     * @param produtoRepository     repositório de produtos
     */
    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Cria um novo pedido multi-restaurante com os itens especificados.
     *
     * Regras aplicadas:
     * - Cada restaurante (via produto) deve estar aberto ({@code aberto = true}).
     * - O pedido deve ter pelo menos um item.
     * - Cada produto deve existir, estar disponível e não estar eliminado.
     * - A quantidade de cada item deve ser maior que zero.
     * - O preço total é calculado automaticamente a partir de
     *   {@code precoCompra × quantity} de cada item.
     * - Os produtos podem ser de diferentes restaurantes.
     *
     * @param clienteId       ID do cliente que efetua o pedido
     * @param enderecoEntrega morada de entrega para este pedido
     * @param itens           mapa de {@code produtoId → quantidade}
     * @return o pedido criado e persistido com estado {@code CREATED}
     * @throws RuntimeException         se o cliente ou algum produto não for encontrado
     * @throws IllegalStateException    se algum restaurante estiver fechado ou algum
     *                                  produto estiver esgotado/eliminado
     * @throws IllegalArgumentException se o mapa de itens estiver vazio ou alguma
     *                                  quantidade for inválida
     */
    @Transactional
    public Pedido criarPedido(Long clienteId, Endereco enderecoEntrega, Map<Long, Integer> itens) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: id=" + clienteId));

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um produto.");
        }

        Pedido pedido = new Pedido(cliente, enderecoEntrega);

        for (Map.Entry<Long, Integer> entry : itens.entrySet()) {
            Produto produto = produtoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: id=" + entry.getKey()));

            if (produto.isEliminado()) {
                throw new IllegalStateException("O produto '" + produto.getNome() + "' foi removido do menu.");
            }
            if (!produto.isDisponivel()) {
                throw new IllegalStateException("O produto '" + produto.getNome() + "' está esgotado.");
            }
            if (!produto.getRestaurante().isAberto()) {
                throw new IllegalStateException(
                        "O restaurante '" + produto.getRestaurante().getNome() + "' está fechado.");
            }
            if (entry.getValue() <= 0) {
                throw new IllegalArgumentException(
                        "A quantidade do produto '" + produto.getNome() + "' deve ser maior que zero.");
            }

            pedido.adicionarProduto(new ProdutoPedido(produto, entry.getValue()));
        }

        return pedidoRepository.save(pedido);
    }

    /**
     * Avança o estado do pedido para o seguinte no fluxo normal.
     *
     * Fluxo: {@code CREATED → PAID → PREPARING → READY → IN_DELIVERY → DELIVERED}.
     *
     * Nota: a transição {@code CREATED → PAID} é feita pelo
     * {@link PagamentoService#processarPagamento} quando o pagamento é processado.
     * Este método pode ser usado pelo Admin para avançar estados subsequentes.
     *
     * @param pedidoId ID do pedido a avançar
     * @return o pedido atualizado com o novo estado
     * @throws RuntimeException      se o pedido não for encontrado
     * @throws IllegalStateException se o estado atual não permitir avançar
     */
    @Transactional
    public Pedido avancarEstado(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.avancarEstado();
        return pedidoRepository.save(pedido);
    }

    /**
     * Cancela um pedido. Apenas possível nos estados {@code CREATED} ou
     * {@code PAID}.
     *
     * @param pedidoId ID do pedido a cancelar
     * @throws RuntimeException      se o pedido não for encontrado
     * @throws IllegalStateException se o estado atual não permitir cancelar (ex:
     *                               {@code PREPARING})
     */
    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.cancelar();
        pedidoRepository.save(pedido);
    }

    /**
     * Procura um pedido pelo seu ID.
     *
     * @param id ID do pedido
     * @return o pedido encontrado
     * @throws RuntimeException se o pedido não for encontrado
     */
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: id=" + id));
    }

    /**
     * Lista todos os pedidos de um determinado cliente, por ordem cronológica
     * decrescente.
     *
     * @param clienteId ID do cliente
     * @return lista de pedidos do cliente
     */
    public List<Pedido> buscarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataHoraDesc(clienteId);
    }
}
