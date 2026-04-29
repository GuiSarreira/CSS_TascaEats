package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.AtribuirEntregadorRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.EntregaService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { EntregaController.class, GlobalExceptionHandler.class })
class EntregaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntregaService entregaService;

    private Entrega entrega;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua G, 7", "7000-007", "Faro");
        Cliente cliente = new Cliente("c@test.com", "Rui", "pass", endereco);
        Pedido pedido = new Pedido(cliente, endereco);
        Entregador entregador = new Entregador("e@test.com", "Nuno", "pass", "bicicleta", "Faro");
        entrega = new Entrega(pedido, entregador);
    }

    // ─── POST /api/pedidos/{id}/entregar ──────────────────────────────────────

    @Test
    void atribuirEntregador_ComId_Returns201() throws Exception {
        when(entregaService.atribuirEntregador(1L, 2L)).thenReturn(entrega);

        AtribuirEntregadorRequest request = new AtribuirEntregadorRequest();
        request.setEntregadorId(2L);

        mockMvc.perform(post("/api/pedidos/1/entregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATRIBUIDA"))
                .andExpect(jsonPath("$.entregadorNome").value("Nuno"));
    }

    @Test
    void atribuirEntregador_Automatico_Returns201() throws Exception {
        when(entregaService.atribuirEntregadorAutomatico(1L)).thenReturn(Optional.of(entrega));

        AtribuirEntregadorRequest request = new AtribuirEntregadorRequest();
        request.setEntregadorId(null);

        mockMvc.perform(post("/api/pedidos/1/entregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void atribuirEntregador_PedidoNaoREADY_Returns422() throws Exception {
        when(entregaService.atribuirEntregador(1L, 2L))
                .thenThrow(new IllegalStateException("Pedido não está no estado READY"));

        AtribuirEntregadorRequest request = new AtribuirEntregadorRequest();
        request.setEntregadorId(2L);

        mockMvc.perform(post("/api/pedidos/1/entregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ─── PATCH /api/entregas/{id}/concluir ────────────────────────────────────

    @Test
    void concluirEntrega_Sucesso_Returns200() throws Exception {
        entrega.iniciarEntrega(); // ATRIBUIDA → A_CAMINHO
        when(entregaService.concluirEntrega(1L)).thenReturn(entrega);

        mockMvc.perform(patch("/api/entregas/1/concluir"))
                .andExpect(status().isOk());
    }

    // ─── PATCH /api/entregas/{id}/cancelar ───────────────────────────────────

    @Test
    void cancelarEntrega_Sucesso_Returns204() throws Exception {
        mockMvc.perform(patch("/api/entregas/1/cancelar"))
                .andExpect(status().isNoContent());
    }

    // ─── GET /api/pedidos/{id}/entrega ────────────────────────────────────────

    @Test
    void buscarEntregaPorPedido_Sucesso_Returns200() throws Exception {
        when(entregaService.buscarPorPedidoId(1L)).thenReturn(entrega);

        mockMvc.perform(get("/api/pedidos/1/entrega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATRIBUIDA"));
    }
}
