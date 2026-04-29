package pt.ul.fc.css.tascaeats.services;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Produto;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.repositories.MenuRepository;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.repositories.specs.MenuSpecifications;

import java.util.List;

/**
 * Serviço responsável pela gestão de menus partilhados e filtros avançados.
 * Implementa os requisitos da Pessoa 2 da Fase 2[cite: 44, 68].
 */
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;

    public MenuService(MenuRepository menuRepository,
            RestauranteRepository restauranteRepository,
            ProdutoRepository produtoRepository) {
        this.menuRepository = menuRepository;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Menu criarMenu(String nome, String descricao, List<Produto> produtos, List<Restaurante> restaurantes) {
        Menu menu = new Menu(nome, descricao, produtos, restaurantes);
        menu = menuRepository.save(menu);
        if (restaurantes != null) {
            for (Restaurante r : restaurantes) {
                r.setMenuPartilhado(menu);
                restauranteRepository.save(r);
            }
        }
        return menu;
    }

    @Transactional
    public Menu atualizarMenu(Long menuId, String nome, String descricao, List<Produto> produtos,
            List<Restaurante> restaurantes) {
        Menu menu = buscarPorId(menuId);
        menu.setNome(nome);
        menu.setDescricao(descricao);
        menu.setProdutos(produtos);

        for (Restaurante r : menu.getRestaurantes()) {
            if (restaurantes == null || !restaurantes.contains(r)) {
                r.setMenuPartilhado(null);
                restauranteRepository.save(r);
            }
        }

        if (restaurantes != null) {
            for (Restaurante r : restaurantes) {
                if (!menu.getRestaurantes().contains(r)) {
                    r.setMenuPartilhado(menu);
                    restauranteRepository.save(r);
                }
            }
            menu.setRestaurantes(restaurantes);
        } else {
            menu.getRestaurantes().clear();
        }

        return menuRepository.save(menu);
    }

    @Transactional
    public void removerMenu(Long menuId) {
        Menu menu = buscarPorId(menuId);
        for (Restaurante r : menu.getRestaurantes()) {
            r.setMenuPartilhado(null);
            restauranteRepository.save(r);
        }
        menu.getRestaurantes().clear();
        menu.getProdutos().clear();
        menuRepository.delete(menu);
    }

    /**
     * Estabelece a relação N:N entre um menu e um restaurante[cite: 25].
     */
    @Transactional
    public void associarMenuRestaurante(Long menuId, Long restauranteId) {
        Menu menu = buscarPorId(menuId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado: " + restauranteId));

        if (!menu.getRestaurantes().contains(restaurante)) {
            menu.getRestaurantes().add(restaurante);
            restaurante.setMenuPartilhado(menu);
            menuRepository.save(menu);
            restauranteRepository.save(restaurante);
        }
    }

    @Transactional
    public void removerMenuRestaurante(Long menuId, Long restauranteId) {
        Menu menu = buscarPorId(menuId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        menu.getRestaurantes().remove(restaurante);
        restaurante.setMenuPartilhado(null);
        menuRepository.save(menu);
        restauranteRepository.save(restaurante);
    }

    /**
     * Adiciona um produto ao menu. Uma alteração aqui reflete-se em todos
     * os restaurantes que partilham este menu.
     */
    @Transactional
    public void adicionarProdutoMenu(Long menuId, Long produtoId) {
        Menu menu = buscarPorId(menuId);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!menu.getProdutos().contains(produto)) {
            menu.getProdutos().add(produto);
            produto.getMenus().add(menu);
            menuRepository.save(menu);
        }
    }

    @Transactional
    public void removerProdutoMenu(Long menuId, Long produtoId) {
        Menu menu = buscarPorId(menuId);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        menu.getProdutos().remove(produto);
        menuRepository.save(menu);
    }

    /**
     * Aplica os filtros dinâmicos definidos no enunciado[cite: 90, 91, 92, 93].
     */
    public List<Menu> listarMenusComFiltros(String nome, Integer minProd, Integer maxProd, Double minPreco,
            Double maxPreco) {
        Specification<Menu> spec = Specification.where(MenuSpecifications.comNome(nome))
                .and(MenuSpecifications.quantidadeProdutosEntre(minProd, maxProd))
                .and(MenuSpecifications.precoMedioEntre(minPreco, maxPreco));

        return menuRepository.findAll(spec);
    }

    public Menu buscarPorId(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu não encontrado: id=" + id));
    }
}