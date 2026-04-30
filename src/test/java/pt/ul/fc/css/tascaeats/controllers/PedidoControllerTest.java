package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.CriarPedidoRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.PedidoService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { PedidoController.class, GlobalExceptionHandler.class })
class PedidoControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PedidoService pedidoService;

        private Pedido pedido;
        private Endereco endereco;

        @BeforeEach
        void setUp() {
                endereco = new Endereco("Rua das Flores, 10", "1200-123", "Lisboa");
                Cliente cliente = new Cliente("c@test.com", "Ana Silva", "pass", endereco);
                Restaurante restaurante = new Restaurante("Tasca Boa", endereco, "123456789");
                restaurante.setAberto(true);
                pedido = new Pedido(cliente, endereco);
        }

        // ─── POST /api/pedidos ────────────────────────────────────────────────────

        @Test
        void criar_Sucesso_Returns201() throws Exception {
                when(pedidoService.criarPedido(any(), any(), any(), any())).thenReturn(pedido);

                CriarPedidoRequest request = new CriarPedidoRequest();
                request.setClienteId(1L);
                request.setEnderecoEntrega(endereco);
                request.setItens(Map.of(1L, 2));

                mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        void criar_RestauranteFechado_Returns422() throws Exception {
                when(pedidoService.criarPedido(any(), any(), any(), any()))
                                .thenThrow(new IllegalStateException("o restaurante está fechado"));

                CriarPedidoRequest request = new CriarPedidoRequest();
                request.setClienteId(1L);
                request.setEnderecoEntrega(endereco);
                request.setItens(Map.of(1L, 1));

                mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.status").value(422))
                                .andExpect(jsonPath("$.message").value("o restaurante está fechado"));
        }

        @Test
        void criar_ClienteNaoEncontrado_Returns404() throws Exception {
                when(pedidoService.criarPedido(any(), any(), any(), any()))
                                .thenThrow(new RuntimeException("Cliente não encontrado: id=99"));

                CriarPedidoRequest request = new CriarPedidoRequest();
                request.setClienteId(99L);
                request.setEnderecoEntrega(endereco);
                request.setItens(Map.of(1L, 1));

                mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        // ─── GET /api/pedidos/{id} ────────────────────────────────────────────────

        @Test
        void buscarPorId_Sucesso_Returns200() throws Exception {
                when(pedidoService.buscarPorId(1L)).thenReturn(pedido);

                mockMvc.perform(get("/api/pedidos/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        void buscarPorId_NaoEncontrado_Returns404() throws Exception {
                when(pedidoService.buscarPorId(99L))
                                .thenThrow(new RuntimeException("Pedido não encontrado: id=99"));

                mockMvc.perform(get("/api/pedidos/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("Pedido não encontrado: id=99"));
        }

        // ─── PATCH /api/pedidos/{id}/cancelar ─────────────────────────────────────

        @Test
        void cancelar_Sucesso_Returns204() throws Exception {
                mockMvc.perform(patch("/api/pedidos/1/cancelar"))
                                .andExpect(status().isNoContent());
        }

        @Test
        void cancelar_EstadoInvalido_Returns422() throws Exception {
                doThrow(new IllegalStateException("Pedido só pode ser cancelado nos estados CREATED ou PAID"))
                                .when(pedidoService).cancelarPedido(1L);

                mockMvc.perform(patch("/api/pedidos/1/cancelar"))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.status").value(422));
        }

        // ─── PATCH /api/pedidos/{id}/avancar ──────────────────────────────────────

        @Test
        void avancarEstado_Sucesso_Returns200() throws Exception {
                pedido.setStatus(PedidoStatus.PAID);
                when(pedidoService.avancarEstado(1L)).thenReturn(pedido);

                mockMvc.perform(patch("/api/pedidos/1/avancar"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PAID"));
        }

        // ─── GET /api/pedidos/cliente/{clienteId} ─────────────────────────────────

        @Test
        void listarPorCliente_Sucesso_Returns200() throws Exception {
                when(pedidoService.buscarPorCliente(anyLong(), isNull())).thenReturn(java.util.List.of(pedido));

                mockMvc.perform(get("/api/pedidos/cliente/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].status").value("CREATED"));
        }
}
