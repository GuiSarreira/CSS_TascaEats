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
import pt.ul.fc.css.tascaeats.repositories.PagamentoRepository;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pedido pedido;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua X", "1000-001", "Lisboa");
        cliente = new Cliente("c@test.com", "Cliente", "pass", endereco);
        pedido = new Pedido(cliente, endereco);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(false);
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── processarPagamento ───────────────────────────────────────────────────

    @Test
    void processarPagamento_Multibanco_Sucesso() {
        Pagamento pag = pagamentoService.processarPagamento(1L, "MULTIBANCO", "123 456 789", "Visa", null);

        assertThat(pag).isInstanceOf(Multibanco.class);
        assertThat(pag.getStatus()).isEqualTo(PagamentoStatus.COMPLETED);
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.PAID);
        verify(pagamentoRepository).save(any(Pagamento.class));
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void processarPagamento_MBWay_Sucesso() {
        Pagamento pag = pagamentoService.processarPagamento(1L, "MBWAY", "912345678", null, null);

        assertThat(pag).isInstanceOf(MBWay.class);
        assertThat(pag.getStatus()).isEqualTo(PagamentoStatus.COMPLETED);
    }

    @Test
    void processarPagamento_Dinheiro_Sucesso() {
        Pagamento pag = pagamentoService.processarPagamento(1L, "DINHEIRO", null, null, 5.0);
        assertThat(pag).isInstanceOf(Dinheiro.class);
        assertThat(pag.getStatus()).isEqualTo(PagamentoStatus.COMPLETED);
    }

    @Test
    void processarPagamento_PedidoNaoCREATED_ThrowsIllegalStateException() {
        pedido.avancarEstado(); // CREATED → PAID

        assertThatThrownBy(() -> pagamentoService.processarPagamento(1L, "DINHEIRO", null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");
    }

    @Test
    void processarPagamento_JaTemPagamento_ThrowsIllegalStateException() {
        when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> pagamentoService.processarPagamento(1L, "DINHEIRO", null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já tem um pagamento");
    }

    @Test
    void processarPagamento_TipoInvalido_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> pagamentoService.processarPagamento(1L, "BITCOIN", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de pagamento inválido");
    }

    @Test
    void processarPagamento_PedidoNaoEncontrado_ThrowsRuntimeException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.processarPagamento(99L, "DINHEIRO", null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    @Test
    void buscarPorPedido_Existente_ReturnsOptionalComPagamento() {
        Pagamento pag = new Dinheiro(pedido, 15.0, null);
        when(pagamentoRepository.findByPedidoId(1L)).thenReturn(Optional.of(pag));

        Optional<Pagamento> resultado = pagamentoService.buscarPorPedido(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isInstanceOf(Dinheiro.class);
    }

    @Test
    void buscarPorPedido_Inexistente_ReturnsEmpty() {
        when(pagamentoRepository.findByPedidoId(99L)).thenReturn(Optional.empty());

        assertThat(pagamentoService.buscarPorPedido(99L)).isEmpty();
    }
}
