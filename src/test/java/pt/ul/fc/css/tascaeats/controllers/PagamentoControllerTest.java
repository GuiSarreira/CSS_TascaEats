package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.PagamentoRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.PagamentoService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { PagamentoController.class, GlobalExceptionHandler.class })
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagamentoService pagamentoService;

    private Endereco endereco;
    private Pedido pedido;
    private Pagamento pagamento;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua F, 6", "6000-006", "Évora");
        Cliente cliente = new Cliente("c@test.com", "Ana", "pass", endereco);
        pedido = new Pedido(cliente, endereco);
        pagamento = new Multibanco(pedido, 25.0, "123456789", "Visa");
    }

    // ─── POST /api/pedidos/{id}/pagamento ─────────────────────────────────────

    @Test
    void processarPagamento_Sucesso_Returns200() throws Exception {
        when(pagamentoService.processarPagamento(eq(1L), any(), any(), any(), any())).thenReturn(pagamento);

        PagamentoRequest request = new PagamentoRequest();
        request.setTipoPagamento("MULTIBANCO");
        request.setDadosExtra("ref:123456789");

        mockMvc.perform(post("/api/pedidos/1/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoPagamento").value("MULTIBANCO"))
                .andExpect(jsonPath("$.preco").value(25.0));
    }

    @Test
    void processarPagamento_PedidoNaoCREATED_Returns422() throws Exception {
        when(pagamentoService.processarPagamento(eq(1L), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Pedido não está no estado CREATED"));

        PagamentoRequest request = new PagamentoRequest();
        request.setTipoPagamento("DINHEIRO");

        mockMvc.perform(post("/api/pedidos/1/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ─── GET /api/pedidos/{id}/pagamento ──────────────────────────────────────

    @Test
    void buscarPagamentoPorPedido_Sucesso_Returns200() throws Exception {
        when(pagamentoService.buscarPorPedido(1L)).thenReturn(Optional.of(pagamento));

        mockMvc.perform(get("/api/pedidos/1/pagamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoPagamento").value("MULTIBANCO"));
    }

    @Test
    void buscarPagamentoPorPedido_NaoExiste_Returns404() throws Exception {
        when(pagamentoService.buscarPorPedido(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pedidos/99/pagamento"))
                .andExpect(status().isNotFound());
    }
}
