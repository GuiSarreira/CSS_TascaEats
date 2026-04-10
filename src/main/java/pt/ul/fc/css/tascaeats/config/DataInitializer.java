package pt.ul.fc.css.tascaeats.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;

/**
 * Preenche a base de dados com dados iniciais quando a aplicação arranca.
 * Facilita a demonstração sem necessidade de inserção manual pelo Swagger.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RestauranteService restauranteService;
    private final ProdutoService produtoService;

    public DataInitializer(UserService userService,
                           RestauranteService restauranteService,
                           ProdutoService produtoService) {
        this.userService = userService;
        this.restauranteService = restauranteService;
        this.produtoService = produtoService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // ── Utilizadores ─────────────────────────────────────────────
            Admin admin = userService.registarAdmin(
                    "admin@tascaeats.pt", "Carlos Mendes", "admin123");

            userService.registarCliente(
                    "ana@tascaeats.pt", "Ana Silva", "pass123",
                    new Endereco("Rua das Flores, 10", "1200-192", "Lisboa"));

            userService.registarEntregador(
                    "bruno@tascaeats.pt", "Bruno Costa", "pass123",
                    "Mota", "Lisboa");

            // ── Restaurante ───────────────────────────────────────────────
            Restaurante restaurante = restauranteService.criarRestaurante(
                    "Tasca Lisboa",
                    new Endereco("Av. da Liberdade, 100", "1250-096", "Lisboa"),
                    "123456789",
                    admin.getId());

            restauranteService.alterarEstadoAbertura(restaurante.getId(), true);

            // ── Produtos no menu ──────────────────────────────────────────
            Produto prego = new Produto("Prego no Pão", "Carne de vitela grelhada", 5.50, null);
            prego.setDisponivel(true);
            produtoService.criarProduto(restaurante.getId(), prego);

            Produto sopa = new Produto("Sopa do Dia", "Caldo verde com broa", 2.50, null);
            sopa.setDisponivel(true);
            produtoService.criarProduto(restaurante.getId(), sopa);

            Produto peixe = new Produto("Bacalhau à Brás", "Com batata palha e ovos", 9.50, null);
            peixe.setDisponivel(true);
            produtoService.criarProduto(restaurante.getId(), peixe);

            System.out.println("\u001B[32m[DataInitializer] Dados iniciais carregados com sucesso!\u001B[0m");
            System.out.println("\u001B[36m  Admin     → id=" + admin.getId() + "  email=admin@tascaeats.pt\u001B[0m");
            System.out.println("\u001B[36m  Cliente   → email=ana@tascaeats.pt\u001B[0m");
            System.out.println("\u001B[36m  Entregador→ email=bruno@tascaeats.pt\u001B[0m");
            System.out.println("\u001B[36m  Restaurante → id=" + restaurante.getId() + "  nome=Tasca Lisboa (ABERTO)\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[DataInitializer] Erro ao carregar dados: " + e.getMessage() + "\u001B[0m");
        }
    }
}
