package pt.ul.fc.css.tascaeats.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pt.ul.fc.css.tascaeats.dto.PedidoPagoEvent;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.PagamentoRepository;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;

import java.util.List;
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
 * - Publica evento {@code pedido.pago} no Kafka para o microserviço de entregas.
 */
@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private static final String TOPIC_PEDIDO_PAGO = "pedido.pago";

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Construtor para injeção de dependências.
     *
     * @param pagamentoRepository repositório de pagamentos
     * @param pedidoRepository    repositório de pedidos
     * @param kafkaTemplate       template Kafka para publicar eventos
     * @param objectMapper        mapper JSON para serialização de eventos
     */
    public PagamentoService(PagamentoRepository pagamentoRepository,
                            PedidoRepository pedidoRepository,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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
     * Após o pagamento ser confirmado, publica um evento {@code pedido.pago}
     * no Kafka para que o microserviço de entregas inicie a atribuição automática.
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
    public Pagamento processarPagamento(Long pedidoId, String tipoPagamento, String dadosExtra, String bandeira, Double troco) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: id=" + pedidoId));

        if (pedido.getStatus() != PedidoStatus.CREATED) {
            throw new IllegalStateException(
                    "Só é possível pagar um pedido no estado CREATED. Estado atual: " + pedido.getStatus());
        }

        if (pagamentoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Este pedido já tem um pagamento associado.");
        }

        Pagamento pagamento = criarPagamento(pedido, tipoPagamento, dadosExtra, bandeira, troco);
        pagamento.processar(); // PENDING → COMPLETED
        pedido.avancarEstado(); // CREATED → PAID

        pedidoRepository.save(pedido);
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);

        // Publicar evento Kafka após commit da transação (garante que o pagamento foi persistido)
        publicarEventoPedidoPago(pedido);

        return pagamentoSalvo;
    }

    /**
     * Publica o evento {@code pedido.pago} no Kafka após o commit da transação.
     *
     * <p>Utiliza {@link TransactionSynchronization#afterCommit()} para garantir
     * que o evento só é publicado se a transação de BD tiver sucesso.
     * Isto evita publicar eventos para pagamentos que foram revertidos.
     *
     * @param pedido o pedido que acabou de ser pago
     */
    private void publicarEventoPedidoPago(Pedido pedido) {
        Endereco endereco = pedido.getEnderecoEntrega();
        String moradaCompleta = endereco.getRua() + ", " + endereco.getCodigoPostal();

        PedidoPagoEvent evento = new PedidoPagoEvent(
                pedido.getId(),
                moradaCompleta,
                endereco.getCidade(),
                pedido.getPrecoTotal()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    String json = objectMapper.writeValueAsString(evento);
                    kafkaTemplate.send(TOPIC_PEDIDO_PAGO, pedido.getId().toString(), json);
                    logger.info("Evento Kafka publicado no tópico '{}': {}", TOPIC_PEDIDO_PAGO, json);
                } catch (JsonProcessingException e) {
                    logger.error("Erro ao serializar PedidoPagoEvent para pedido {}: {}", pedido.getId(), e.getMessage());
                }
            }
        });
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
    private Pagamento criarPagamento(Pedido pedido, String tipoPagamento, String dadosExtra, String bandeira, Double troco) {
        return switch (tipoPagamento.toUpperCase()) {
            case "MULTIBANCO" -> new Multibanco(pedido, pedido.getPrecoTotal(), dadosExtra, bandeira);
            case "MBWAY" -> new MBWay(pedido, pedido.getPrecoTotal(), dadosExtra);
            case "DINHEIRO" -> new Dinheiro(pedido, pedido.getPrecoTotal(), troco);
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

    /**
     * Query de negócio — devolve a média do troco nos pagamentos a dinheiro concluídos.
     *
     * @return média do troco, ou {@code null} se não houver pagamentos a dinheiro concluídos
     */
    public Double calcularMediaTroco() {
        return pagamentoRepository.findMediaTroco();
    }

    /**
     * FASE 1 — Query 5: Método de pagamento mais utilizado.
     * Conta o número de vezes que cada método de pagamento foi usado.
     * @return lista de arrays [metodoPagamento, quantidade] ordenada por quantidade DESC
     */
    public List<Object[]> metodosPagamentoMaisUtilizados() {
        return pagamentoRepository.findMetodoPagamentoMaisUtilizado();
    }
}
