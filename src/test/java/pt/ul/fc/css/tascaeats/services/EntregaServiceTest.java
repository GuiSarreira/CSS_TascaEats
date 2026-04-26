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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntregaServiceTest {

    @Mock
    private EntregaRepository entregaRepository;
    @Mock
    private EntregadorRepository entregadorRepository;
    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private EntregaService entregaService;

    private Pedido pedido;
    private Entregador entregador;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua Y", "2000-002", "Porto");
        Cliente cliente = new Cliente("c@test.com", "Cliente", "pass", endereco);

        pedido = new Pedido(cliente, endereco);
        pedido.setStatus(PedidoStatus.READY);

        entregador = new Entregador("e@test.com", "João Mota", "pass", "moto", "Porto");

        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entregaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entregadorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── atribuirEntregador ───────────────────────────────────────────────────

    @Test
    void atribuirEntregador_ComSucesso_CriaEntregaEAtualiza() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.existsByPedidoId(1L)).thenReturn(false);
        when(entregadorRepository.findByIdAndDisponivelTrue(1L)).thenReturn(Optional.of(entregador));
        when(entregaRepository.findEntregasAtivasByEntregadorId(1L)).thenReturn(Collections.emptyList());

        Entrega entrega = entregaService.atribuirEntregador(1L, 1L);

        assertThat(entrega).isNotNull();
        assertThat(entrega.getStatus()).isEqualTo(EntregaStatus.ATRIBUIDA);
        assertThat(entregador.isDisponivel()).isFalse();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.IN_DELIVERY);
    }

    @Test
    void atribuirEntregador_PedidoNaoREADY_ThrowsIllegalStateException() {
        pedido.setStatus(PedidoStatus.PAID);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> entregaService.atribuirEntregador(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("READY");
    }

    @Test
    void atribuirEntregador_JaTemEntrega_ThrowsIllegalStateException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.existsByPedidoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> entregaService.atribuirEntregador(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já tem entrega atribuída");
    }

    @Test
    void atribuirEntregador_EntregadorIndisponivel_ThrowsRuntimeException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.existsByPedidoId(1L)).thenReturn(false);
        when(entregadorRepository.findByIdAndDisponivelTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entregaService.atribuirEntregador(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entregador não disponível");
    }

    @Test
    void atribuirEntregador_EntregadorComEntregaAtiva_ThrowsIllegalStateException() {
        Entrega entregaAtiva = new Entrega(pedido, entregador);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.existsByPedidoId(1L)).thenReturn(false);
        when(entregadorRepository.findByIdAndDisponivelTrue(1L)).thenReturn(Optional.of(entregador));
        when(entregaRepository.findEntregasAtivasByEntregadorId(1L)).thenReturn(List.of(entregaAtiva));

        assertThatThrownBy(() -> entregaService.atribuirEntregador(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entrega ativa");
    }

    @Test
    void atribuirEntregador_PedidoNaoEncontrado_ThrowsRuntimeException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entregaService.atribuirEntregador(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    // ─── concluirEntrega ──────────────────────────────────────────────────────

    @Test
    void concluirEntrega_ComSucesso_EntregaConcluida() {
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.iniciarEntrega(); // ATRIBUIDA → A_CAMINHO
        pedido.setStatus(PedidoStatus.IN_DELIVERY);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        Entrega resultado = entregaService.concluirEntrega(1L);

        assertThat(resultado.getStatus()).isEqualTo(EntregaStatus.CONCLUIDA);
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.DELIVERED);
        assertThat(entregador.isDisponivel()).isTrue();
    }

    @Test
    void concluirEntrega_NaoACaminho_ThrowsIllegalStateException() {
        Entrega entrega = new Entrega(pedido, entregador); // estado: ATRIBUIDA
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        assertThatThrownBy(() -> entregaService.concluirEntrega(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("A_CAMINHO");
    }

    @Test
    void concluirEntrega_NaoEncontrada_ThrowsRuntimeException() {
        when(entregaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entregaService.concluirEntrega(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entrega não encontrada");
    }

    // ─── cancelarEntrega ──────────────────────────────────────────────────────

    @Test
    void cancelarEntrega_ComSucesso_EntregaCancelada() {
        Entrega entrega = new Entrega(pedido, entregador); // estado: ATRIBUIDA
        entregador.setDisponivel(false);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        Entrega resultado = entregaService.cancelarEntrega(1L);

        assertThat(resultado.getStatus()).isEqualTo(EntregaStatus.CANCELADA);
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.READY);
        assertThat(entregador.isDisponivel()).isTrue();
    }

    @Test
    void cancelarEntrega_NaoAtribuida_ThrowsIllegalStateException() {
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.iniciarEntrega(); // ATRIBUIDA → A_CAMINHO
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        assertThatThrownBy(() -> entregaService.cancelarEntrega(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ATRIBUIDA");
    }
}
