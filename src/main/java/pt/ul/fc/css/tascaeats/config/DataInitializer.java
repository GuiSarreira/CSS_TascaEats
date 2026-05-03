package pt.ul.fc.css.tascaeats.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.AvaliacaoRepository;
import pt.ul.fc.css.tascaeats.repositories.MenuRepository;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.repositories.UserRepository;
import pt.ul.fc.css.tascaeats.services.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Preenche a base de dados com dados iniciais quando a aplicação arranca.
 * Facilita a demonstração sem necessidade de inserção manual pelo Swagger.
 */
@Component
public class DataInitializer implements CommandLineRunner {

        private final UserService userService;
        private final RestauranteService restauranteService;
        private final ProdutoService produtoService;
        private final PedidoService pedidoService;
        private final PagamentoService pagamentoService;
        private final EntregaService entregaService;
        private final MenuService menuService;

        private final AvaliacaoRepository avaliacaoRepository;
        private final UserRepository userRepository;
        private final RestauranteRepository restauranteRepository;
        private final PedidoRepository pedidoRepository;
        private final MenuRepository menuRepository;

        public DataInitializer(UserService userService,
                        RestauranteService restauranteService,
                        ProdutoService produtoService,
                        PedidoService pedidoService,
                        PagamentoService pagamentoService,
                        EntregaService entregaService,
                        MenuService menuService,
                        AvaliacaoRepository avaliacaoRepository,
                        UserRepository userRepository,
                        RestauranteRepository restauranteRepository,
                        PedidoRepository pedidoRepository,
                        MenuRepository menuRepository) {
                this.userService = userService;
                this.restauranteService = restauranteService;
                this.produtoService = produtoService;
                this.pedidoService = pedidoService;
                this.pagamentoService = pagamentoService;
                this.entregaService = entregaService;
                this.menuService = menuService;
                this.avaliacaoRepository = avaliacaoRepository;
                this.userRepository = userRepository;
                this.restauranteRepository = restauranteRepository;
                this.pedidoRepository = pedidoRepository;
                this.menuRepository = menuRepository;
        }

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                try {
                        // ── Utilizadores ─────────────────────────────────────────────
                        Admin adminPrincipal = getOrCreateAdmin("admin@tascaeats.pt", "Carlos Mendes", "admin123");
                        Admin adminSecundario = getOrCreateAdmin("admin2@tascaeats.pt", "Marta Rocha", "admin123");

                        Cliente clienteAna = getOrCreateCliente(
                                        "ana@tascaeats.pt", "Ana Silva", "pass123",
                                        new Endereco("Rua das Flores, 10", "1200-192", "Lisboa"));
                        Cliente clienteJoao = getOrCreateCliente(
                                        "joao@tascaeats.pt", "João Pires", "pass123",
                                        new Endereco("Rua do Ouro, 21", "1100-061", "Lisboa"));
                        getOrCreateCliente(
                                        "rita@tascaeats.pt", "Rita Gomes", "pass123",
                                        new Endereco("Rua das Oliveiras, 2", "4050-449", "Porto"));

                        Entregador entregadorBruno = getOrCreateEntregador(
                                        "bruno@tascaeats.pt", "Bruno Costa", "pass123", "Mota", "Lisboa");
                        getOrCreateEntregador(
                                        "ines@tascaeats.pt", "Inês Barros", "pass123", "Bicicleta", "Lisboa");
                        getOrCreateEntregador(
                                        "mario@tascaeats.pt", "Mário Lima", "pass123", "Carro", "Porto");
                        getOrCreateEntregador(
                                        "pedro.removivel@tascaeats.pt", "Pedro Removível", "pass123", "Mota", "Braga");

                        // Garantir que o entregador base está disponível para as demos de atribuição.
                        userService.atualizarDisponibilidadeEntregador(entregadorBruno.getId(), true);

                        ensureTelemovel(adminPrincipal, "911111111");
                        ensureTelemovel(adminSecundario, "922222222");
                        ensureTelemovel(clienteAna, "933333333");
                        ensureTelemovel(clienteJoao, "944444444");
                        ensureTelemovel(entregadorBruno, "955555555");

                        // ── Restaurantes ──────────────────────────────────────────────
                        Restaurante tascaLisboa = getOrCreateRestaurante(
                                        "123456789",
                                        "Tasca Lisboa",
                                        new Endereco("Av. da Liberdade, 100", "1250-096", "Lisboa"),
                                        "Portuguesa",
                                        LocalTime.of(11, 0),
                                        LocalTime.of(23, 0),
                                        adminPrincipal,
                                        true);

                        Restaurante solarPorto = getOrCreateRestaurante(
                                        "987654321",
                                        "Solar do Bacalhau",
                                        new Endereco("Rua de Cedofeita, 80", "4050-175", "Porto"),
                                        "Portuguesa",
                                        LocalTime.of(12, 0),
                                        LocalTime.of(23, 30),
                                        adminSecundario,
                                        true);

                        Restaurante casaFechada = getOrCreateRestaurante(
                                        "333222111",
                                        "Casa Fechada",
                                        new Endereco("Rua Fechada, 5", "1000-001", "Lisboa"),
                                        "Tradicional",
                                        LocalTime.of(10, 0),
                                        LocalTime.of(22, 0),
                                        adminPrincipal,
                                        false);

                        Restaurante cantinaItaliana = getOrCreateRestaurante(
                                        "444555666",
                                        "Cantina Italiana",
                                        new Endereco("Rua Augusta, 220", "1100-060", "Lisboa"),
                                        "Italiana",
                                        LocalTime.of(12, 0),
                                        LocalTime.of(23, 30),
                                        adminSecundario,
                                        true);

                        Restaurante dragaoWok = getOrCreateRestaurante(
                                        "555666777",
                                        "Dragão Wok",
                                        new Endereco("Rua das Flores, 77", "4000-098", "Porto"),
                                        "Asiática",
                                        LocalTime.of(18, 0),
                                        LocalTime.of(23, 0),
                                        adminPrincipal,
                                        false);

                        // ── Produtos ──────────────────────────────────────────────────
                        Produto pregoLisboa = getOrCreateProduto(
                                        tascaLisboa,
                                        "Prego no Pão",
                                        "Carne de vitela grelhada",
                                        5.50,
                                        "Prato Principal",
                                        true);
                        Produto sopaLisboa = getOrCreateProduto(
                                        tascaLisboa,
                                        "Sopa do Dia",
                                        "Caldo verde com broa",
                                        2.50,
                                        "Entrada",
                                        true);
                        Produto bacalhauLisboa = getOrCreateProduto(
                                        tascaLisboa,
                                        "Bacalhau à Brás",
                                        "Com batata palha e ovos",
                                        9.50,
                                        "Prato Principal",
                                        true);
                        getOrCreateProduto(
                                        tascaLisboa,
                                        "Arroz de Pato",
                                        "Com chouriço e laranja",
                                        11.00,
                                        "Prato Principal",
                                        false);

                        Produto francesinhaPorto = getOrCreateProduto(
                                        solarPorto,
                                        "Francesinha",
                                        "Francesinha tradicional do Porto",
                                        10.00,
                                        "Prato Principal",
                                        true);
                        Produto caldoVerdePorto = getOrCreateProduto(
                                        solarPorto,
                                        "Caldo Verde",
                                        "Sopa tradicional",
                                        3.00,
                                        "Entrada",
                                        true);

                        getOrCreateProduto(
                                        casaFechada,
                                        "Bitoque da Casa",
                                        "Bitoque com ovo a cavalo",
                                        8.00,
                                        "Prato Principal",
                                        true);

                        getOrCreateProduto(
                                        cantinaItaliana,
                                        "Pizza Margherita",
                                        "Tomate, mozzarella e manjericão",
                                        8.90,
                                        "Prato Principal",
                                        true);
                        getOrCreateProduto(
                                        cantinaItaliana,
                                        "Tiramisu",
                                        "Sobremesa italiana clássica",
                                        4.20,
                                        "Sobremesa",
                                        true);
                        getOrCreateProduto(
                                        cantinaItaliana,
                                        "Limonada da Casa",
                                        "Limonada fresca com hortelã",
                                        2.20,
                                        "Bebida",
                                        true);

                        getOrCreateProduto(
                                        dragaoWok,
                                        "Noodles de Frango",
                                        "Noodles salteados com legumes",
                                        9.80,
                                        "Prato Principal",
                                        true);
                        getOrCreateProduto(
                                        dragaoWok,
                                        "Gyoza",
                                        "Raviolis japoneses grelhados",
                                        4.60,
                                        "Entrada",
                                        false);

                        // Garantir um menu com associação produto-restaurante para suportar
                        // validações de avaliações em pedidos seed.
                        ensureSharedMenu(tascaLisboa, solarPorto, pregoLisboa, francesinhaPorto);

                        seedPedidosSeNecessario(clienteAna, clienteJoao, entregadorBruno,
                                        pregoLisboa, sopaLisboa, bacalhauLisboa, francesinhaPorto, caldoVerdePorto,
                                        tascaLisboa);

                        printResumo(adminPrincipal, adminSecundario, tascaLisboa, solarPorto, casaFechada,
                                        cantinaItaliana, dragaoWok);

                } catch (Exception e) {
                        System.out.println("\u001B[31m[DataInitializer] Erro ao carregar dados: " + e.getMessage()
                                        + "\u001B[0m");
                }
        }

        private Admin getOrCreateAdmin(String email, String nome, String password) {
                Optional<User> existing = userRepository.findByEmail(email);
                if (existing.isPresent()) {
                        if (existing.get() instanceof Admin admin) {
                                return admin;
                        }
                        throw new IllegalStateException("Email já existe com tipo diferente: " + email);
                }
                return userService.registarAdmin(email, nome, password);
        }

        private Cliente getOrCreateCliente(String email, String nome, String password, Endereco morada) {
                Optional<User> existing = userRepository.findByEmail(email);
                if (existing.isPresent()) {
                        if (existing.get() instanceof Cliente cliente) {
                                return cliente;
                        }
                        throw new IllegalStateException("Email já existe com tipo diferente: " + email);
                }
                return userService.registarCliente(email, nome, password, morada);
        }

        private Entregador getOrCreateEntregador(String email, String nome, String password,
                        String veiculo, String zonaAtuacao) {
                Optional<User> existing = userRepository.findByEmail(email);
                if (existing.isPresent()) {
                        if (existing.get() instanceof Entregador entregador) {
                                return entregador;
                        }
                        throw new IllegalStateException("Email já existe com tipo diferente: " + email);
                }
                return userService.registarEntregador(email, nome, password, veiculo, zonaAtuacao);
        }

        private Restaurante getOrCreateRestaurante(String nif, String nome, Endereco morada,
                        String tipoCozinha, LocalTime abertura, LocalTime fecho,
                        Admin admin, boolean aberto) {
                Optional<Restaurante> existing = restauranteRepository.findByNif(nif);
                Restaurante restaurante = existing.orElseGet(() -> restauranteService.criarRestaurante(
                                nome, morada, nif, tipoCozinha, abertura, fecho, admin.getId()));

                if (restaurante.isAberto() != aberto) {
                        restauranteService.alterarEstadoAbertura(restaurante.getId(), aberto);
                        restaurante = restauranteService.buscarPorId(restaurante.getId());
                }
                return restaurante;
        }

        private Produto getOrCreateProduto(Restaurante restaurante, String nome, String descricao,
                        double preco, String categoria, boolean disponivel) {
                List<Produto> produtosExistentes = produtoService.listarMenuDoRestaurante(restaurante.getId());
                for (Produto produto : produtosExistentes) {
                        if (produto.getNome().equalsIgnoreCase(nome)) {
                                produtoService.alternarDisponibilidade(produto.getId(), disponivel);
                                return produtoService.buscarPorId(produto.getId());
                        }
                }

                Produto novo = new Produto(nome, descricao, preco, categoria);
                novo.setDisponivel(disponivel);
                return produtoService.criarProduto(restaurante.getId(), novo);
        }

        private void ensureSharedMenu(Restaurante restauranteA, Restaurante restauranteB,
                        Produto produtoA, Produto produtoB) {
                List<Menu> existentes = menuRepository.findByNomeContainingIgnoreCase("Menu Partilhado Seed");
                if (!existentes.isEmpty()) {
                        return;
                }

                menuService.criarMenu(
                                "Menu Partilhado Seed",
                                "Menu partilhado para demonstração de gestão de menus",
                                List.of(produtoA, produtoB),
                                List.of(restauranteA, restauranteB));
        }

        private void seedPedidosSeNecessario(Cliente clienteAna, Cliente clienteJoao, Entregador entregadorBruno,
                        Produto pregoLisboa, Produto sopaLisboa, Produto bacalhauLisboa,
                        Produto francesinhaPorto, Produto caldoVerdePorto,
                        Restaurante restauranteAvaliacao) {
                if (pedidoRepository.count() > 0) {
                        System.out.println(
                                        "\u001B[33m[DataInitializer] Pedidos já existentes — seed avançado de pedidos foi ignorado.\u001B[0m");
                        return;
                }

                // Pedido CREATED (bom para pagamento e cancelamento válido)
                pedidoService.criarPedido(
                                clienteAna.getId(),
                                new Endereco("Rua Seed A, 1", "1000-001", "Lisboa"),
                                Map.of(pregoLisboa.getId(), 1, sopaLisboa.getId(), 1));

                // Pedido PAID (bom para avançar estado)
                Pedido pedidoPaid = pedidoService.criarPedido(
                                clienteJoao.getId(),
                                new Endereco("Rua Seed B, 2", "1000-002", "Lisboa"),
                                Map.of(sopaLisboa.getId(), 2));
                pagamentoService.processarPagamento(pedidoPaid.getId(), "MULTIBANCO", "111 222 333", "VISA", null);

                // Pedido READY para atribuição manual
                Pedido pedidoReadyManual = pedidoService.criarPedido(
                                clienteAna.getId(),
                                new Endereco("Rua Seed C, 3", "1000-003", "Lisboa"),
                                Map.of(bacalhauLisboa.getId(), 1));
                pagamentoService.processarPagamento(pedidoReadyManual.getId(), "MBWAY", "912345678", null, null);
                pedidoService.avancarEstado(pedidoReadyManual.getId()); // PAID -> PREPARING
                pedidoService.avancarEstado(pedidoReadyManual.getId()); // PREPARING -> READY

                // Pedido READY para atribuição automática
                Pedido pedidoReadyAuto = pedidoService.criarPedido(
                                clienteJoao.getId(),
                                new Endereco("Rua Seed D, 4", "4050-100", "Porto"),
                                Map.of(francesinhaPorto.getId(), 1, caldoVerdePorto.getId(), 1));
                pagamentoService.processarPagamento(pedidoReadyAuto.getId(), "DINHEIRO", null, null, 20.0);
                pedidoService.avancarEstado(pedidoReadyAuto.getId());
                pedidoService.avancarEstado(pedidoReadyAuto.getId());

                // Pedido DELIVERED para validação negativa de cancelamento
                Pedido pedidoDelivered = pedidoService.criarPedido(
                                clienteAna.getId(),
                                new Endereco("Rua Seed E, 5", "1000-004", "Lisboa"),
                                Map.of(pregoLisboa.getId(), 1));
                pagamentoService.processarPagamento(pedidoDelivered.getId(), "MULTIBANCO", "999 888 777", "MASTERCARD",
                                null);
                pedidoService.avancarEstado(pedidoDelivered.getId());
                pedidoService.avancarEstado(pedidoDelivered.getId());
                Entrega entrega = entregaService.atribuirEntregador(pedidoDelivered.getId(), entregadorBruno.getId());
                entregaService.iniciarEntrega(entrega.getId());
                entregaService.concluirEntrega(entrega.getId());

                // Segunda entrega concluída para ter mais avaliações disponíveis na demo.
                Pedido pedidoDelivered2 = pedidoService.criarPedido(
                                clienteJoao.getId(),
                                new Endereco("Rua Seed F, 6", "1000-005", "Lisboa"),
                                Map.of(sopaLisboa.getId(), 1, pregoLisboa.getId(), 1));
                pagamentoService.processarPagamento(pedidoDelivered2.getId(), "MBWAY", "919191919", null, null);
                pedidoService.avancarEstado(pedidoDelivered2.getId());
                pedidoService.avancarEstado(pedidoDelivered2.getId());
                Entrega entrega2 = entregaService.atribuirEntregador(pedidoDelivered2.getId(), entregadorBruno.getId());
                entregaService.iniciarEntrega(entrega2.getId());
                entregaService.concluirEntrega(entrega2.getId());

                // Mais 3 pedidos DELIVERED sem avaliação para demonstração web (avaliar
                // pedido).
                Pedido pedidoDelivered3 = pedidoService.criarPedido(
                                clienteAna.getId(),
                                new Endereco("Rua Seed G, 7", "1000-006", "Lisboa"),
                                Map.of(bacalhauLisboa.getId(), 1));
                pagamentoService.processarPagamento(pedidoDelivered3.getId(), "MBWAY", "913333333", null, null);
                pedidoService.avancarEstado(pedidoDelivered3.getId());
                pedidoService.avancarEstado(pedidoDelivered3.getId());
                Entrega entrega3 = entregaService.atribuirEntregador(pedidoDelivered3.getId(), entregadorBruno.getId());
                entregaService.iniciarEntrega(entrega3.getId());
                entregaService.concluirEntrega(entrega3.getId());

                Pedido pedidoDelivered4 = pedidoService.criarPedido(
                                clienteAna.getId(),
                                new Endereco("Rua Seed H, 8", "1000-007", "Lisboa"),
                                Map.of(pregoLisboa.getId(), 1, sopaLisboa.getId(), 1));
                pagamentoService.processarPagamento(pedidoDelivered4.getId(), "MULTIBANCO", "123 123 123", "VISA",
                                null);
                pedidoService.avancarEstado(pedidoDelivered4.getId());
                pedidoService.avancarEstado(pedidoDelivered4.getId());
                Entrega entrega4 = entregaService.atribuirEntregador(pedidoDelivered4.getId(), entregadorBruno.getId());
                entregaService.iniciarEntrega(entrega4.getId());
                entregaService.concluirEntrega(entrega4.getId());

                Pedido pedidoDelivered5 = pedidoService.criarPedido(
                                clienteJoao.getId(),
                                new Endereco("Rua Seed I, 9", "4050-101", "Porto"),
                                Map.of(francesinhaPorto.getId(), 1));
                pagamentoService.processarPagamento(pedidoDelivered5.getId(), "DINHEIRO", null, null, 15.0);
                pedidoService.avancarEstado(pedidoDelivered5.getId());
                pedidoService.avancarEstado(pedidoDelivered5.getId());
                Entrega entrega5 = entregaService.atribuirEntregador(pedidoDelivered5.getId(), entregadorBruno.getId());
                entregaService.iniciarEntrega(entrega5.getId());
                entregaService.concluirEntrega(entrega5.getId());

                criarAvaliacaoSeed(
                                clienteJoao,
                                restauranteAvaliacao,
                                pedidoDelivered2,
                                4,
                                "Boa experiência no geral.");

                criarAvaliacaoSeed(
                                clienteJoao,
                                restauranteAvaliacao,
                                pedidoDelivered5,
                                5,
                                "Entrega impecável e muito rápida.");
        }

        private void criarAvaliacaoSeed(Cliente cliente, Restaurante restaurante, Pedido pedido,
                        int nota, String comentario) {
                if (avaliacaoRepository.findByPedidoId(pedido.getId()).isPresent()) {
                        return;
                }

                Restaurante restauranteAssociado = pedido.getProdutosPedido().stream()
                                .flatMap(pp -> pp.getProduto().getMenus().stream())
                                .flatMap(menu -> menu.getRestaurantes().stream())
                                .findFirst()
                                .orElse(restaurante);

                Avaliacao avaliacao = new Avaliacao(nota, comentario, cliente, restauranteAssociado, pedido);
                avaliacaoRepository.save(avaliacao);
        }

        private void ensureTelemovel(User user, String telemovel) {
                if (user.getTelemovel() == null || user.getTelemovel().isBlank()) {
                        user.setTelemovel(telemovel);
                        userRepository.save(user);
                }
        }

        private void printResumo(Admin adminPrincipal, Admin adminSecundario,
                        Restaurante tascaLisboa, Restaurante solarPorto, Restaurante casaFechada,
                        Restaurante cantinaItaliana, Restaurante dragaoWok) {
                System.out.println("\u001B[32m[DataInitializer] Dados iniciais carregados com sucesso!\u001B[0m");
                System.out.println("\u001B[36m  Admin 1     → id=" + adminPrincipal.getId()
                                + "  email=admin@tascaeats.pt\u001B[0m");
                System.out.println("\u001B[36m  Admin 2     → id=" + adminSecundario.getId()
                                + "  email=admin2@tascaeats.pt\u001B[0m");
                System.out.println(
                                "\u001B[36m  Clientes    → ana@tascaeats.pt, joao@tascaeats.pt, rita@tascaeats.pt\u001B[0m");
                System.out.println(
                                "\u001B[36m  Entregadores→ bruno@tascaeats.pt, ines@tascaeats.pt, mario@tascaeats.pt\u001B[0m");
                System.out.println("\u001B[36m  Restaurante  → id=" + tascaLisboa.getId()
                                + "  nome=Tasca Lisboa (ABERTO)\u001B[0m");
                System.out.println("\u001B[36m  Restaurante  → id=" + solarPorto.getId()
                                + "  nome=Solar do Bacalhau (ABERTO)\u001B[0m");
                System.out.println("\u001B[36m  Restaurante  → id=" + casaFechada.getId()
                                + "  nome=Casa Fechada (FECHADO)\u001B[0m");
                System.out.println("\u001B[36m  Restaurante  → id=" + cantinaItaliana.getId()
                                + "  nome=Cantina Italiana (ABERTO)\u001B[0m");
                System.out.println("\u001B[36m  Restaurante  → id=" + dragaoWok.getId()
                                + "  nome=Dragão Wok (FECHADO)\u001B[0m");
                System.out.println(
                                "\u001B[36m  Nota         → Pedidos seed só são criados quando a tabela de pedidos está vazia\u001B[0m");
        }
}
