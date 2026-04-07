package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.*;

import java.util.List;

/**
 * Serviço responsável pela gestão da lógica de negócio de Entregas.
 * Coordena a atribuição de entregadores a pedidos prontos para entrega.
 */
@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;
    private final PedidoRepository pedidoRepository;

    public EntregaService(EntregaRepository entregaRepository,
                          EntregadorRepository entregadorRepository,
                          PedidoRepository pedidoRepository) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Atribui um entregador a um pedido que está pronto para entrega.
     * Regras: Pedido deve estar READY; entregador disponível; sem outras entregas ativas.
     */
    @Transactional
    public Entrega atribuirEntregador(Long pedidoId, Long entregadorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));

        if (pedido.getStatus() != PedidoStatus.READY) {
            throw new IllegalStateException("Pedido não está READY. Estado: " + pedido.getStatus());
        }

        if (entregaRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Pedido já tem entrega atribuída.");
        }

        Entregador entregador = entregadorRepository.findByIdAndDisponivelTrue(entregadorId)
                .orElseThrow(() -> new RuntimeException("Entregador não disponível: " + entregadorId));

        List<Entrega> entregasAtivas = entregaRepository.findEntregasAtivasByEntregadorId(entregadorId);
        if (!entregasAtivas.isEmpty()) {
            throw new IllegalStateException("Entregador já tem uma entrega ativa.");
        }

        Entrega entrega = new Entrega(pedido, entregador);
        entregador.setDisponivel(false);
        pedido.avancarEstado(); // READY → IN_DELIVERY

        entregadorRepository.save(entregador);
        pedidoRepository.save(pedido);
        return entregaRepository.save(entrega);
    }

    /**
     * Atribui automaticamente o primeiro entregador disponível na zona do restaurante.
     */
    @Transactional
    public Entrega atribuirEntregadorAutomatico(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + pedidoId));

        if (pedido.getStatus() != PedidoStatus.READY) {
            throw new IllegalStateException("Pedido não está READY. Estado: " + pedido.getStatus());
        }

        if (entregaRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Pedido já tem entrega atribuída.");
        }

        String zona = pedido.getRestaurante().getCidade();
        List<Entregador> entregadores = entregadorRepository.findEntregadoresDisponiveisPorZona(zona);

        if (entregadores.isEmpty()) {
            throw new RuntimeException("Não há entregadores disponíveis na zona: " + zona);
        }

        Entregador entregador = entregadores.get(0);
        Entrega entrega = new Entrega(pedido, entregador);
        entregador.setDisponivel(false);
        pedido.avancarEstado();

        entregadorRepository.save(entregador);
        pedidoRepository.save(pedido);
        return entregaRepository.save(entrega);
    }

    /**
     * Regista a conclusão de uma entrega.
     * Regras: Entrega deve estar EM_CAMINHO; após concluir, entregador fica disponível.
     */
    @Transactional
    public Entrega concluirEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + entregaId));

        if (entrega.getStatus() != EntregaStatus.A_CAMINHO) {
            throw new IllegalStateException("Entrega não está A_CAMINHO. Estado: " + entrega.getStatus());
        }

        entrega.concluir();
        entrega.getPedido().avancarEstado(); // IN_DELIVERY → DELIVERED

        pedidoRepository.save(entrega.getPedido());
        return entregaRepository.save(entrega);
    }

    /**
     * Cancela uma entrega que ainda não foi iniciada.
     */
    @Transactional
    public Entrega cancelarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + entregaId));

        if (entrega.getStatus() != EntregaStatus.ATRIBUIDA) {
            throw new IllegalStateException("Só é possível cancelar entrega no estado ATRIBUIDA.");
        }

        entrega.cancelar();
        entrega.getPedido().setStatus(PedidoStatus.READY);

        pedidoRepository.save(entrega.getPedido());
        return entregaRepository.save(entrega);
    }

    /**
     * Inicia uma entrega (muda de ATRIBUIDA para A_CAMINHO).
     */
    @Transactional
    public Entrega iniciarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + entregaId));

        entrega.iniciarEntrega();
        return entregaRepository.save(entrega);
    }

    /**
     * Busca uma entrega pelo ID do pedido.
     */
    public Entrega buscarPorPedidoId(Long pedidoId) {
        return entregaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Não existe entrega para o pedido: " + pedidoId));
    }

    /**
     * Lista entregas ativas de um entregador.
     */
    public List<Entrega> listarEntregasAtivasPorEntregador(Long entregadorId) {
        return entregaRepository.findEntregasAtivasByEntregadorId(entregadorId);
    }
}