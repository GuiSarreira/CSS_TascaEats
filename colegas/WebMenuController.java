package pt.ul.fc.css.tascaeats.controllers;

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
 * <p>Expõe páginas web para listar, criar, editar e visualizar menus,
 * bem como associar/desassociar restaurantes e produtos.</p>
 *
 * <p>Endpoints definidos conforme distribuição da Pessoa 2 — Semana 2.</p>
 */
@Controller
@RequestMapping("/menus")
public class WebMenuController {

    private final MenuService menuService;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * Construtor com injeção de dependências.
     *
     * @param menuService           serviço de menus
     * @param restauranteRepository repositório de restaurantes (para formulários)
     * @param produtoRepository     repositório de produtos (para formulários)
     */
    public WebMenuController(MenuService menuService,
                             RestauranteRepository restauranteRepository,
                             ProdutoRepository produtoRepository) {
        this.menuService = menuService;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
    }

    // ─── Listagem ────────────────────────────────────────────────────────────

    /**
     * Lista menus com filtros opcionais.
     *
     * @param nome        filtro por nome (opcional)
     * @param minProdutos número mínimo de produtos (opcional)
     * @param maxProdutos número máximo de produtos (opcional)
     * @param minPreco    preço mínimo médio (opcional)
     * @param maxPreco    preço máximo médio (opcional)
     * @param model       modelo Thymeleaf
     * @return template menus/index
     */
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

    /**
     * Apresenta o formulário para criar um novo menu.
     *
     * @param model modelo Thymeleaf
     * @return template menus/form
     */
    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findByEliminadoFalse());
        return "menus/form";
    }

    /**
     * Processa a submissão do formulário de criação de menu.
     *
     * @param nome            nome do menu
     * @param descricao       descrição do menu
     * @param produtoIds      IDs dos produtos a associar (opcional)
     * @param restauranteIds  IDs dos restaurantes a associar (opcional)
     * @return redirect para a lista de menus
     */
    @PostMapping
    public String criar(@RequestParam String nome,
                        @RequestParam(required = false) String descricao,
                        @RequestParam(required = false) List<Long> produtoIds,
                        @RequestParam(required = false) List<Long> restauranteIds) {

        List<Produto> produtos = resolverProdutos(produtoIds);
        List<Restaurante> restaurantes = resolverRestaurantes(restauranteIds);

        menuService.criarMenu(nome, descricao, produtos, restaurantes);
        return "redirect:/menus";
    }

    // ─── Editar ──────────────────────────────────────────────────────────────

    /**
     * Apresenta o formulário de edição de um menu existente.
     *
     * @param id    ID do menu a editar
     * @param model modelo Thymeleaf
     * @return template menus/form (em modo edição)
     */
    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model) {
        Menu menu = menuService.buscarPorId(id);
        model.addAttribute("menu", menu);
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findByEliminadoFalse());
        return "menus/form";
    }

    /**
     * Processa a submissão do formulário de edição de menu.
     *
     * @param id              ID do menu a atualizar
     * @param nome            novo nome
     * @param descricao       nova descrição
     * @param produtoIds      IDs dos produtos a associar (opcional)
     * @param restauranteIds  IDs dos restaurantes a associar (opcional)
     * @return redirect para o detalhe do menu
     */
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @RequestParam String nome,
                            @RequestParam(required = false) String descricao,
                            @RequestParam(required = false) List<Long> produtoIds,
                            @RequestParam(required = false) List<Long> restauranteIds) {

        List<Produto> produtos = resolverProdutos(produtoIds);
        List<Restaurante> restaurantes = resolverRestaurantes(restauranteIds);

        menuService.atualizarMenu(id, nome, descricao, produtos, restaurantes);
        return "redirect:/menus/" + id;
    }

    // ─── Detalhe ─────────────────────────────────────────────────────────────

    /**
     * Apresenta a página de detalhe de um menu, incluindo os seus produtos e
     * restaurantes associados.
     *
     * @param id    ID do menu
     * @param model modelo Thymeleaf
     * @return template menus/detalhe
     */
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Menu menu = menuService.buscarPorId(id);
        model.addAttribute("menu", menu);
        // Disponibilizar todos os restaurantes para o formulário de associação
        model.addAttribute("todosRestaurantes", restauranteRepository.findAll());
        return "menus/detalhe";
    }

    // ─── Associação N:N com Restaurante ──────────────────────────────────────

    /**
     * Associa um restaurante a um menu existente.
     *
     * @param id              ID do menu
     * @param restauranteId   ID do restaurante a associar
     * @return redirect para o detalhe do menu
     */
    @PostMapping("/{id}/restaurantes/{restauranteId}")
    public String associarRestaurante(@PathVariable Long id,
                                      @PathVariable Long restauranteId) {
        menuService.associarMenuRestaurante(id, restauranteId);
        return "redirect:/menus/" + id;
    }

    /**
     * Desassocia um restaurante de um menu.
     *
     * @param id              ID do menu
     * @param restauranteId   ID do restaurante a desassociar
     * @return redirect para o detalhe do menu
     */
    @PostMapping("/{id}/restaurantes/{restauranteId}/remover")
    public String desassociarRestaurante(@PathVariable Long id,
                                         @PathVariable Long restauranteId) {
        menuService.removerMenuRestaurante(id, restauranteId);
        return "redirect:/menus/" + id;
    }

    // ─── Remover ─────────────────────────────────────────────────────────────

    /**
     * Remove um menu do sistema.
     *
     * @param id ID do menu a remover
     * @return redirect para a lista de menus
     */
    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id) {
        menuService.removerMenu(id);
        return "redirect:/menus";
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolve uma lista de IDs de produto em entidades.
     * Retorna lista vazia se a lista de IDs for null ou vazia.
     */
    private List<Produto> resolverProdutos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(produtoRepository.findAllById(ids));
    }

    /**
     * Resolve uma lista de IDs de restaurante em entidades.
     * Retorna lista vazia se a lista de IDs for null ou vazia.
     */
    private List<Restaurante> resolverRestaurantes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(restauranteRepository.findAllById(ids));
    }
}
