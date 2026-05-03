package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Produto;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.services.MenuService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller MVC (Thymeleaf) para gestão de Menus Partilhados.
 *
 * Expõe páginas web para listar, criar, editar e visualizar menus,
 * bem como associar/desassociar restaurantes e produtos.
 */
@Controller
@RequestMapping("/menus")
public class WebMenuController {

    private final MenuService menuService;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;

    public WebMenuController(MenuService menuService,
            RestauranteRepository restauranteRepository,
            ProdutoRepository produtoRepository) {
        this.menuService = menuService;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
    }

    // ─── Listagem ────────────────────────────────────────────────────────────

    @GetMapping
    public String listar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer minProdutos,
            @RequestParam(required = false) Integer maxProdutos,
            @RequestParam(required = false) Double minPreco,
            @RequestParam(required = false) Double maxPreco,
            Model model) {

        List<Menu> menus = menuService.listarMenusComFiltros(nome, minProdutos, maxProdutos, minPreco, maxPreco);
        model.addAttribute("menus", menus);
        model.addAttribute("filtroNome", nome);
        model.addAttribute("filtroMinProdutos", minProdutos);
        model.addAttribute("filtroMaxProdutos", maxProdutos);
        model.addAttribute("filtroMinPreco", minPreco);
        model.addAttribute("filtroMaxPreco", maxPreco);
        return "menus/index";
    }

    // ─── Criar ───────────────────────────────────────────────────────────────

    @GetMapping("/novo")
    public String formNovo(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/menus";
        }
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findByEliminadoFalse());
        return "menus/form";
    }

    @PostMapping
    public String criar(@RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) List<Long> produtoIds,
            @RequestParam(required = false) List<Long> restauranteIds,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/menus";
        }

        List<Produto> produtos = resolverProdutos(produtoIds);
        List<Restaurante> restaurantes = resolverRestaurantes(restauranteIds);

        menuService.criarMenu(nome, descricao, produtos, restaurantes);
        return "redirect:/menus";
    }

    // ─── Editar ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/menus";
        }
        Menu menu = menuService.buscarPorId(id);
        model.addAttribute("menu", menu);
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findByEliminadoFalse());
        return "menus/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) List<Long> produtoIds,
            @RequestParam(required = false) List<Long> restauranteIds,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/menus";
        }

        List<Produto> produtos = resolverProdutos(produtoIds);
        List<Restaurante> restaurantes = resolverRestaurantes(restauranteIds);

        menuService.atualizarMenu(id, nome, descricao, produtos, restaurantes);
        return "redirect:/menus/" + id;
    }

    // ─── Detalhe ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Menu menu = menuService.buscarPorId(id);
        model.addAttribute("menu", menu);
        model.addAttribute("todosRestaurantes", restauranteRepository.findAll());
        return "menus/detalhe";
    }

    // ─── Associação N:N com Restaurante ──────────────────────────────────────

    @PostMapping("/{id}/restaurantes/{restauranteId}")
    public String associarRestaurante(@PathVariable Long id,
            @PathVariable Long restauranteId,
            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/menus";
        }
        menuService.associarMenuRestaurante(id, restauranteId);
        return "redirect:/menus/" + id;
    }

    @PostMapping("/{id}/restaurantes/{restauranteId}/remover")
    public String desassociarRestaurante(@PathVariable Long id,
            @PathVariable Long restauranteId,
            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/menus";
        }
        menuService.removerMenuRestaurante(id, restauranteId);
        return "redirect:/menus/" + id;
    }

    // ─── Remover ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/menus";
        }
        menuService.removerMenu(id);
        return "redirect:/menus";
    }

    // ─── Query 4: Restaurante mais popular da franquia ────────────────────────

    /**
     * Formulário para consultar o restaurante mais popular de uma franquia.
     * Query de negócio: "Qual o restaurante mais popular (com mais avaliações) de
     * uma franquia (menu)?"
     */
    @GetMapping("/restaurante-popular")
    public String restaurantePopularForm(Model model) {
        model.addAttribute("titulo", "Restaurante Popular da Franquia");
        model.addAttribute("descricao", "Qual o restaurante com mais avaliações de um menu partilhado?");
        return "menus/restaurante-popular";
    }

    /**
     * Resultado — restaurante mais popular de uma franquia.
     */
    @PostMapping("/restaurante-popular")
    public String restaurantePopularResultado(
            @RequestParam Long menuId,
            Model model) {
        var resultado = menuService.restauranteMaisPopularDoMenu(menuId);

        if (resultado.isPresent()) {
            model.addAttribute("restaurante", resultado.get());
            model.addAttribute("encontrado", true);
        } else {
            model.addAttribute("encontrado", false);
            model.addAttribute("mensagem", "Sem avaliações para este menu");
        }

        model.addAttribute("menuId", menuId);
        model.addAttribute("titulo", "Restaurante Popular");
        return "menus/restaurante-popular-resultado";
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private List<Produto> resolverProdutos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(produtoRepository.findAllById(ids));
    }

    private List<Restaurante> resolverRestaurantes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(restauranteRepository.findAllById(ids));
    }

    private boolean isAdmin(HttpSession session) {
        Object rawUser = session.getAttribute("user");
        if (rawUser instanceof pt.ul.fc.css.tascaeats.dto.UserResponse user) {
            return "ADMIN".equalsIgnoreCase(user.getRole());
        }
        return false;
    }
}
