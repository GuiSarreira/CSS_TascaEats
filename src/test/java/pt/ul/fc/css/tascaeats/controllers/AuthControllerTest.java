package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.dto.LoginRequest;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.exceptions.GlobalExceptionHandler;
import pt.ul.fc.css.tascaeats.services.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua Login, 1", "1000-001", "Lisboa");
        cliente = new Cliente("user@test.com", "Utilizador Login", "pass", endereco);
    }

    @Test
    void login_Sucesso_Returns200() throws Exception {
        when(authService.login(any(), any())).thenReturn(cliente);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("qualquercoisa");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void login_CredenciaisInvalidas_Returns404() throws Exception {
        when(authService.login(any(), any()))
                .thenThrow(new RuntimeException("Credenciais inválidas ou utilizador inativo."));

        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@test.com");
        request.setPassword("pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
