package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.ProdutoResponse;
import pt.ul.fc.css.tascaeats.dto.RestauranteResponse;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.services.EntregaService;
import pt.ul.fc.css.tascaeats.services.MenuService;
import pt.ul.fc.css.tascaeats.services.PagamentoService;
import pt.ul.fc.css.tascaeats.services.PedidoService;
import pt.ul.fc.css.tascaeats.services.ProdutoService;
import pt.ul.fc.css.tascaeats.services.RestauranteService;
import pt.ul.fc.css.tascaeats.services.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para queries de negócio analíticas.
 *
 * Expõe os endpoints correspondentes às queries de negócio definidas no
 * enunciado da Fase 1 e Fase 2 — queries que agregam ou calculam métricas sobre
 * o estado do sistema.
 *
 * FASE 1 QUERIES:
 * GET /api/negocio/fase1/restaurantes/volume-vendas — Query 1: restaurantes com
 * maior volume de vendas (€)
 * GET /api/negocio/fase1/restaurantes/mais-pedidos — Query 2: morada do
 * restaurante com mais vendas
 * GET /api/negocio/fase1/pedidos/media-por-cliente-mes — Query 3: média de
 * pedidos por cliente por mês
 * GET /api/negocio/fase1/produtos/mais-vendidos — Query 4: produtos mais
 * vendidos da plataforma
 * GET /api/negocio/fase1/pagamentos/metodo-mais-utilizado — Query 5: método de
 * pagamento mais utilizado
 * GET /api/negocio/fase1/clientes/sem-compras — Query 6: clientes registados
 * sem compras
 *
 * FASE 2 QUERIES:
 * GET /api/negocio/pagamentos/troco/media — Query 1: média do troco (Dinheiro)
 * GET /api/negocio/restaurantes/{id}/melhor-entregador — Query 3: entregador
 * com mais entregas
 * GET /api/negocio/menus/{id}/restaurante-popular — Query 4: restaurante mais
 * popular da franquia
 * GET /api/negocio/restaurantes/{id}/produto-mais-pedido — Query 2: item mais
 * pedido
 * GET /api/negocio/clientes/mais-pedidos-intervalo — Query 5: cliente com mais
 * pedidos
 */
@RestController
@RequestMapping("/api/negocio")
public class NegocioController {

    private final PagamentoService pagamentoService;
    private final EntregaService entregaService;
    private final MenuService menuService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;
    private final UserService userService;
    private final RestauranteService restauranteService;

    public NegocioController(PagamentoService pagamentoService,
            EntregaService entregaService,
            MenuService menuService,
            ProdutoService produtoService,
            PedidoService pedidoService,
            UserService userService,
            RestauranteService restauranteService) {
        this.pagamentoService = pagamentoService;
        this.entregaService = entregaService;
        this.menuService = menuService;
        this.produtoService = produtoService;
        this.pedidoService = pedidoService;
        this.userService = userService;
        this.restauranteService = restauranteService;
    }

    // ============================================================================
    // FASE 1 QUERIES — Queries de Negócio Básicas
    // ============================================================================

