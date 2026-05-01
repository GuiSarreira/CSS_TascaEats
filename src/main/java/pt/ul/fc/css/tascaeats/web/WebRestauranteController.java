package pt.ul.fc.css.tascaeats.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;
import pt.ul.fc.css.tascaeats.services.EntregaService;
import pt.ul.fc.css.tascaeats.services.ProdutoService;
import pt.ul.fc.css.tascaeats.services.RestauranteService;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Controller MVC (Thymeleaf) para visualização de Restaurantes.
 *
 * <p>
 * Expõe páginas web para listar restaurantes com filtros avançados
 * e visualizar o detalhe de cada restaurante (menus e produtos).
 * </p>
 */
@Controller
@RequestMapping("/restaurantes")
public class WebRestauranteController {

    private final RestauranteService restauranteService;
    private final ProdutoService produtoService;
    private final AvaliacaoService avaliacaoService;
    private final EntregaService entregaService;

    public WebRestauranteController(RestauranteService restauranteService,
            ProdutoService produtoService,
            AvaliacaoService avaliacaoService,
            EntregaService entregaService) {
        this.restauranteService = restauranteService;
        this.produtoService = produtoService;
        this.avaliacaoService = avaliacaoService;
        this.entregaService = entregaService;
    }

    // ─── Listagem com Filtros ────────────────────────────────────────────────

    @GetMapping
    public String listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoCozinha,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horario,
            @RequestParam(required = false) Double minPreco,
            @RequestParam(required = false) Double maxPreco,
            @RequestParam(required = false) Integer minAvaliacoes,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Integer minPedidos,
            Model model) {

        List<Restaurante> restaurantes = restauranteService.listarRestaurantesComFiltros(
                nome, tipoCozinha, horario, minPreco, maxPreco, minAvaliacoes, cidade, minPedidos);

        model.addAttribute("restaurantes", restaurantes);
        model.addAttribute("filtroNome", nome);
        model.addAttribute("filtroTipoCozinha", tipoCozinha);
        model.addAttribute("filtroHorario", horario);
        model.addAttribute("filtroMinPreco", minPreco);
        model.addAttribute("filtroMaxPreco", maxPreco);
        model.addAttribute("filtroMinAvaliacoes", minAvaliacoes);
        model.addAttribute("filtroCidade", cidade);
        model.addAttribute("filtroMinPedidos", minPedidos);

        return "restaurantes/index";
    }

    // ─── Detalhe ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(id);

        List<?> produtos;
        try {
            produtos = produtoService.listarMenuDoRestaurante(id);
        } catch (RuntimeException e) {
            produtos = Collections.emptyList();
        }

        Double mediaAvaliacoes = avaliacaoService.mediaNotasRestaurante(id);

        model.addAttribute("restaurante", restaurante);
        model.addAttribute("produtos", produtos);
        model.addAttribute("mediaAvaliacoes", mediaAvaliacoes != null ? mediaAvaliacoes : 0.0);
        model.addAttribute("avaliacoes", restaurante.getAvaliacoes());

        return "restaurantes/detalhe";
    }

    // ─── Query 3: Melhor Entregador ───────────────────────────────────────────

    /**
     * Formulário para consultar o entregador com mais entregas de um restaurante.
     * Query de negócio: "Qual o entregador com mais entregas para um restaurante?"
     */
    @GetMapping("/melhor-entregador")
    public String melhorEntregadorForm(Model model) {
        model.addAttribute("titulo", "Melhor Entregador");
        model.addAttribute("descricao", "Qual o entregador com mais entregas concluídas para um restaurante?");
        return "restaurantes/melhor-entregador";
    }

    /**
     * Resultado — entregador com mais entregas para um restaurante.
     */
    @PostMapping("/melhor-entregador")
    public String melhorEntregadorResultado(
            @RequestParam Long restauranteId,
            Model model) {
        var resultado = entregaService.entregadorComMaisEntregasParaRestaurante(restauranteId);

        if (resultado.isPresent()) {
            model.addAttribute("entregador", resultado.get());
            model.addAttribute("encontrado", true);
        } else {
            model.addAttribute("encontrado", false);
            model.addAttribute("mensagem", "Sem entregas para este restaurante");
        }

        model.addAttribute("restauranteId", restauranteId);
        model.addAttribute("titulo", "Melhor Entregador");
        return "restaurantes/melhor-entregador-resultado";
    }

    // ─── Queries de Negócio (Fase 1) ─────────────────────────────────────────

    /**
     * FASE 1 — Query 1: Restaurantes com maior volume de vendas (€)
     * Página que lista todos os restaurantes com seu volume total de vendas.
     */
    @GetMapping("/volume-vendas")
    public String volumeVendas(Model model) {
        var resultados = restauranteService.restaurantesComVolumeSvendas();
        model.addAttribute("resultados", resultados);
        model.addAttribute("titulo", "Volume de Vendas por Restaurante");
        model.addAttribute("descricao", "Restaurantes ordenados por total de vendas (€)");
        return "restaurantes/volume-vendas";
    }

    /**
     * FASE 1 — Query 2: Restaurante com mais pedidos + morada
     * Página que mostra o restaurante com maior número de pedidos completados.
     */
    @GetMapping("/mais-pedidos")
    public String maisPedidos(Model model) {
        var resultados = restauranteService.restaurantesComMaisPedidos();
        if (!resultados.isEmpty()) {
            var topResultado = resultados.get(0);
            var dados = (Object[]) topResultado;
            model.addAttribute("restaurante", dados[0]);
            model.addAttribute("quantidadePedidos", dados[1]);
            model.addAttribute("encontrado", true);
        } else {
            model.addAttribute("encontrado", false);
        }
        model.addAttribute("titulo", "Restaurante com Mais Pedidos");
        model.addAttribute("descricao", "Qual é o restaurante com maior número de pedidos completados?");
        return "restaurantes/mais-pedidos";
    }
}
