package pt.ul.fc.css.entrega.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.entrega.entities.*;
import pt.ul.fc.css.entrega.kafka.EntregaEventProducer;
import pt.ul.fc.css.entrega.repositories.EntregaRepository;
import pt.ul.fc.css.entrega.repositories.EntregadorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EntregaService {

    private static final Logger logger = LoggerFactory.getLogger(EntregaService.class);

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;
    private final EntregaEventProducer eventProducer;

    public EntregaService(EntregaRepository entregaRepository,
                          EntregadorRepository entregadorRepository,
                          EntregaEventProducer eventProducer) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
        this.eventProducer = eventProducer;
    }

    public Entrega buscarPorId(Long id) {
        return entregaRepository.findByIdWithEntregador(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + id));
    }

    public Entrega buscarPorPedidoId(Long pedidoId) {
        return entregaRepository.findByPedidoIdWithEntregador(pedidoId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada para pedido: " + pedidoId));
    }

    @Transactional
    public Optional<Entrega> atribuirEntregadorAutomatico(Long pedidoId) {
        Entregador entregador = entregadorRepository.findFirstByDisponivelTrue().orElse(null);
        if (entregador == null) return Optional.empty();
        entregador.setDisponivel(false);
        entregadorRepository.save(entregador);
        Entrega entrega = new Entrega(pedidoId, entregador, "N/A");
        return Optional.of(entregaRepository.save(entrega));
    }

    @Transactional
    public Entrega atribuirEntregador(Long pedidoId, Long entregadorId) {
        Entregador entregador = entregadorRepository.findByIdAndDisponivelTrue(entregadorId)
                .orElseThrow(() -> new RuntimeException("Entregador não disponível: " + entregadorId));
        entregador.setDisponivel(false);
        entregadorRepository.save(entregador);
        Entrega entrega = new Entrega(pedidoId, entregador, "N/A");
        return entregaRepository.save(entrega);
    }

    @Transactional
    public void atribuirEntregadorAutomatico(Long pedidoId, String moradaEntrega, String cidade) {
        logger.info("A processar atribuição automática para pedido {}", pedidoId);

        // Verificar se já existe entrega para este pedido
        if (entregaRepository.existsByPedidoId(pedidoId)) {
            logger.warn("Pedido {} já tem entrega atribuída", pedidoId);
            return;
        }

        // Procurar entregador disponível na mesma zona (cidade)
        List<Entregador> entregadores = entregadorRepository.findByZonaAtuacaoIgnoreCaseAndDisponivelTrue(cidade);
        if (entregadores.isEmpty()) {
            logger.warn("Nenhum entregador disponível na zona {}", cidade);
            // Em produção, poderia tentar novamente mais tarde ou notificar admin
            return;
        }

        Entregador entregador = entregadores.get(0);
        entregador.setDisponivel(false);
        entregadorRepository.save(entregador);

        // Criar entrega
        Entrega entrega = new Entrega(pedidoId, entregador, moradaEntrega);
        entregaRepository.save(entrega);

        // Publicar evento de atribuição para o monólito
        eventProducer.publicarEntregaAtribuida(pedidoId, entrega.getId(), entregador.getId());

        logger.info("Entregador {} atribuído ao pedido {} com entrega {}", entregador.getId(), pedidoId, entrega.getId());
    }

    @Transactional
    public Entrega iniciarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findByIdWithEntregador(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
        entrega.iniciarEntrega(); // muda estado para A_CAMINHO
        Entrega saved = entregaRepository.save(entrega);
        eventProducer.publicarStatusAtualizado(entrega.getPedidoId(), "A_CAMINHO", entregaId);
        return saved;
    }

    @Transactional
    public Entrega concluirEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findByIdWithEntregador(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
        entrega.concluir(); // muda para CONCLUIDA e liberta entregador
        Entrega saved = entregaRepository.save(entrega);
        eventProducer.publicarStatusAtualizado(entrega.getPedidoId(), "CONCLUIDA", entregaId);
        return saved;
    }

    @Transactional
    public void cancelarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findByIdWithEntregador(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
        entrega.cancelar(); // muda para CANCELADA e liberta entregador
        entregaRepository.save(entrega);

        eventProducer.publicarStatusAtualizado(entrega.getPedidoId(), "CANCELADA", entregaId);
    }
}