package pt.ul.fc.css.tascaeats.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;
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

    public WebRestauranteController(RestauranteService restauranteService,
            ProdutoService produtoService,
            AvaliacaoService avaliacaoService) {
        this.restauranteService = restauranteService;
        this.produtoService = produtoService;
        this.avaliacaoService = avaliacaoService;
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
}
