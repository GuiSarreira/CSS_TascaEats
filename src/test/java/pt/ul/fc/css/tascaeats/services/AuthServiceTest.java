package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private AuthService authService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua Auth, 1", "1000-001", "Lisboa");
        cliente = new Cliente("user@test.com", "Utilizador Ativo", "qualquercoisa", endereco);
    }

    @Test
    void login_UtilizadorAtivoExistente_ReturnsUser() {
        when(userRepository.findByEmailAndAtivoTrue("user@test.com"))
                .thenReturn(Optional.of(cliente));

        User resultado = authService.login("user@test.com", "qualquercoisa");

        assertThat(resultado).isEqualTo(cliente);
        assertThat(resultado.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void login_PasswordErrada_AindaRetornaUser() {
        // Mock auth: qualquer password é aceite
        when(userRepository.findByEmailAndAtivoTrue("user@test.com"))
                .thenReturn(Optional.of(cliente));

        User resultado = authService.login("user@test.com", "passwordErrada123");

        assertThat(resultado).isNotNull();
    }

    @Test
    void login_UtilizadorInexistente_ThrowsRuntimeException() {
        when(userRepository.findByEmailAndAtivoTrue("naoexiste@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("naoexiste@test.com", "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("inválidas");
    }

    @Test
    void login_UtilizadorInativo_ThrowsRuntimeException() {
        // Conta desativada → findByEmailAndAtivoTrue devolve empty
        when(userRepository.findByEmailAndAtivoTrue("inativo@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("inativo@test.com", "pass"))
                .isInstanceOf(RuntimeException.class);
    }
}
