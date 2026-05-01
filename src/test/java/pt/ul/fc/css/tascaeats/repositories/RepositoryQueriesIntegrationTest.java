package pt.ul.fc.css.tascaeats.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pt.ul.fc.css.tascaeats.entities.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para as queries de negócio (Fase 1) dos repositórios.
 * - Produtos mais vendidos
 * - Restaurantes com maior volume de vendas
 * - Restaurantes com mais pedidos
 *
 * Usa @DataJpaTest com H2 em memória para executar queries reais.
 */
@DataJpaTest
class RepositoryQueriesIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    private Restaurante restaurante1;
    private Restaurante restaurante2;
    private Produto produto1;
    private Produto produto2;
    private Produto produto3;

    @BeforeEach
    void setUp() {
        // ── Admin (obrigatório para Restaurante) ───────────────────────────
        Admin admin = new Admin("admin@test.com", "Admin Test", "123456");
        em.persist(admin);

        // ── Restaurantes ──────────────────────────────────────────────────
        Endereco morada1 = new Endereco("Rua da Tasca", "1000-001", "Lisboa");
        restaurante1 = new Restaurante("Tasca Portuguesa", morada1, "123456789");
        restaurante1.setAberto(true);
        restaurante1.setAdmin(admin);

        Endereco morada2 = new Endereco("Rua de Roma", "4000-001", "Porto");
        restaurante2 = new Restaurante("Pizzaria Roma", morada2, "987654321");
        restaurante2.setAberto(true);
        restaurante2.setAdmin(admin);

        em.persist(restaurante1);
        em.persist(restaurante2);

        // ── Produtos ──────────────────────────────────────────────────────
        produto1 = new Produto("Francesinha", "Sandes portuguesa", 8.50, new ArrayList<>(), "Prato Principal");
        produto2 = new Produto("Caldo Verde", "Sopa quente", 3.00, new ArrayList<>(), "Entrada");
        produto3 = new Produto("Pizza Margherita", "Pizza clássica", 10.00, new ArrayList<>(), "Prato Principal");

        em.persist(produto1);
        em.persist(produto2);
        em.persist(produto3);

        // ── Menus ─────────────────────────────────────────────────────────
        Menu menu1 = new Menu("Menu Típico", "Menu português", 
                            new ArrayList<>(java.util.Arrays.asList(produto1, produto2)), 
                            new ArrayList<>(java.util.Arrays.asList(restaurante1)));
        Menu menu2 = new Menu("Menu Especial", "Menu italiano", 
                            new ArrayList<>(java.util.Arrays.asList(produto3)), 
                            new ArrayList<>(java.util.Arrays.asList(restaurante2)));

        em.persist(menu1);
        em.persist(menu2);
        
        restaurante1.setMenu(menu1);
        restaurante2.setMenu(menu2);

        // ── Clientes ──────────────────────────────────────────────────────
        Cliente cliente1 = new Cliente("cliente1@mail.com", "Cliente Um", "123456");
        Cliente cliente2 = new Cliente("cliente2@mail.com", "Cliente Dois", "123456");

        em.persist(cliente1);
        em.persist(cliente2);

        // Pedido 1: 3 unidades Francesinha (8.50 cada) + 2 unidades Caldo Verde (3.00 cada)
        Endereco entrega1 = new Endereco("Rua A", "1100-100", "Lisboa");
        Pedido pedido1 = new Pedido(cliente1, entrega1);
        pedido1.setStatus(PedidoStatus.DELIVERED);

        ProdutoPedido pp1_1 = new ProdutoPedido(produto1, 3);
        ProdutoPedido pp1_2 = new ProdutoPedido(produto2, 2);
        pedido1.adicionarProduto(pp1_1);
        pedido1.adicionarProduto(pp1_2);
        em.persist(pedido1);

        // Pedido 2: 2 unidades Francesinha (8.50 cada)
        Pedido pedido2 = new Pedido(cliente1, entrega1);
        pedido2.setStatus(PedidoStatus.DELIVERED);

        ProdutoPedido pp2_1 = new ProdutoPedido(produto1, 2);
        pedido2.adicionarProduto(pp2_1);
        em.persist(pedido2);

        // Pedido 3: 1 unidade Pizza Margherita (10.00)
        Endereco entrega2 = new Endereco("Rua B", "4100-100", "Porto");
        Pedido pedido3 = new Pedido(cliente2, entrega2);
        pedido3.setStatus(PedidoStatus.DELIVERED);

        ProdutoPedido pp3_1 = new ProdutoPedido(produto3, 1);
        pedido3.adicionarProduto(pp3_1);
        em.persist(pedido3);

        // Pedido 4: 4 unidades Pizza Margherita (10.00) - restaurante2
        Pedido pedido4 = new Pedido(cliente2, entrega2);
        pedido4.setStatus(PedidoStatus.DELIVERED);

        ProdutoPedido pp4_1 = new ProdutoPedido(produto3, 4);
        pedido4.adicionarProduto(pp4_1);
        em.persist(pedido4);

        // Pedido CANCELLED (não deve ser incluído nas queries)
        Pedido pedidoCancelado = new Pedido(cliente1, entrega1);
        pedidoCancelado.setStatus(PedidoStatus.CANCELLED);

        ProdutoPedido ppCancelado = new ProdutoPedido(produto1, 10);
        pedidoCancelado.adicionarProduto(ppCancelado);
        em.persist(pedidoCancelado);

        em.flush();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Testes findProdutosMaisVendidos()
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testFindProdutosMaisVendidos_ShouldReturnProductsOrderedByQuantity() {
        List<Object[]> results = produtoRepository.findProdutosMaisVendidos();

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3); // 3 produtos vendidos

        // Esperado: Pizza (5 unidades) > Francesinha (5 unidades) > Caldo Verde (2 unidades)
        // Nota: quando há empate, a ordem pode variar

        // Primeiro resultado deve ser um dos top 2
        Produto firstProduct = (Produto) results.get(0)[0];
        Long firstQuantity = ((Number) results.get(0)[1]).longValue();
        assertThat(firstQuantity).isEqualTo(5L);
        assertThat(firstProduct.getNome()).isIn("Pizza Margherita", "Francesinha");

        // Último resultado deve ser Caldo Verde (2 unidades)
        Produto lastProduct = (Produto) results.get(results.size() - 1)[0];
        Long lastQuantity = ((Number) results.get(results.size() - 1)[1]).longValue();
        assertThat(lastQuantity).isEqualTo(2L);
        assertThat(lastProduct.getNome()).isEqualTo("Caldo Verde");
    }

    @Test
    void testFindProdutosMaisVendidos_ShouldNotIncludeCancelledOrders() {
        List<Object[]> results = produtoRepository.findProdutosMaisVendidos();

        // Verificar que as quantidades não incluem o pedido cancelado
        for (Object[] result : results) {
            Produto produto = (Produto) result[0];
            Long quantidade = ((Number) result[1]).longValue();

            if (produto.getNome().equals("Francesinha")) {
                // 3 (pedido1) + 2 (pedido2) = 5, NÃO inclui 10 do pedido cancelado
                assertThat(quantidade).isEqualTo(5L);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Testes findRestaurantesComMaisPedidos()
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testFindRestaurantesComMaisPedidos_ShouldReturnRestaurantsOrderedByOrderCount() {
        List<Object[]> results = restauranteRepository.findRestaurantesComMaisPedidos();

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2); // 2 restaurantes com pedidos

        // Restaurante 1: 2 pedidos (DELIVERED)
        // Restaurante 2: 2 pedidos (DELIVERED)
        // Ambos têm a mesma quantidade

        // Verificar que todos têm pelo menos 2 pedidos
        for (Object[] result : results) {
            Long orderCount = ((Number) result[1]).longValue();
            assertThat(orderCount).isEqualTo(2L);
        }
    }

    @Test
    void testFindRestaurantesComMaisPedidos_ShouldNotIncludeCancelledOrders() {
        List<Object[]> results = restauranteRepository.findRestaurantesComMaisPedidos();

        for (Object[] result : results) {
            Restaurante restaurante = (Restaurante) result[0];
            Long orderCount = ((Number) result[1]).longValue();

            if (restaurante.getNome().equals("Tasca Portuguesa")) {
                // 2 pedidos DELIVERED (pedido1 e pedido2)
                // NÃO inclui: pedidoCancelado (status = CANCELLED)
                assertThat(orderCount).isEqualTo(2L);
            }
        }
    }

    @Test
    void testFindRestaurantesComMaisPedidos_ShouldCountDistinctOrders() {
        List<Object[]> results = restauranteRepository.findRestaurantesComMaisPedidos();

        // Verificar que contagem é por pedido, não por produto-pedido
        // Por exemplo, se um pedido tem 2 produtos, deve contar como 1 pedido
        assertThat(results).isNotEmpty();

        long totalOrders = 0;
        for (Object[] result : results) {
            Long count = ((Number) result[1]).longValue();
            totalOrders += count;
        }

        // Esperado: 4 pedidos DELIVERED no total (2 por restaurante)
        assertThat(totalOrders).isEqualTo(4L);
    }
}
