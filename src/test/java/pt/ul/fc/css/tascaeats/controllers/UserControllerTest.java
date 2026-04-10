package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { UserController.class, GlobalExceptionHandler.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private Endereco endereco;
    private Cliente cliente;
    private Admin admin;
    private Entregador entregador;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua D, 4", "4400-004", "Vila Nova de Gaia");
        cliente = new Cliente("c@test.com", "Maria", "pass", endereco);
        admin = new Admin("a@test.com", "Carlos", "pass");
        entregador = new Entregador("e@test.com", "João", "pass", "moto", "Lisboa");
    }

    // ─── POST /api/users/clientes ─────────────────────────────────────────────

    @Test
    void registarCliente_Sucesso_Returns201() throws Exception {
        when(userService.registarCliente(any(), any(), any(), any())).thenReturn(cliente);

        RegistarClienteRequest request = new RegistarClienteRequest();
        request.setEmail("c@test.com");
        request.setNome("Maria");
        request.setPassword("pass");
        request.setMorada(endereco);

        mockMvc.perform(post("/api/users/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("c@test.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void registarCliente_EmailDuplicado_Returns400() throws Exception {
        when(userService.registarCliente(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Email já registado: c@test.com"));

        RegistarClienteRequest request = new RegistarClienteRequest();
        request.setEmail("c@test.com");
        request.setNome("Maria");
        request.setPassword("pass");
        request.setMorada(endereco);

        mockMvc.perform(post("/api/users/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── POST /api/users/admins ───────────────────────────────────────────────

    @Test
    void registarAdmin_Sucesso_Returns201() throws Exception {
        when(userService.registarAdmin(any(), any(), any())).thenReturn(admin);

        RegistarAdminRequest request = new RegistarAdminRequest();
        request.setEmail("a@test.com");
        request.setNome("Carlos");
        request.setPassword("pass");

        mockMvc.perform(post("/api/users/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // ─── POST /api/users/entregadores ─────────────────────────────────────────

    @Test
    void registarEntregador_Sucesso_Returns201() throws Exception {
        when(userService.registarEntregador(any(), any(), any(), any(), any())).thenReturn(entregador);

        RegistarEntregadorRequest request = new RegistarEntregadorRequest();
        request.setEmail("e@test.com");
        request.setNome("João");
        request.setPassword("pass");
        request.setVeiculo("moto");
        request.setZonaAtuacao("Lisboa");

        mockMvc.perform(post("/api/users/entregadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ENTREGADOR"));
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────

    @Test
    void listarTodos_Returns200() throws Exception {
        when(userService.listarTodosAtivos()).thenReturn(List.of(cliente, admin));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    void buscarPorId_Sucesso_Returns200() throws Exception {
        when(userService.buscarPorId(1L)).thenReturn(cliente);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("c@test.com"));
    }

    @Test
    void buscarPorId_NaoEncontrado_Returns404() throws Exception {
        when(userService.buscarPorId(99L))
                .thenThrow(new RuntimeException("Utilizador não encontrado com ID: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── DELETE /api/users/{id} ───────────────────────────────────────────────

    @Test
    void remover_Sucesso_Returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void remover_ComPedidosAtivos_Returns422() throws Exception {
        doThrow(new IllegalStateException("Não é possível remover cliente com pedidos ativos."))
                .when(userService).removerUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // ─── GET /api/users/email ─────────────────────────────────────────────────

    @Test
    void buscarPorEmail_Sucesso_Returns200() throws Exception {
        when(userService.buscarPorEmail("c@test.com")).thenReturn(Optional.of(cliente));

        mockMvc.perform(get("/api/users/email").param("email", "c@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("c@test.com"));
    }
}
