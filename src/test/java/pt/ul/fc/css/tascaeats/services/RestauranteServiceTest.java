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
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.repositories.UserRepository;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestauranteServiceTest {

    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RestauranteService restauranteService;

    private Endereco endereco;
    private Admin admin;
    private Restaurante restaurante;

    @BeforeEach
    void setUp() throws Exception {
        endereco = new Endereco("Av. Central, 10", "4000-010", "Porto");
        admin = new Admin("admin@test.com", "Dono Admin", "pass");
        setId(admin, 1L);

        restaurante = new Restaurante("O Tasqueiro", endereco, "987654321",
                "Portuguesa", LocalTime.of(12, 0), LocalTime.of(22, 0));
        restaurante.setAdmin(admin);
        setId(restaurante, 10L);
    }

    /**
     * Auxiliar: define o ID de uma entidade via reflection (percorre hierarquia).
     */
    private static void setId(Object entity, Long id) throws Exception {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField("id");
                f.setAccessible(true);
                f.set(entity, id);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("id not found in hierarchy of " + entity.getClass());
    }

    // ─── criarRestaurante ─────────────────────────────────────────────────────

    @Test
    void criarRestaurante_ComSucesso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByNif("987654321")).thenReturn(Optional.empty());
        when(restauranteRepository.save(any())).thenReturn(restaurante);

        Restaurante resultado = restauranteService.criarRestaurante("O Tasqueiro", endereco, "987654321",
                "Portuguesa", LocalTime.of(12, 0), LocalTime.of(22, 0), 1L);

        assertThat(resultado.getNome()).isEqualTo("O Tasqueiro");
        verify(restauranteRepository).save(any(Restaurante.class));
    }

    @Test
    void criarRestaurante_AdminNaoEncontrado_ThrowsRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.criarRestaurante("X", endereco, "111", null, null, null, 99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void criarRestaurante_UtilizadorNaoAdmin_ThrowsSecurityException() {
        Cliente cliente = new Cliente("c@c.com", "Cliente", "pass", endereco);
        when(userRepository.findById(2L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> restauranteService.criarRestaurante("X", endereco, "222", null, null, null, 2L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void criarRestaurante_NifDuplicado_ThrowsIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(restauranteRepository.findByNif("987654321")).thenReturn(Optional.of(restaurante));

        assertThatThrownBy(
                () -> restauranteService.criarRestaurante("Outro", endereco, "987654321", null, null, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIF");
    }

    // ─── alterarEstadoAbertura ────────────────────────────────────────────────

    @Test
    void alterarEstadoAbertura_Abrir_ComSucesso() {
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        restauranteService.alterarEstadoAbertura(10L, true);

        assertThat(restaurante.isAberto()).isTrue();
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    void alterarEstadoAbertura_NaoEncontrado_ThrowsRuntimeException() {
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.alterarEstadoAbertura(99L, true))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    void buscarPorId_Encontrado_ReturnsRestaurante() {
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        Restaurante resultado = restauranteService.buscarPorId(10L);

        assertThat(resultado.getNif()).isEqualTo("987654321");
    }

    @Test
    void buscarPorId_NaoEncontrado_ThrowsRuntimeException() {
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── atualizarRestaurante ─────────────────────────────────────────────────

    @Test
    void atualizarRestaurante_ComSucesso() {
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));
        when(restauranteRepository.save(any())).thenReturn(restaurante);
        Endereco novaMorada = new Endereco("Nova Rua, 5", "5000-005", "Braga");

        Restaurante resultado = restauranteService.atualizarRestaurante(10L, "Novo Nome", novaMorada, 1L);

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
    }

    @Test
    void atualizarRestaurante_AdminErrado_ThrowsSecurityException() throws Exception {
        Admin outroAdmin = new Admin("outro@test.com", "Outro", "pass");
        setId(outroAdmin, 2L);
        restaurante.setAdmin(outroAdmin);
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        assertThatThrownBy(() -> restauranteService.atualizarRestaurante(10L, "X", endereco, 1L))
                .isInstanceOf(SecurityException.class);
    }

    // ─── removerRestaurante ───────────────────────────────────────────────────

    @Test
    void removerRestaurante_ComSucesso() {
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        assertThatCode(() -> restauranteService.removerRestaurante(10L, 1L))
                .doesNotThrowAnyException();
        verify(restauranteRepository).delete(restaurante);
    }

    @Test
    void removerRestaurante_AdminErrado_ThrowsSecurityException() throws Exception {
        Admin outroAdmin = new Admin("outro@test.com", "Outro", "pass");
        setId(outroAdmin, 99L);
        restaurante.setAdmin(outroAdmin);
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        assertThatThrownBy(() -> restauranteService.removerRestaurante(10L, 1L))
                .isInstanceOf(SecurityException.class);
    }

    // ─── buscarPorNome, buscarPorNif, buscarPorCidade, listarTodos ────────────

    @Test
    void buscarPorNome_ReturnsList() {
        when(restauranteRepository.findByNomeContainingIgnoreCase("Tasq")).thenReturn(List.of(restaurante));

        List<Restaurante> resultado = restauranteService.buscarPorNome("Tasq");

        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscarPorNif_Encontrado_ReturnsOptional() {
        when(restauranteRepository.findByNif("987654321")).thenReturn(Optional.of(restaurante));

        assertThat(restauranteService.buscarPorNif("987654321")).isPresent();
    }

    @Test
    void buscarPorCidade_ReturnsList() {
        when(restauranteRepository.findByMoradaCidadeIgnoreCase("Porto")).thenReturn(List.of(restaurante));

        List<Restaurante> resultado = restauranteService.buscarPorCidade("Porto");

        assertThat(resultado).hasSize(1);
    }

    @Test
    void listarTodos_ReturnsList() {
        when(restauranteRepository.findAll()).thenReturn(List.of(restaurante));

        List<Restaurante> resultado = restauranteService.listarTodos();

        assertThat(resultado).hasSize(1);
    }
}
