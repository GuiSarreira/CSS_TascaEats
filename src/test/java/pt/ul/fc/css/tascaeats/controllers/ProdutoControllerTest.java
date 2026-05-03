package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.CriarProdutoRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.ProdutoService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { ProdutoController.class, GlobalExceptionHandler.class })
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("Francesinha", "Com molho especial", 8.5, "Prato Principal");
        produto.setDisponivel(true);
    }

    // ─── POST /api/restaurantes/{id}/produtos ─────────────────────────────────

    @Test
    void criar_Sucesso_Returns201() throws Exception {
        when(produtoService.criarProduto(eq(1L), any())).thenReturn(produto);

        CriarProdutoRequest request = new CriarProdutoRequest();
        request.setNome("Francesinha");
        request.setDescricao("Com molho especial");
        request.setPreco(8.5);

        mockMvc.perform(post("/api/restaurantes/1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Francesinha"))
                .andExpect(jsonPath("$.preco").value(8.5));
    }

    @Test
    void criar_RestauranteNaoEncontrado_Returns404() throws Exception {
        when(produtoService.criarProduto(eq(99L), any()))
                .thenThrow(new RuntimeException("Restaurante não encontrado."));

        CriarProdutoRequest request = new CriarProdutoRequest();
        request.setNome("X");
        request.setDescricao("Y");
        request.setPreco(5.0);

        mockMvc.perform(post("/api/restaurantes/99/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/restaurantes/{id}/produtos ──────────────────────────────────

    @Test
    void listarMenu_Sucesso_Returns200() throws Exception {
        when(produtoService.listarMenuDoRestaurante(1L)).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/restaurantes/1/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Francesinha"));
    }

    // ─── GET /api/restaurantes/{restId}/produtos/{id} ─────────────────────────

    @Test
    void buscarPorId_Sucesso_Returns200() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(produto);

        mockMvc.perform(get("/api/restaurantes/1/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Francesinha"));
    }

    // ─── PATCH /api/restaurantes/{restId}/produtos/{id}/disponibilidade ───────

    @Test
    void alterarDisponibilidade_Returns204() throws Exception {
        mockMvc.perform(patch("/api/restaurantes/1/produtos/1/disponibilidade")
                .param("disponivel", "false"))
                .andExpect(status().isNoContent());
    }

    // ─── DELETE /api/restaurantes/{restId}/produtos/{id} ─────────────────────

    @Test
    void remover_Sucesso_Returns204() throws Exception {
        mockMvc.perform(delete("/api/restaurantes/1/produtos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void remover_ProdutoNaoEncontrado_Returns404() throws Exception {
        doThrow(new RuntimeException("Produto não encontrado."))
                .when(produtoService).removerProduto(99L);

        mockMvc.perform(delete("/api/restaurantes/1/produtos/99"))
                .andExpect(status().isNotFound());
    }
}
