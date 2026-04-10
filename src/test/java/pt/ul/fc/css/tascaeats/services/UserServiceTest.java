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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private EntregadorRepository entregadorRepository;

    @InjectMocks
    private UserService userService;

    private Endereco endereco;
    private Cliente cliente;
    private Admin admin;
    private Entregador entregador;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua B, 2", "1100-002", "Porto");
        cliente = new Cliente("c@test.com", "Maria Cliente", "pass", endereco);
        admin = new Admin("a@test.com", "Carlos Admin", "pass");
        entregador = new Entregador("e@test.com", "João Entregador", "pass", "moto", "Lisboa");
    }

    // ─── registarCliente ─────────────────────────────────────────────────────

    @Test
    void registarCliente_ComSucesso() {
        when(userRepository.existsByEmail("c@test.com")).thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(cliente);

        Cliente resultado = userService.registarCliente("c@test.com", "Maria Cliente", "pass", endereco);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("c@test.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void registarCliente_EmailDuplicado_ThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail("c@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registarCliente("c@test.com", "X", "pass", endereco))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email já registado");
    }

    // ─── registarAdmin ────────────────────────────────────────────────────────

    @Test
    void registarAdmin_ComSucesso() {
        when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(adminRepository.save(any())).thenReturn(admin);

        Admin resultado = userService.registarAdmin("a@test.com", "Carlos Admin", "pass");

        assertThat(resultado.getEmail()).isEqualTo("a@test.com");
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void registarAdmin_EmailDuplicado_ThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail("a@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registarAdmin("a@test.com", "X", "pass"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── registarEntregador ───────────────────────────────────────────────────

    @Test
    void registarEntregador_ComSucesso() {
        when(userRepository.existsByEmail("e@test.com")).thenReturn(false);
        when(entregadorRepository.save(any())).thenReturn(entregador);

        Entregador resultado = userService.registarEntregador("e@test.com", "João Entregador", "pass", "moto",
                "Lisboa");

        assertThat(resultado.getVeiculo()).isEqualTo("moto");
        verify(entregadorRepository).save(any(Entregador.class));
    }

    @Test
    void registarEntregador_EmailDuplicado_ThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail("e@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registarEntregador("e@test.com", "X", "p", "moto", "Lisboa"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    void buscarPorId_Encontrado_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));

        User resultado = userService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(cliente);
    }

    @Test
    void buscarPorId_NaoEncontrado_ThrowsRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ─── listarTodosAtivos / listarEntregadoresDisponiveis ────────────────────

    @Test
    void listarTodosAtivos_ReturnsList() {
        when(userRepository.findByAtivoTrue()).thenReturn(List.of(cliente, admin));

        List<User> resultado = userService.listarTodosAtivos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void listarEntregadoresDisponiveis_ReturnsList() {
        when(entregadorRepository.findByDisponivelTrueAndAtivoTrue()).thenReturn(List.of(entregador));

        List<Entregador> resultado = userService.listarEntregadoresDisponiveis();

        assertThat(resultado).hasSize(1);
    }

    // ─── buscarPorEmail ───────────────────────────────────────────────────────

    @Test
    void buscarPorEmail_Encontrado_ReturnsOptional() {
        when(userRepository.findByEmail("c@test.com")).thenReturn(Optional.of(cliente));

        Optional<User> resultado = userService.buscarPorEmail("c@test.com");

        assertThat(resultado).isPresent();
    }

    // ─── atualizarUser ────────────────────────────────────────────────────────

    @Test
    void atualizarUser_AlteraNome_ComSucesso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any())).thenReturn(cliente);

        User resultado = userService.atualizarUser(1L, "Novo Nome", null, null);

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
    }

    @Test
    void atualizarUser_ClienteAlteraMorada() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any())).thenReturn(cliente);

        Endereco novaMorada = new Endereco("Rua Nova, 9", "2000-009", "Braga");
        userService.atualizarUser(1L, null, null, novaMorada);

        assertThat(cliente.getMorada().getCidade()).isEqualTo("Braga");
    }

    // ─── removerUser ─────────────────────────────────────────────────────────

    @Test
    void removerUser_Cliente_SemPedidosAtivos_Sucesso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(userRepository.save(any())).thenReturn(cliente);

        userService.removerUser(1L);

        verify(userRepository).save(cliente);
    }

    @Test
    void removerUser_Cliente_ComPedidosAtivos_ThrowsIllegalStateException() {
        Pedido pedidoAtivo = new Pedido(cliente, new Restaurante("R", endereco, "111"), endereco);
        // Status por defeito é CREATED — ativo
        cliente.setPedidos(List.of(pedidoAtivo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> userService.removerUser(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pedidos ativos");
    }

    @Test
    void removerUser_EntregadorComEntregasAtivas_ThrowsIllegalStateException() {
        Entregador entregadorSpy = spy(entregador);
        Entrega entregaAtiva = new Entrega(
                new Pedido(cliente, new Restaurante("R", endereco, "222"), endereco),
                entregadorSpy);
        doReturn(List.of(entregaAtiva)).when(entregadorSpy).getEntregas();
        when(userRepository.findById(2L)).thenReturn(Optional.of(entregadorSpy));

        assertThatThrownBy(() -> userService.removerUser(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entregas ativas");
    }
}
