package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private Restaurante restaurante;
    private Produto produto;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua das Flores, 10", "1200-123", "Lisboa");
        cliente = new Cliente("cliente@test.com", "Ana Silva", "pass", endereco);
        restaurante = new Restaurante("Tasca Boa", endereco, "123456789");
        restaurante.setAberto(true);
        // Ligar produto → menu → restaurante para que PedidoService encontre o
        // restaurante
        Menu menu = new Menu("Menu", "desc", new ArrayList<>(), new ArrayList<>());
        restaurante.setMenu(menu);
        menu.getRestaurantes().add(restaurante);
        produto = new Produto("Pizza", "Margherita", 10.0, new ArrayList<>(List.of(menu)), null);
        menu.getProdutos().add(produto);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── criarPedido ──────────────────────────────────────────────────────────

    @Test
    void criarPedido_ComSucesso_RetornaPedidoCREATED() {
        Pedido resultado = pedidoService.criarPedido(1L, endereco, Map.of(1L, 2));

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(PedidoStatus.CREATED);
        assertThat(resultado.getPrecoTotal()).isEqualTo(20.0); // 10.0 * 2
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void criarPedido_ClienteNaoEncontrado_ThrowsRuntimeException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPedido(99L, endereco, Map.of(1L, 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    void criarPedido_RestauranteFechado_ThrowsIllegalStateException() {
        restaurante.setAberto(false);

        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of(1L, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fechado");
    }

    @Test
    void criarPedido_ItensVazios_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos um produto");
    }

    @Test
    void criarPedido_ItensNulos_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void criarPedido_ProdutoEliminado_ThrowsIllegalStateException() {
        produto.deleteLogicamente();

        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of(1L, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("removido do menu");
    }

    @Test
    void criarPedido_ProdutoEsgotado_ThrowsIllegalStateException() {
        produto.setDisponivel(false);

        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of(1L, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("esgotado");
    }

    @Test
    void criarPedido_QuantidadeZero_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of(1L, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve ser maior que zero");
    }

    @Test
    void criarPedido_ProdutoNaoEncontrado_ThrowsRuntimeException() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPedido(1L, endereco, Map.of(99L, 1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    // ─── cancelarPedido ───────────────────────────────────────────────────────

    @Test
    void cancelarPedido_EstadoCREATED_ComSucesso() {
        Pedido pedido = new Pedido(cliente, endereco);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.cancelarPedido(1L);

        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.CANCELLED);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void cancelarPedido_EstadoPREPARING_ThrowsIllegalStateException() {
        Pedido pedido = new Pedido(cliente, endereco);
        pedido.avancarEstado(); // CREATED → PAID
        pedido.avancarEstado(); // PAID → PREPARING
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelarPedido_NaoEncontrado_ThrowsRuntimeException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.cancelarPedido(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    // ─── avancarEstado ────────────────────────────────────────────────────────

    @Test
    void avancarEstado_DeCREATEDParaPAID_Sucesso() {
        Pedido pedido = new Pedido(cliente, endereco);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.avancarEstado(1L);

        assertThat(resultado.getStatus()).isEqualTo(PedidoStatus.PAID);
    }

    @Test
    void avancarEstado_DeDelivered_ThrowsIllegalStateException() {
        Pedido pedido = new Pedido(cliente, endereco);
        pedido.setStatus(PedidoStatus.DELIVERED);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.avancarEstado(1L))
                .isInstanceOf(IllegalStateException.class);
    }
}
