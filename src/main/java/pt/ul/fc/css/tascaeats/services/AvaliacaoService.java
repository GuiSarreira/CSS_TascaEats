package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.*;

import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final PedidoRepository pedidoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
            ClienteRepository clienteRepository,
            RestauranteRepository restauranteRepository,
            PedidoRepository pedidoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.clienteRepository = clienteRepository;
        this.restauranteRepository = restauranteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Cria uma nova avaliação.
     * Regras:
     * - O pedido deve pertencer ao cliente.
     * - O pedido deve estar no estado DELIVERED.
     * - O restaurante deve ser um dos restaurantes que forneceu produtos no pedido.
     * - O cliente não pode ter avaliado o mesmo pedido antes.
     */
    @Transactional
    public Avaliacao criarAvaliacao(Long clienteId, Long restauranteId, Long pedidoId,
            int nota, String comentario) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        Pedido pedido = null;

        // Fluxo com pedido (web/API): mantém as regras completas de negócio.
        if (pedidoId != null && pedidoId > 0) {
            pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

            // 1. O pedido pertence ao cliente?
            if (!pedido.getCliente().getId().equals(clienteId)) {
                throw new IllegalStateException("Este pedido não pertence ao cliente.");
            }
            // 2. Pedido entregue?
            if (pedido.getStatus() != PedidoStatus.DELIVERED) {
                throw new IllegalStateException("Só é possível avaliar após a entrega do pedido.");
            }
            // 3. O restaurante está presente no pedido? (verificar nos produtos do pedido via menus)
            boolean restauranteNoPedido = pedido.getProdutosPedido().stream()
                    .flatMap(pp -> pp.getProduto().getMenus().stream())
                    .flatMap(menu -> menu.getRestaurantes().stream())
                    .anyMatch(r -> r.getId().equals(restauranteId));
            if (!restauranteNoPedido) {
                throw new IllegalStateException("O restaurante não forneceu produtos neste pedido.");
            }
            // 4. Já existe avaliação para este pedido?
            if (avaliacaoRepository.findByPedidoId(pedidoId).isPresent()) {
                throw new IllegalStateException("Este pedido já tem uma avaliação.");
            }
        }

        Avaliacao avaliacao = new Avaliacao(nota, comentario, cliente, restaurante, pedido);
        return avaliacaoRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> obterAvaliacoesPorRestaurante(Long restauranteId) {
        return avaliacaoRepository.findByRestauranteId(restauranteId);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> obterAvaliacoesPorCliente(Long clienteId) {
        return avaliacaoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public Double mediaNotasRestaurante(Long restauranteId) {
        return avaliacaoRepository.calcularMediaNotasPorRestaurante(restauranteId);
    }

    /**
     * Atualiza avaliação existente. Só o cliente criador pode modificar.
     */
    @Transactional
    public Avaliacao atualizarAvaliacao(Long avaliacaoId, int nota, String comentario, Long clienteId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        if (!avaliacao.getCliente().getId().equals(clienteId)) {
            throw new IllegalStateException("Apenas o cliente autor da avaliação pode modificá-la.");
        }
        avaliacao.atualizar(nota, comentario);
        return avaliacaoRepository.save(avaliacao);
    }

    /**
     * Remove uma avaliação. Só o cliente criador ou um administrador pode fazê-lo.
     */
    @Transactional
    public void removerAvaliacao(Long avaliacaoId, Long clienteId, boolean isAdmin) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        if (!isAdmin && !avaliacao.getCliente().getId().equals(clienteId)) {
            throw new IllegalStateException("Não tem permissão para remover esta avaliação.");
        }
        avaliacaoRepository.delete(avaliacao);
    }
}
