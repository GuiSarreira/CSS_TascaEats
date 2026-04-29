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
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.repositories.MenuRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Endereco endereco;
    private Restaurante restaurante;
    private Produto produto;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua C, 3", "3000-003", "Coimbra");
        restaurante = new Restaurante("Tasca Coimbra", endereco, "111222333");
        produto = new Produto("Bacalhau", "Bacalhau à Bras", 12.5, "Prato Principal");
    }

    // ─── criarProduto ─────────────────────────────────────────────────────────

    @Test
    void criarProduto_ComSucesso() {
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.save(any())).thenReturn(produto);

        Produto novo = new Produto("Bacalhau", "Bacalhau à Bras", 12.5, "Prato Principal");
        Produto resultado = produtoService.criarProduto(1L, novo);

        assertThat(resultado.getNome()).isEqualTo("Bacalhau");
        verify(produtoRepository).save(novo);
    }

    @Test
    void criarProduto_RestauranteNaoEncontrado_ThrowsRuntimeException() {
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criarProduto(99L, new Produto("X", "Y", 5.0, (String) null)))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    void buscarPorId_Encontrado_ReturnsProduto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        Produto resultado = produtoService.buscarPorId(1L);

        assertThat(resultado.getNome()).isEqualTo("Bacalhau");
    }

    @Test
    void buscarPorId_NaoEncontrado_ThrowsRuntimeException() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── atualizarProduto ─────────────────────────────────────────────────────

    @Test
    void atualizarProduto_ComSucesso() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any())).thenReturn(produto);

        Produto resultado = produtoService.atualizarProduto(1L, "Sardinhas", "Grelhadas", 9.0);

        assertThat(resultado.getNome()).isEqualTo("Sardinhas");
        assertThat(resultado.getPreco()).isEqualTo(9.0);
    }

    // ─── alternarDisponibilidade ──────────────────────────────────────────────

    @Test
    void alternarDisponibilidade_ParaFalse_Esgotado() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        produtoService.alternarDisponibilidade(1L, false);

        assertThat(produto.isDisponivel()).isFalse();
    }

    // ─── removerProduto ───────────────────────────────────────────────────────

    @Test
    void removerProduto_SemHistorico_DeleteFisico() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        // produto.getItensPedido() returns empty list by default

        produtoService.removerProduto(1L);

        verify(produtoRepository).delete(produto);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void removerProduto_ComHistorico_SoftDelete() {
        Produto produtoSpy = spy(produto);
        ProdutoPedido itemMock = mock(ProdutoPedido.class);
        doReturn(List.of(itemMock)).when(produtoSpy).getItensPedido();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoSpy));

        produtoService.removerProduto(1L);

        verify(produtoRepository).save(produtoSpy);
        verify(produtoRepository, never()).delete(any());
        assertThat(produtoSpy.isEliminado()).isTrue();
    }

    // ─── listarMenuDoRestaurante ──────────────────────────────────────────────

    @Test
    void listarMenuDoRestaurante_ReturnsList() {
        Menu menu = new Menu("M", "D", new ArrayList<>(List.of(produto)), new ArrayList<>());
        restaurante.setMenu(menu);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));

        List<Produto> resultado = produtoService.listarMenuDoRestaurante(1L);

        assertThat(resultado).hasSize(1);
    }
}
