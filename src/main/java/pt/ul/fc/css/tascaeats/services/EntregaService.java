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

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     *
     * @param entregaRepository    repositório de entregas
     * @param entregadorRepository repositório de entregadores
     * @param pedidoRepository     repositório de pedidos
     */
    public EntregaService(EntregaRepository entregaRepository, 
                          EntregadorRepository entregadorRepository,
                          PedidoRepository pedidoRepository) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Atribui um entregador específico a um pedido pronto para entrega.
     *
     * Regras aplicadas:
     * - O pedido deve estar no estado {@code READY}.
     * - O pedido não pode já ter uma entrega atribuída.
     * - O entregador deve estar disponível ({@code disponivel = true}).
     * - O entregador não pode ter nenhuma entrega ativa.
     *
     * Após atribuição: o entregador fica {@code disponivel = false} e o pedido
     * avança de {@code READY} para {@code IN_DELIVERY}.
     *
     * @param pedidoId     ID do pedido a entregar
     * @param entregadorId ID do entregador a atribuir
     * @return a entrega criada e persistida com estado {@code ATRIBUIDA}
     * @throws RuntimeException      se o pedido ou entregador não forem
     *                               encontrados, ou o entregador estiver
     *                               indisponível
     * @throws IllegalStateException se o pedido não estiver {@code READY} ou já
     *                               tiver entrega atribuída
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
     * Atribui automaticamente o primeiro entregador disponível na cidade do
     * restaurante.
     *
     * Aplica as mesmas regras que {@link #atribuirEntregador(Long, Long)},
     * selecionando o primeiro entregador disponível na zona correspondente
     * à cidade do restaurante associado ao pedido.
     *
     * @param pedidoId ID do pedido a entregar
     * @return a entrega criada e persistida com estado {@code ATRIBUIDA}
     * @throws RuntimeException      se o pedido não for encontrado ou não houver
     *                               entregadores disponíveis na zona
     * @throws IllegalStateException se o pedido não estiver {@code READY} ou já
     *                               tiver entrega atribuída
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
     * Regista a conclusão bem-sucedida de uma entrega.
     *
     * Regras aplicadas:
     * - A entrega deve estar no estado {@code A_CAMINHO}.
     *
     * Após conclusão: o entregador volta a {@code disponivel = true} e o pedido
     * avança de {@code IN_DELIVERY} para {@code DELIVERED}.
     *
     * @param entregaId ID da entrega a concluir
     * @return a entrega atualizada com estado {@code CONCLUIDA}
     * @throws RuntimeException      se a entrega não for encontrada
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code A_CAMINHO}
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
     *
     * Regras aplicadas:
     * - A entrega deve estar no estado {@code ATRIBUIDA}.
     *
     * Após cancelamento: o entregador volta a {@code disponivel = true} e o pedido
     * retorna ao estado {@code READY} para poder ser reatribuído.
     *
     * @param entregaId ID da entrega a cancelar
     * @return a entrega atualizada com estado {@code CANCELADA}
     * @throws RuntimeException      se a entrega não for encontrada
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code ATRIBUIDA}
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
     * Marca uma entrega como iniciada, avançando de {@code ATRIBUIDA} para
     * {@code A_CAMINHO}.
     *
     * Indica que o entregador já recolheu o pedido no restaurante e está a caminho
     * do cliente.
     *
     * @param entregaId ID da entrega a iniciar
     * @return a entrega atualizada com estado {@code A_CAMINHO}
     * @throws RuntimeException      se a entrega não for encontrada
     * @throws IllegalStateException se a entrega não estiver no estado
     *                               {@code ATRIBUIDA}
     */
    @Transactional
    public Entrega iniciarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada: " + entregaId));

        entrega.iniciarEntrega();
        return entregaRepository.save(entrega);
    }

    /**
     * Procura a entrega associada a um pedido.
     *
     * @param pedidoId ID do pedido
     * @return a entrega associada ao pedido
     * @throws RuntimeException se não existir nenhuma entrega para o pedido
     *                          indicado
     */
    public Entrega buscarPorPedidoId(Long pedidoId) {
        return entregaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Não existe entrega para o pedido: " + pedidoId));
    }

    /**
     * Lista as entregas ativas (estados {@code ATRIBUIDA} ou {@code A_CAMINHO}) de
     * um entregador.
     *
     * @param entregadorId ID do entregador
     * @return lista de entregas ativas do entregador
     */
    public List<Entrega> listarEntregasAtivasPorEntregador(Long entregadorId) {
        return entregaRepository.findEntregasAtivasByEntregadorId(entregadorId);
    }
}