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
import java.util.Optional;

/**
 * Serviço responsável pela gestão de menus partilhados.
 *
 * Permite criar, atualizar e remover menus, associá-los a restaurantes e
 * gerir os produtos que os compõem. Suporta filtragem dinâmica por nome,
 * quantidade de produtos e preço médio.
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

    /**
     * Cria um novo menu com a lista de produtos e restaurantes indicados.
     */
    @Transactional
    public Menu criarMenu(String nome, String descricao, List<Produto> produtos, List<Restaurante> restaurantes) {
        Menu menu = new Menu(nome, descricao, produtos, restaurantes);
        return menuRepository.save(menu);
    }

    /**
     * Atualiza os dados de um menu existente.
     *
     * @throws RuntimeException se o menu não for encontrado
     */
    @Transactional
    public Menu atualizarMenu(Long menuId, String nome, String descricao,
            List<Produto> produtos, List<Restaurante> restaurantes) {
        Menu menu = buscarPorId(menuId);
        menu.setNome(nome);
        menu.setDescricao(descricao);
        menu.setProdutos(produtos);

        List<Restaurante> newList = restaurantes != null ? restaurantes : new java.util.ArrayList<>();
        // Desassociar restaurantes que já não fazem parte deste menu
        for (Restaurante r : new java.util.ArrayList<>(menu.getRestaurantes())) {
            if (!newList.contains(r)) {
                r.setMenu(null);
                restauranteRepository.save(r);
            }
        }
        // Associar novos restaurantes
        for (Restaurante r : newList) {
            if (!menu.getRestaurantes().contains(r)) {
                r.setMenu(menu);
                restauranteRepository.save(r);
            }
        }
        menu.setRestaurantes(newList);
        return menuRepository.save(menu);
    }

    /**
     * Remove um menu, desassociando-o de todos os restaurantes e produtos.
     *
     * @throws RuntimeException se o menu não for encontrado
     */
    @Transactional
    public void removerMenu(Long menuId) {
        Menu menu = buscarPorId(menuId);
        for (Restaurante r : new java.util.ArrayList<>(menu.getRestaurantes())) {
            r.setMenu(null);
            restauranteRepository.save(r);
        }
        menu.getRestaurantes().clear();
        menu.getProdutos().clear();
        menuRepository.delete(menu);
    }

    /**
     * Procura um menu pelo seu ID.
     *
     * @throws RuntimeException se o menu não for encontrado
     */
    public Menu buscarPorId(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu não encontrado: id=" + id));
    }

    /**
     * Associa um menu a um restaurante (relação N:1).
     * Se a associação já existir, não efectua alterações.
     *
     * @throws RuntimeException se o menu ou restaurante não forem encontrados
     */
    @Transactional
    public void associarMenuRestaurante(Long menuId, Long restauranteId) {
        Menu menu = buscarPorId(menuId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado: " + restauranteId));

        if (!menu.getRestaurantes().contains(restaurante)) {
            menu.getRestaurantes().add(restaurante);
            restaurante.setMenu(menu);
            restauranteRepository.save(restaurante);
        }
    }

    /**
     * Remove a associação entre um menu e um restaurante.
     *
     * @throws RuntimeException se o menu ou restaurante não forem encontrados
     */
    @Transactional
    public void removerMenuRestaurante(Long menuId, Long restauranteId) {
        Menu menu = buscarPorId(menuId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        menu.getRestaurantes().remove(restaurante);
        restaurante.setMenu(null);
        restauranteRepository.save(restaurante);
    }

    /**
     * Adiciona um produto ao menu, mantendo a consistência bidirecional.
     * Se o produto já estiver no menu, não efectua alterações.
     *
     * @throws RuntimeException se o menu ou produto não forem encontrados
     */
    @Transactional
    public void adicionarProdutoMenu(Long menuId, Long produtoId) {
        Menu menu = buscarPorId(menuId);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

        if (!menu.getProdutos().contains(produto)) {
            menu.getProdutos().add(produto);
            produto.getMenus().add(menu);
            menuRepository.save(menu);
        }
    }

    /**
     * Remove um produto do menu.
     *
     * @throws RuntimeException se o menu ou produto não forem encontrados
     */
    @Transactional
    public void removerProdutoMenu(Long menuId, Long produtoId) {
        Menu menu = buscarPorId(menuId);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

        menu.getProdutos().remove(produto);
        menuRepository.save(menu);
    }

    /**
     * Lista menus aplicando filtros dinâmicos.
     *
     * @param nome     parte do nome a pesquisar (opcional)
     * @param minProd  número mínimo de produtos (opcional)
     * @param maxProd  número máximo de produtos (opcional)
     * @param minPreco preço médio mínimo (opcional)
     * @param maxPreco preço médio máximo (opcional)
     * @return lista de menus que satisfazem todos os critérios
     */
    public List<Menu> listarMenusComFiltros(String nome, Integer minProd, Integer maxProd,
            Double minPreco, Double maxPreco) {
        Specification<Menu> spec = Specification.where(MenuSpecifications.comNome(nome))
                .and(MenuSpecifications.quantidadeProdutosEntre(minProd, maxProd))
                .and(MenuSpecifications.precoMedioEntre(minPreco, maxPreco));

        return menuRepository.findAll(spec);
    }

    /**
     * Query de negócio — devolve o restaurante mais popular de uma franquia (menu partilhado).
     * Popularidade medida pelo número de avaliações recebidas.
     *
     * @param menuId ID do menu partilhado
     * @return Optional com o restaurante mais popular, vazio se não houver avaliações
     */
    public Optional<Restaurante> restauranteMaisPopularDoMenu(Long menuId) {
        List<Object[]> results = menuRepository.findRestauranteMaisPopularDoMenu(menuId);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((Restaurante) results.get(0)[0]);
    }
}
