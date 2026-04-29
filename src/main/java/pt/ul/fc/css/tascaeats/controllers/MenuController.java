package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.MenuRequest;
import pt.ul.fc.css.tascaeats.dto.MenuResponse;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Produto;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.services.MenuService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller REST para gestão de Menus.
 * Expõe endpoints para CRUD de menus e gestão das relações N:N
 * com produtos e restaurantes.
 */
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    public MenuController(MenuService menuService,
                          ProdutoRepository produtoRepository,
                          RestauranteRepository restauranteRepository) {
        this.menuService = menuService;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    @PostMapping
    public ResponseEntity<MenuResponse> criar(@RequestBody MenuRequest request) {
        List<Produto> produtos = resolverProdutos(request.produtoIds());
        List<Restaurante> restaurantes = resolverRestaurantes(request.restauranteIds());

        Menu menu = menuService.criarMenu(request.nome(), request.descricao(), produtos, restaurantes);
        return ResponseEntity.status(HttpStatus.CREATED).body(MenuResponse.from(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuResponse> atualizar(@PathVariable Long id, @RequestBody MenuRequest request) {
        List<Produto> produtos = resolverProdutos(request.produtoIds());
        List<Restaurante> restaurantes = resolverRestaurantes(request.restauranteIds());

        Menu menu = menuService.atualizarMenu(id, request.nome(), request.descricao(), produtos, restaurantes);
        return ResponseEntity.ok(MenuResponse.from(menu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        menuService.removerMenu(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> listarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer minProdutos,
            @RequestParam(required = false) Integer maxProdutos,
            @RequestParam(required = false) Double minPreco,
            @RequestParam(required = false) Double maxPreco) {

        List<MenuResponse> menus = menuService.listarMenusComFiltros(nome, minProdutos, maxProdutos, minPreco, maxPreco)
                .stream()
                .map(MenuResponse::from)
                .toList();
        return ResponseEntity.ok(menus);
    }

    @PostMapping("/{menuId}/restaurantes/{restauranteId}")
    public ResponseEntity<Void> associarRestaurante(@PathVariable Long menuId, @PathVariable Long restauranteId) {
        menuService.associarMenuRestaurante(menuId, restauranteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{menuId}/restaurantes/{restauranteId}")
    public ResponseEntity<Void> desassociarRestaurante(@PathVariable Long menuId, @PathVariable Long restauranteId) {
        menuService.removerMenuRestaurante(menuId, restauranteId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{menuId}/produtos/{produtoId}")
    public ResponseEntity<Void> adicionarProduto(@PathVariable Long menuId, @PathVariable Long produtoId) {
        menuService.adicionarProdutoMenu(menuId, produtoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{menuId}/produtos/{produtoId}")
    public ResponseEntity<Void> removerProduto(@PathVariable Long menuId, @PathVariable Long produtoId) {
        menuService.removerProdutoMenu(menuId, produtoId);
        return ResponseEntity.noContent().build();
    }

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
}
