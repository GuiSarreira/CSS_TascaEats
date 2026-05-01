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
import pt.ul.fc.css.tascaeats.repositories.MenuRepository;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private MenuService menuService;

    private Menu menu;
    private Restaurante restaurante;
    private Produto produto;
    private Endereco endereco;

    @BeforeEach
    void setUp() throws Exception {
        endereco = new Endereco("Av. Central, 10", "4000-010", "Porto");

        menu = new Menu("Menu Almoço", "Menu do dia", new ArrayList<>(), new ArrayList<>());
        setId(menu, 1L);

        restaurante = new Restaurante("O Tasqueiro", endereco, "987654321",
                "Portuguesa", LocalTime.of(12, 0), LocalTime.of(22, 0));
        setId(restaurante, 10L);

        produto = new Produto("Bacalhau à Brás", "Com batata palha", 9.50, new ArrayList<>(), "Prato Principal");
        setId(produto, 100L);
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

    // ─── criarMenu ───────────────────────────────────────────────────────────

    @Test
    void criarMenu_ComSucesso() {
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        Menu resultado = menuService.criarMenu("Menu Almoço", "Menu do dia",
                new ArrayList<>(), new ArrayList<>());

        assertThat(resultado.getNome()).isEqualTo("Menu Almoço");
        assertThat(resultado.getDescricao()).isEqualTo("Menu do dia");
        verify(menuRepository).save(any(Menu.class));
    }

    // ─── atualizarMenu ──────────────────────────────────────────────────────

    @Test
    void atualizarMenu_ComSucesso() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenReturn(menu);

        Menu resultado = menuService.atualizarMenu(1L, "Novo Nome", "Nova Descrição",
                new ArrayList<>(), new ArrayList<>());

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        assertThat(resultado.getDescricao()).isEqualTo("Nova Descrição");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void atualizarMenu_NaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.atualizarMenu(99L, "X", "Y",
                new ArrayList<>(), new ArrayList<>()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu não encontrado");
    }

    // ─── removerMenu ────────────────────────────────────────────────────────

    @Test
    void removerMenu_ComSucesso() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        assertThatCode(() -> menuService.removerMenu(1L))
                .doesNotThrowAnyException();
        verify(menuRepository).delete(menu);
    }

    @Test
    void removerMenu_NaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.removerMenu(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── buscarPorId ────────────────────────────────────────────────────────

    @Test
    void buscarPorId_Encontrado_ReturnsMenu() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        Menu resultado = menuService.buscarPorId(1L);

        assertThat(resultado.getNome()).isEqualTo("Menu Almoço");
    }

    @Test
    void buscarPorId_NaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu não encontrado");
    }

    // ─── associarMenuRestaurante (N:1) ──────────────────────────────────────

    @Test
    void associarMenuRestaurante_ComSucesso() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        menuService.associarMenuRestaurante(1L, 10L);

        assertThat(menu.getRestaurantes()).contains(restaurante);
        assertThat(restaurante.getMenu()).isEqualTo(menu);
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    void associarMenuRestaurante_JaAssociado_NaoRepete() {
        menu.getRestaurantes().add(restaurante);
        restaurante.setMenu(menu);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        menuService.associarMenuRestaurante(1L, 10L);

        assertThat(menu.getRestaurantes()).hasSize(1);
        verify(restauranteRepository, never()).save(any());
    }

    @Test
    void associarMenuRestaurante_RestauranteNaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.associarMenuRestaurante(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurante não encontrado");
    }

    // ─── removerMenuRestaurante ─────────────────────────────────────────────

    @Test
    void removerMenuRestaurante_ComSucesso() {
        menu.getRestaurantes().add(restaurante);
        restaurante.setMenu(menu);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        menuService.removerMenuRestaurante(1L, 10L);

        assertThat(menu.getRestaurantes()).doesNotContain(restaurante);
        assertThat(restaurante.getMenu()).isNull();
        verify(restauranteRepository).save(restaurante);
    }

    // ─── adicionarProdutoMenu (N:N) ─────────────────────────────────────────

    @Test
    void adicionarProdutoMenu_ComSucesso() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        menuService.adicionarProdutoMenu(1L, 100L);

        assertThat(menu.getProdutos()).contains(produto);
        assertThat(produto.getMenus()).contains(menu);
        verify(menuRepository).save(menu);
    }

    @Test
    void adicionarProdutoMenu_JaAssociado_NaoRepete() {
        menu.getProdutos().add(produto);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        menuService.adicionarProdutoMenu(1L, 100L);

        assertThat(menu.getProdutos()).hasSize(1);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void adicionarProdutoMenu_ProdutoNaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.adicionarProdutoMenu(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    // ─── removerProdutoMenu ─────────────────────────────────────────────────

    @Test
    void removerProdutoMenu_ComSucesso() {
        menu.getProdutos().add(produto);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));

        menuService.removerProdutoMenu(1L, 100L);

        assertThat(menu.getProdutos()).doesNotContain(produto);
        verify(menuRepository).save(menu);
    }

    @Test
    void removerProdutoMenu_ProdutoNaoEncontrado_ThrowsRuntimeException() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.removerProdutoMenu(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }
}
