package pt.ul.fc.css.tascaeats.repositories.specs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.MenuRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para as Specifications de Menu e Restaurante.
 * Usa @DataJpaTest com H2 em memória para executar queries reais.
 */
@DataJpaTest
class SpecificationsIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    private Menu menuBarato;
    private Menu menuCaro;
    private Menu menuVazio;

    @BeforeEach
    void setUp() {
        // ── Produtos ─────────────────────────────────────────────────────
        Produto sopa = new Produto("Sopa do Dia", "Caldo verde", 3.00, new ArrayList<>(), "Entrada");
        Produto prego = new Produto("Prego no Pão", "Carne grelhada", 5.00, new ArrayList<>(), "Prato Principal");
        Produto bacalhau = new Produto("Bacalhau à Brás", "Com batata palha", 12.00, new ArrayList<>(),
                "Prato Principal");
        Produto lagosta = new Produto("Lagosta Grelhada", "Premium", 35.00, new ArrayList<>(), "Prato Principal");

        em.persist(sopa);
        em.persist(prego);
        em.persist(bacalhau);
        em.persist(lagosta);

        // ── Menus ─────────────────────────────────────────────────────────
        // Menu barato: sopa (3) + prego (5) → média = 4.00
        menuBarato = new Menu("Menu Económico", "Menu acessível", new ArrayList<>(), new ArrayList<>());
        em.persist(menuBarato);
        menuBarato.getProdutos().add(sopa);
        menuBarato.getProdutos().add(prego);
        sopa.getMenus().add(menuBarato);
        prego.getMenus().add(menuBarato);

        // Menu caro: bacalhau (12) + lagosta (35) → média = 23.50
        menuCaro = new Menu("Menu Premium", "Menu gourmet", new ArrayList<>(), new ArrayList<>());
        em.persist(menuCaro);
        menuCaro.getProdutos().add(bacalhau);
        menuCaro.getProdutos().add(lagosta);
        bacalhau.getMenus().add(menuCaro);
        lagosta.getMenus().add(menuCaro);

        // Menu vazio: sem produtos
        menuVazio = new Menu("Menu Vazio", "Ainda sem produtos", new ArrayList<>(), new ArrayList<>());
        em.persist(menuVazio);

        em.flush();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MenuSpecifications
    // ═══════════════════════════════════════════════════════════════════════

    // ─── comNome ──────────────────────────────────────────────────────────

    @Test
    void comNome_FiltraPorNomeParcial() {
        List<Menu> resultado = menuRepository.findAll(MenuSpecifications.comNome("econ"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Económico");
    }

    @Test
    void comNome_IgnoraCase() {
        List<Menu> resultado = menuRepository.findAll(MenuSpecifications.comNome("PREMIUM"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Premium");
    }

    @Test
    void comNome_NullReturnsTodos() {
        List<Menu> resultado = menuRepository.findAll(MenuSpecifications.comNome(null));

        assertThat(resultado).hasSize(3);
    }

    @Test
    void comNome_SemResultados() {
        List<Menu> resultado = menuRepository.findAll(MenuSpecifications.comNome("inexistente"));

        assertThat(resultado).isEmpty();
    }

    // ─── quantidadeProdutosEntre ─────────────────────────────────────────

    @Test
    void quantidadeProdutosEntre_ComMinimo() {
        Specification<Menu> spec = MenuSpecifications.quantidadeProdutosEntre(2, null);
        List<Menu> resultado = menuRepository.findAll(spec);

        // menuBarato(2) e menuCaro(2) — menuVazio(0) excluído
        assertThat(resultado).hasSize(2);
        assertThat(resultado).noneMatch(m -> m.getNome().equals("Menu Vazio"));
    }

    @Test
    void quantidadeProdutosEntre_ComMaximo() {
        Specification<Menu> spec = MenuSpecifications.quantidadeProdutosEntre(null, 0);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Vazio");
    }

    @Test
    void quantidadeProdutosEntre_ComIntervalo() {
        Specification<Menu> spec = MenuSpecifications.quantidadeProdutosEntre(1, 2);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void quantidadeProdutosEntre_NullReturnsTodos() {
        Specification<Menu> spec = MenuSpecifications.quantidadeProdutosEntre(null, null);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(3);
    }

    // ─── precoMedioEntre ─────────────────────────────────────────────────

    @Test
    void precoMedioEntre_FiltraBaratos() {
        // média <= 5.0 → apenas menuBarato (média 4.0)
        Specification<Menu> spec = MenuSpecifications.precoMedioEntre(null, 5.0);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Económico");
    }

    @Test
    void precoMedioEntre_FiltraCaros() {
        // média >= 20.0 → apenas menuCaro (média 23.5)
        Specification<Menu> spec = MenuSpecifications.precoMedioEntre(20.0, null);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Premium");
    }

    @Test
    void precoMedioEntre_ComIntervalo() {
        // 3.0 <= média <= 5.0 → menuBarato (4.0)
        Specification<Menu> spec = MenuSpecifications.precoMedioEntre(3.0, 5.0);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Económico");
    }

    @Test
    void precoMedioEntre_NullReturnsTodos() {
        Specification<Menu> spec = MenuSpecifications.precoMedioEntre(null, null);
        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(3);
    }

    // ─── Combinação de filtros ──────────────────────────────────────────

    @Test
    void filtrosCombinados_NomeEQuantidade() {
        Specification<Menu> spec = Specification
                .where(MenuSpecifications.comNome("menu"))
                .and(MenuSpecifications.quantidadeProdutosEntre(2, null));

        List<Menu> resultado = menuRepository.findAll(spec);

        // "Menu Económico" e "Menu Premium" têm 2 produtos cada
        assertThat(resultado).hasSize(2);
    }

    @Test
    void filtrosCombinados_NomeEPrecoMedio() {
        Specification<Menu> spec = Specification
                .where(MenuSpecifications.comNome("menu"))
                .and(MenuSpecifications.precoMedioEntre(20.0, 30.0));

        List<Menu> resultado = menuRepository.findAll(spec);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Menu Premium");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RestauranteSpecifications
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void comNome_RestauranteFiltraPorNomeParcial() {
        Admin admin = new Admin("admin@test.com", "Admin", "pass");
        em.persist(admin);

        Restaurante r1 = new Restaurante("Tasca Lisboa", new Endereco("Rua A", "1000-001", "Lisboa"),
                "111111111", "Portuguesa", LocalTime.of(10, 0), LocalTime.of(22, 0));
        r1.setAdmin(admin);
        em.persist(r1);

        Restaurante r2 = new Restaurante("Sushi Tokyo", new Endereco("Rua B", "1000-002", "Lisboa"),
                "222222222", "Japonesa", LocalTime.of(12, 0), LocalTime.of(23, 0));
        r2.setAdmin(admin);
        em.persist(r2);
        em.flush();

        List<Restaurante> resultado = restauranteRepository.findAll(
                RestauranteSpecifications.comNome("tasca"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Tasca Lisboa");
    }

    @Test
    void comTipoCozinha_FiltraCorretamente() {
        Admin admin = new Admin("admin2@test.com", "Admin2", "pass");
        em.persist(admin);

        Restaurante r1 = new Restaurante("Tasca", new Endereco("Rua A", "1000-001", "Lisboa"),
                "333333333", "Portuguesa", LocalTime.of(10, 0), LocalTime.of(22, 0));
        r1.setAdmin(admin);
        em.persist(r1);

        Restaurante r2 = new Restaurante("Sakura", new Endereco("Rua B", "1000-002", "Lisboa"),
                "444444444", "Japonesa", LocalTime.of(12, 0), LocalTime.of(23, 0));
        r2.setAdmin(admin);
        em.persist(r2);
        em.flush();

        List<Restaurante> resultado = restauranteRepository.findAll(
                RestauranteSpecifications.comTipoCozinha("portuguesa"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Tasca");
    }

    @Test
    void abertoNoHorario_FiltraCorretamente() {
        Admin admin = new Admin("admin3@test.com", "Admin3", "pass");
        em.persist(admin);

        // Abre 10h–15h
        Restaurante r1 = new Restaurante("Almoço Só", new Endereco("Rua A", "1000-001", "Lisboa"),
                "555555555", "Portuguesa", LocalTime.of(10, 0), LocalTime.of(15, 0));
        r1.setAdmin(admin);
        em.persist(r1);

        // Abre 18h–23h
        Restaurante r2 = new Restaurante("Jantar Só", new Endereco("Rua B", "1000-002", "Lisboa"),
                "666666666", "Italiana", LocalTime.of(18, 0), LocalTime.of(23, 0));
        r2.setAdmin(admin);
        em.persist(r2);
        em.flush();

        // Às 14h, só r1 está aberto
        List<Restaurante> resultado = restauranteRepository.findAll(
                RestauranteSpecifications.abertoNoHorario(LocalTime.of(14, 0)));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Almoço Só");

        // Às 20h, só r2 está aberto
        resultado = restauranteRepository.findAll(
                RestauranteSpecifications.abertoNoHorario(LocalTime.of(20, 0)));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Jantar Só");
    }

    @Test
    void abertoNoHorario_NullReturnsTodos() {
        Admin admin = new Admin("admin4@test.com", "Admin4", "pass");
        em.persist(admin);

        Restaurante r = new Restaurante("Qualquer", new Endereco("Rua X", "1000-001", "Lisboa"),
                "777777777", "Portuguesa", LocalTime.of(10, 0), LocalTime.of(22, 0));
        r.setAdmin(admin);
        em.persist(r);
        em.flush();

        List<Restaurante> resultado = restauranteRepository.findAll(
                RestauranteSpecifications.abertoNoHorario(null));

        assertThat(resultado).isNotEmpty();
    }

    @Test
    void comMinimoAvaliacoes_FiltraCorretamente() {
        Admin admin = new Admin("admin5@test.com", "Admin5", "pass");
        em.persist(admin);
        Cliente cliente = new Cliente("c@c.com", "Cliente", "pass",
                new Endereco("Rua C", "1000-003", "Lisboa"));
        em.persist(cliente);

        Restaurante r1 = new Restaurante("Muito Avaliado", new Endereco("Rua A", "1000-001", "Lisboa"),
                "888888888", "Portuguesa", LocalTime.of(10, 0), LocalTime.of(22, 0));
        r1.setAdmin(admin);
        em.persist(r1);

        // Adiciona 3 avaliações (cada uma de um Cliente distinto, ligada a um Pedido
        // distinto)
        for (int i = 0; i < 3; i++) {
            Cliente clienteAv = new Cliente("av" + i + "@c.com", "Cliente" + i, "pass",
                    new Endereco("Rua C", "1000-003", "Lisboa"));
            em.persist(clienteAv);
            Pedido pedido = new Pedido(clienteAv, new Endereco("Rua C", "1000-003", "Lisboa"));
            em.persist(pedido);
            Avaliacao av = new Avaliacao(4 + i % 2, "Boa " + i, clienteAv, r1, pedido);
            em.persist(av);
        }

        Restaurante r2 = new Restaurante("Pouco Avaliado", new Endereco("Rua B", "1000-002", "Lisboa"),
                "999999999", "Italiana", LocalTime.of(12, 0), LocalTime.of(23, 0));
        r2.setAdmin(admin);
        em.persist(r2);
        em.flush();

        // mínimo 2 avaliações → apenas r1
        List<Restaurante> resultado = restauranteRepository.findAll(
                RestauranteSpecifications.comMinimoAvaliacoes(2));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Muito Avaliado");
    }
}
