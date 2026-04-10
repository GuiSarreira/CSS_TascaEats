package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.CriarRestauranteRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.RestauranteService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RestauranteController.class, GlobalExceptionHandler.class})
class RestauranteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RestauranteService restauranteService;

    private Restaurante restaurante;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Avenida Central, 5", "4000-001", "Porto");
        restaurante = new Restaurante("Casa do Porto", endereco, "555666777");
        restaurante.setAberto(true);
    }

    // ─── POST /api/restaurantes ───────────────────────────────────────────────

    @Test
    void criar_Sucesso_Returns201() throws Exception {
        when(restauranteService.criarRestaurante(any(), any(), any(), any()))
                .thenReturn(restaurante);

        CriarRestauranteRequest request = new CriarRestauranteRequest();
        request.setNome("Casa do Porto");
        request.setNif("555666777");
        request.setMorada(endereco);
        request.setAdminId(1L);

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Casa do Porto"))
                .andExpect(jsonPath("$.nif").value("555666777"));
    }

    @Test
    void criar_UtilizadorNaoAdmin_Returns403() throws Exception {
        when(restauranteService.criarRestaurante(any(), any(), any(), any()))
                .thenThrow(new SecurityException("Apenas administradores podem criar restaurantes"));

        CriarRestauranteRequest request = new CriarRestauranteRequest();
        request.setNome("Tasca");
        request.setNif("111222333");
        request.setMorada(endereco);
        request.setAdminId(99L);

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void criar_NifDuplicado_Returns400() throws Exception {
        when(restauranteService.criarRestaurante(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Já existe um restaurante registado com este NIF"));

        CriarRestauranteRequest request = new CriarRestauranteRequest();
        request.setNome("Outro");
        request.setNif("555666777");
        request.setMorada(endereco);
        request.setAdminId(1L);

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── GET /api/restaurantes ────────────────────────────────────────────────

    @Test
    void listarTodos_Returns200ComLista() throws Exception {
        when(restauranteService.listarTodos()).thenReturn(List.of(restaurante));

        mockMvc.perform(get("/api/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa do Porto"));
    }

    // ─── GET /api/restaurantes/{id} ───────────────────────────────────────────

    @Test
    void buscarPorId_Sucesso_Returns200() throws Exception {
        when(restauranteService.buscarPorId(1L)).thenReturn(restaurante);

        mockMvc.perform(get("/api/restaurantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nif").value("555666777"));
    }

    @Test
    void buscarPorId_NaoEncontrado_Returns404() throws Exception {
        when(restauranteService.buscarPorId(99L))
                .thenThrow(new RuntimeException("Restaurante não encontrado."));

        mockMvc.perform(get("/api/restaurantes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Restaurante não encontrado."));
    }

    // ─── PATCH /api/restaurantes/{id}/estado ──────────────────────────────────

    @Test
    void alterarEstado_Abrir_Returns204() throws Exception {
        mockMvc.perform(patch("/api/restaurantes/1/estado").param("aberto", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void alterarEstado_Fechar_Returns204() throws Exception {
        mockMvc.perform(patch("/api/restaurantes/1/estado").param("aberto", "false"))
                .andExpect(status().isNoContent());
    }

    // ─── DELETE /api/restaurantes/{id} ────────────────────────────────────────

    @Test
    void remover_Sucesso_Returns204() throws Exception {
        mockMvc.perform(delete("/api/restaurantes/1").param("adminId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void remover_ComPedidos_Returns422() throws Exception {
        doThrow(new IllegalStateException("Não é possível remover um restaurante que já processou pedidos"))
                .when(restauranteService).removerRestaurante(1L, 1L);

        mockMvc.perform(delete("/api/restaurantes/1").param("adminId", "1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ─── GET /api/restaurantes/cidade ─────────────────────────────────────────

    @Test
    void buscarPorCidade_Returns200() throws Exception {
        when(restauranteService.buscarPorCidade("Porto")).thenReturn(List.of(restaurante));

        mockMvc.perform(get("/api/restaurantes/cidade").param("cidade", "Porto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa do Porto"));
    }
}
