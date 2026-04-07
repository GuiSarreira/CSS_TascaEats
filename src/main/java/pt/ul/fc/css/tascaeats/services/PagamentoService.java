package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.PagamentoRepository;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;

import java.util.Optional;

/**
 * Serviço responsável pelo processamento de pagamentos na plataforma TascaEats.
 *
 * Coordena a criação e processamento do pagamento associado a um pedido,
 * aplicando as regras de negócio definidas:
 * - O pedido deve estar no estado {@code CREATED} para poder ser pago.
 * - Um pedido só pode ter um único pagamento.
 * - Tipos suportados: {@code MULTIBANCO}, {@code MBWAY}, {@code DINHEIRO}.
 * - Após processamento bem-sucedido, o pedido avança automaticamente para
 * {@code PAID}.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Construtor para injeção de dependências.
     *
     * @param pagamentoRepository repositório de pagamentos
     * @param pedidoRepository    repositório de pedidos
     */
    public PagamentoService(PagamentoRepository pagamentoRepository, PedidoRepository pedidoRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Processa o pagamento de um pedido e avança o seu estado para {@code PAID}.
     *
     * Regras aplicadas:
     * - O pedido deve existir.
     * - O pedido deve estar no estado {@code CREATED}.
     * - O pedido não pode já ter um pagamento associado.
     * - O tipo de pagamento deve ser {@code MULTIBANCO}, {@code MBWAY} ou
     * {@code DINHEIRO}.
     *
     * O montante do pagamento é extraído automaticamente do {@code precoTotal} do
     * pedido.
     *
     * @param pedidoId      ID do pedido a pagar
     * @param tipoPagamento tipo de pagamento: {@code "MULTIBANCO"}, {@code "MBWAY"}
     *                      ou {@code "DINHEIRO"}
     * @param dadosExtra    referência Multibanco ou telemóvel MB WAY; {@code null}
     *                      para Dinheiro
     * @return o pagamento criado e persistido com estado {@code COMPLETED}
     * @throws RuntimeException         se o pedido não for encontrado
     * @throws IllegalStateException    se o pedido não estiver em {@code CREATED}
     *                                  ou já tiver pagamento
     * @throws IllegalArgumentException se o tipo de pagamento for inválido
     */
    @Transactional
    public Pagamento processarPagamento(Long pedidoId, String tipoPagamento, String dadosExtra) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: id=" + pedidoId));

        if (pedido.getStatus() != PedidoStatus.CREATED) {
            throw new IllegalStateException(
                    "Só é possível pagar um pedido no estado CREATED. Estado atual: " + pedido.getStatus());
        }

        if (pagamentoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Este pedido já tem um pagamento associado.");
        }

        Pagamento pagamento = criarPagamento(pedido, tipoPagamento, dadosExtra);
        pagamento.processar(); // PENDING → COMPLETED
        pedido.avancarEstado(); // CREATED → PAID

        pedidoRepository.save(pedido);
        return pagamentoRepository.save(pagamento);
    }

    /**
     * Instancia o subtipo concreto de {@link Pagamento} com base no tipo indicado.
     *
     * @param pedido        pedido associado ao pagamento
     * @param tipoPagamento {@code "MULTIBANCO"}, {@code "MBWAY"} ou
     *                      {@code "DINHEIRO"}
     * @param dadosExtra    dado específico do tipo (referência ou telemóvel);
     *                      ignorado para Dinheiro
     * @return instância concreta de {@link Pagamento}
     * @throws IllegalArgumentException se o tipo não for reconhecido
     */
    private Pagamento criarPagamento(Pedido pedido, String tipoPagamento, String dadosExtra) {
        return switch (tipoPagamento.toUpperCase()) {
            case "MULTIBANCO" -> new Multibanco(pedido, pedido.getPrecoTotal(), dadosExtra);
            case "MBWAY" -> new MBWay(pedido, pedido.getPrecoTotal(), dadosExtra);
            case "DINHEIRO" -> new Dinheiro(pedido, pedido.getPrecoTotal());
            default -> throw new IllegalArgumentException("Tipo de pagamento inválido: " + tipoPagamento
                    + ". Valores aceites: MULTIBANCO, MBWAY, DINHEIRO.");
        };
    }

    /**
     * Devolve o pagamento associado a um pedido, se existir.
     *
     * @param pedidoId ID do pedido
     * @return {@link Optional} com o pagamento, ou vazio se o pedido ainda não foi
     *         pago
     */
    public Optional<Pagamento> buscarPorPedido(Long pedidoId) {
        return pagamentoRepository.findByPedidoId(pedidoId);
    }
}