    /**
     * FASE 1 — Query 1: Restaurantes com maior volume de vendas (€)
     * Calcula o montante total (soma de preços) para cada restaurante.
     *
     * @return 200 com lista de restaurantes e seu volume de vendas, ou 204 se vazio
     */
    @GetMapping("/fase1/restaurantes/volume-vendas")
    public ResponseEntity<?> restaurantesComVolumeSvendas() {
        var resultados = restauranteService.restaurantesComVolumeSvendas();
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resposta = resultados.stream().map(obj -> {
            var dados = (Object[]) obj;
            var restaurante = (pt.ul.fc.css.tascaeats.entities.Restaurante) dados[0];
            var volumeVendas = dados[1];
            var map = new java.util.LinkedHashMap<>();
            map.put("restaurante", RestauranteResponse.from(restaurante));
            map.put("volumeVendas", volumeVendas);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    /**
     * FASE 1 — Query 2: Restaurante com mais pedidos + morada
     * Retorna o restaurante com maior número de pedidos completados e sua morada.
     *
     * @return 200 com restaurante, número de pedidos e morada, ou 204 se vazio
     */
    @GetMapping("/fase1/restaurantes/mais-pedidos")
    public ResponseEntity<?> restauranteComMaisPedidos() {
        var resultados = restauranteService.restaurantesComMaisPedidos();
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Pega apenas o primeiro (com maior número de pedidos)
        var primeiroResultado = resultados.get(0);
        var dados = (Object[]) primeiroResultado;
        var restaurante = (pt.ul.fc.css.tascaeats.entities.Restaurante) dados[0];
        var quantidadePedidos = dados[1];

        var resposta = new java.util.LinkedHashMap<>();
        resposta.put("restaurante", RestauranteResponse.from(restaurante));
        resposta.put("quantidadePedidos", quantidadePedidos);
        resposta.put("morada", restaurante.getMorada() != null ? restaurante.getMorada().toString() : "N/A");

        return ResponseEntity.ok(resposta);
    }

    /**
     * FASE 1 — Query 3: Média de pedidos por cliente por mês
     * Agrupa pedidos por cliente, ano e mês, calculando a média.
     *
     * @return 200 com lista de períodos e contagens, ou 204 se vazio
     */
    @GetMapping("/fase1/pedidos/media-por-cliente-mes")
    public ResponseEntity<?> mediaPedidosPorClientePorMes() {
        var resultados = pedidoService.mediaPedidosPorClientePorMes();
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resposta = resultados.stream().map(obj -> {
            var dados = (Object[]) obj;
            var cliente = (pt.ul.fc.css.tascaeats.entities.Cliente) dados[0];
            var ano = dados[1];
            var mes = dados[2];
            var quantidadePedidos = dados[3];
            var map = new java.util.LinkedHashMap<>();
            map.put("cliente", UserResponse.from(cliente));
            map.put("periodo", String.format("%d-%02d", ano, mes));
            map.put("quantidadePedidos", quantidadePedidos);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    /**
     * FASE 1 — Query 4: Produtos mais vendidos da plataforma
     * Calcula a quantidade total de cada produto vendido (em pedidos DELIVERED).
     *
     * @return 200 com lista de produtos e quantidades, ou 204 se vazio
     */
    @GetMapping("/fase1/produtos/mais-vendidos")
    public ResponseEntity<?> produtosMaisVendidos() {
        var resultados = produtoService.produtosMaisVendidos();
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resposta = resultados.stream().map(obj -> {
            var dados = (Object[]) obj;
            var produto = (pt.ul.fc.css.tascaeats.entities.Produto) dados[0];
            var quantidadeTotal = dados[1];
            var map = new java.util.LinkedHashMap<>();
            map.put("produto", ProdutoResponse.from(produto));
            map.put("quantidadeTotal", quantidadeTotal);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    /**
     * FASE 1 — Query 5: Método de pagamento mais utilizado
     * Conta o número de vezes que cada método de pagamento foi usado.
     *
     * @return 200 com lista de métodos e contagens, ou 204 se vazio
     */
    @GetMapping("/fase1/pagamentos/metodo-mais-utilizado")
    public ResponseEntity<?> metodosPagamentoMaisUtilizados() {
        var resultados = pagamentoService.metodosPagamentoMaisUtilizados();
        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resposta = resultados.stream().map(obj -> {
            var dados = (Object[]) obj;
            var metodo = dados[0].toString();
            var quantidade = dados[1];
            var map = new java.util.LinkedHashMap<>();
            map.put("metodo", metodo);
            map.put("quantidade", quantidade);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    /**
     * FASE 1 — Query 6: Clientes registados sem compras
     * Lista todos os clientes que não realizaram qualquer pedido.
     *
     * @return 200 com lista de clientes, ou 204 se vazio
     */
    @GetMapping("/fase1/clientes/sem-compras")
    public ResponseEntity<List<?>> clientesSemCompras() {
        var clientes = userService.buscarClientesSemCompras();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resposta = clientes.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    // ============================================================================
    // FASE 2 QUERIES — Queries de Negócio Avançadas
    // ============================================================================

    /**
     * Query 1 — Qual é a média do troco nos pagamentos a dinheiro concluídos?
     *
     * @return 200 com o valor médio do troco, ou 204 se não houver pagamentos a
     *         dinheiro
     */
    @GetMapping("/pagamentos/troco/media")
    public ResponseEntity<Double> mediaTroco() {
        Double media = pagamentoService.calcularMediaTroco();
        if (media == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(media);
    }

    /**
     * Query 3 — Qual o entregador com mais entregas concluídas para um restaurante
     * específico?
     *
     * @param restauranteId ID do restaurante
     * @return 200 com o entregador, ou 204 se não houver entregas concluídas para
     *         esse restaurante
     */
    @GetMapping("/restaurantes/{restauranteId}/melhor-entregador")
    public ResponseEntity<UserResponse> melhorEntregadorPorRestaurante(
            @PathVariable Long restauranteId) {
        return entregaService.entregadorComMaisEntregasParaRestaurante(restauranteId)
                .map(e -> ResponseEntity.ok(UserResponse.from(e)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Query 4 — Qual o restaurante mais popular de uma franquia (menu partilhado)?
     * Popularidade medida pelo número de avaliações recebidas.
     *
     * @param menuId ID do menu partilhado (franquia)
     * @return 200 com o restaurante mais avaliado, ou 204 se não houver avaliações
     */
    @GetMapping("/menus/{menuId}/restaurante-popular")
    public ResponseEntity<RestauranteResponse> restaurantePopularDaFranquia(
            @PathVariable Long menuId) {
        return menuService.restauranteMaisPopularDoMenu(menuId)
                .map(r -> ResponseEntity.ok(RestauranteResponse.from(r)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Query 2 — Qual é o item mais pedido de um restaurante?
     * Baseado na soma de quantidades de cada produto pedido.
     *
     * @param restauranteId ID do restaurante
     * @return 200 com um JSON contendo produto e totalVezesPedido, ou 204 se sem
     *         pedidos
     */
    @GetMapping("/restaurantes/{restauranteId}/produto-mais-pedido")
    public ResponseEntity<?> produtoMaisPedidoDoRestaurante(@PathVariable Long restauranteId) {
        var resultado = produtoService.produtoMaisPedidoDoRestaurante(restauranteId);
        if (resultado.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Object[] dados = resultado.get();
        var produto = (pt.ul.fc.css.tascaeats.entities.Produto) dados[0];
        var totalVezesPedido = dados[1];

        var resposta = new java.util.LinkedHashMap<>();
        resposta.put("produto", ProdutoResponse.from(produto));
        resposta.put("totalVezesPedido", totalVezesPedido);

        return ResponseEntity.ok(resposta);
    }

    /**
     * Query 5 — Qual é o cliente que mais pedidos fez num intervalo de tempo?
     *
     * @param dataInicio Data/hora inicial do intervalo (ISO format:
     *                   yyyy-MM-dd'T'HH:mm:ss)
     * @param dataFim    Data/hora final do intervalo (ISO format:
     *                   yyyy-MM-dd'T'HH:mm:ss)
     * @return 200 com um JSON contendo cliente e totalPedidos, ou 204 se sem
     *         pedidos
     */
    @GetMapping("/clientes/mais-pedidos-intervalo")
    public ResponseEntity<?> clienteComMaisPedidosNoIntervalo(
            @RequestParam LocalDateTime dataInicio,
            @RequestParam LocalDateTime dataFim) {
        var resultado = pedidoService.clienteComMaisPedidosNoIntervalo(dataInicio, dataFim);
        if (resultado.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Object[] dados = resultado.get();
        var cliente = (pt.ul.fc.css.tascaeats.entities.Cliente) dados[0];
        var totalPedidos = dados[1];

        var resposta = new java.util.LinkedHashMap<>();
        resposta.put("cliente", UserResponse.from(cliente));
        resposta.put("totalPedidos", totalPedidos);

        return ResponseEntity.ok(resposta);
    }
}
