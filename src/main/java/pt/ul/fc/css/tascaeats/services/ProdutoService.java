package pt.ul.fc.css.tascaeats.services;

import pt.ul.fc.css.tascaeats.repositories.*;
import pt.ul.fc.css.tascaeats.repositories.specs.ProdutoSpecifications;
import pt.ul.fc.css.tascaeats.entities.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pela gestão da lógica de negócio de Produtos.
 * Permite a gestão do menu dos restaurantes, incluindo criação, 
 * atualização, disponibilidade e remoção lógica (soft-delete).
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;
    private final MenuRepository menuRepository;
    private final ProdutoPedidoRepository produtoPedidoRepository;

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     *
     * @param produtoRepository   repositório de produtos
     * @param restauranteRepository repositório de restaurantes (necessário para validar acesso ao menu)
     * @param menuRepository      repositório de menus (necessário para gestão de menus padrão)
     * @param produtoPedidoRepository repositório de itens de pedidos (necessário para queries de negócio)
     */
    public ProdutoService(ProdutoRepository produtoRepository,
                          RestauranteRepository restauranteRepository,
                          MenuRepository menuRepository,
                          ProdutoPedidoRepository produtoPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
        this.menuRepository = menuRepository;
        this.produtoPedidoRepository = produtoPedidoRepository;
    }

    /**
     * Cria um novo produto e associa-o a um restaurante existente.
     * @param restauranteId Identificador único do restaurante proprietário do produto.
     * @param produto Objeto produto contendo os dados a serem gravados.
     * @return O produto guardado com a associação ao restaurante estabelecida.
     * @throws RuntimeException Caso o restaurante indicado não seja encontrado.
     */
    @Transactional
    public Produto criarProduto(Long restauranteId, Produto produto) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));

        // Verificar duplicados no menu do restaurante
        boolean duplicado = restaurante.getMenu() != null &&
                restaurante.getMenu().getProdutos().stream()
                .anyMatch(p -> !p.isEliminado() && p.getNome().equalsIgnoreCase(produto.getNome()));
        if (duplicado) {
            throw new RuntimeException(
                    "Já existe um produto com o nome '" + produto.getNome() + "' neste restaurante.");
        }

        Produto saved = produtoRepository.save(produto);

        // Adicionar ao menu do restaurante; criar menu padrão se ainda não tiver nenhum
        if (restaurante.getMenu() == null) {
            Menu defaultMenu = new Menu(restaurante.getNome() + " - Menu", "Menu principal",
                    new ArrayList<>(), new ArrayList<>());
            defaultMenu.getProdutos().add(saved);
            saved.getMenus().add(defaultMenu);
            menuRepository.save(defaultMenu);
            restaurante.setMenu(defaultMenu);
            restauranteRepository.save(restaurante);
        } else {
            Menu menu = restaurante.getMenu();
            menu.getProdutos().add(saved);
            saved.getMenus().add(menu);
            menuRepository.save(menu);
        }

        return saved;
    }

    /**
     * Retorna o menu ativo do restaurante (apenas produtos não eliminados).
     * @param restauranteId Identificador único do restaurante.
     * @return Lista de produtos ativos associados ao restaurante.
     */
    public List<Produto> listarMenuDoRestaurante(Long restauranteId) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));
        Menu menu = restaurante.getMenu();
        if (menu == null) return List.of();
        return menu.getProdutos().stream()
                .filter(p -> !p.isEliminado())
                .toList();
    }

    /**
     * Procura um produto específico pelo seu identificador técnico (ID).
     * @param id Identificador único do produto.
     * @return O objeto Produto correspondente ao ID.
     * @throws RuntimeException Caso o produto não seja encontrado na base de dados.
     */
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }

    /**
     * Atualiza dados fundamentais do produto (nome, descrição e preço).
     *
     * @param id      identificador único do produto a ser editado
     * @param nome    novo nome do produto
     * @param descricao nova descrição do produto
     * @param preco   novo preço unitário em euros
     * @return o produto após a persistência das alterações
     * @throws RuntimeException se o produto não for encontrado
     */
    @Transactional
    public Produto atualizarProduto(Long id, String nome, String descricao, Double preco) {
        Produto produto = buscarPorId(id);

        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);

        return produtoRepository.save(produto);
    }

    /**
     * Altera a disponibilidade de um produto (ex: marcar como esgotado).
     * @param id Identificador único do produto.
     * @param disponivel Estado de disponibilidade (true para disponível, false para esgotado).
     */
    @Transactional
    public void alternarDisponibilidade(Long id, boolean disponivel) {
        Produto produto = buscarPorId(id);
        produto.setDisponivel(disponivel);
    }

    /**
     * Soft-Delete: Marca o produto como eliminado em vez de o apagar fisicamente.
     * Garante a manutenção do histórico de pedidos que referenciam este produto.
     * @param id Identificador único do produto a ser removido logicamente.
     */
    @Transactional
    public void removerProduto(Long id) {
        Produto produto = buscarPorId(id);
        
        if (produto.getItensPedido().isEmpty()) {
            produtoRepository.delete(produto);
        } else {
            produto.deleteLogicamente();
            produtoRepository.save(produto);
        }
    }

    /**
     * Procura produtos específicos dentro do menu de um restaurante através do nome.
     * @param restauranteId Identificador único do restaurante para filtrar a busca.
     * @param nome Sequência de caracteres a pesquisar no nome do produto.
     * @return Lista de produtos que pertencem ao restaurante e satisfazem o critério de nome.
     */
    public List<Produto> buscarNoMenu(Long restauranteId, String nome) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));
        Menu menu = restaurante.getMenu();
        if (menu == null) return List.of();
        return menu.getProdutos().stream()
                .filter(p -> !p.isEliminado() && p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }

    /**
     * Lista produtos do menu de um restaurante com preço até ao máximo indicado.
     * Filtra apenas produtos ativos (não eliminados) e disponíveis até ao preço limite.
     *
     * @param restauranteId identificador único do restaurante
     * @param precoMaximo   preço máximo em euros (produtos com preço <= precoMaximo)
     * @return lista de produtos que satisfazem os critérios de preço
     * @throws RuntimeException se o restaurante não for encontrado
     */
    public List<Produto> listarPorPreco(Long restauranteId, Double precoMaximo) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));
        Menu menu = restaurante.getMenu();
        if (menu == null) return List.of();
        return menu.getProdutos().stream()
                .filter(p -> !p.isEliminado() && p.getPreco() <= precoMaximo)
                .toList();
    }

    /**
     * Filtra produtos com múltiplos critérios usando JPA Specifications.
     * Todos os parâmetros são opcionais; apenas os não-null são aplicados.
     *
     * Filtros suportados:
     * - Nome: substring search (case-insensitive)
     * - Preço: intervalo (precoMin/precoMax)
     * - Categoria: correspondência exata
     * - Disponibilidade: booleano (apenas admin/entregador devem usar filtro de disponibilidade)
     * - Popularidade: número mínimo de vezes pedido em intervalo de tempo
     *
     * @param nome Parte do nome a procurar (null para ignorar)
     * @param precoMin Preço mínimo (null para ignorar)
     * @param precoMax Preço máximo (null para ignorar)
     * @param categoria Categoria exata (null para ignorar)
     * @param disponivel Estado de disponibilidade (null para ignorar)
     * @param minPopularidade Número mínimo de vezes pedido (null para ignorar)
     * @param dataInicio Data/hora inicial para popularidade (null para ignorar)
     * @param dataFim Data/hora final para popularidade (null para ignorar)
     * @return Lista de produtos que satisfazem todos os critérios aplicados
     */
    public List<Produto> filtrarProdutos(String nome, Double precoMin, Double precoMax,
                                         String categoria, Boolean disponivel,
                                         Integer minPopularidade, LocalDateTime dataInicio, LocalDateTime dataFim) {
        Specification<Produto> spec = Specification.where(ProdutoSpecifications.naoEliminado());

        if (nome != null && !nome.isBlank()) {
            spec = spec.and(ProdutoSpecifications.comNome(nome));
        }
        if (precoMin != null || precoMax != null) {
            spec = spec.and(ProdutoSpecifications.comPreco(precoMin, precoMax));
        }
        if (categoria != null && !categoria.isBlank()) {
            spec = spec.and(ProdutoSpecifications.comCategoria(categoria));
        }
        if (disponivel != null) {
            spec = spec.and(ProdutoSpecifications.comDisponibilidade(disponivel));
        }
        if (minPopularidade != null && minPopularidade > 0) {
            spec = spec.and(ProdutoSpecifications.comPopularidade(minPopularidade, dataInicio, dataFim));
        }

        return produtoRepository.findAll(spec);
    }

    /**
     * Encontra o produto mais pedido (mais vezes encomendado) de um restaurante.
     *
     * Query de negócio: "Qual é o item mais pedido de um restaurante?"
     *
     * Responde com uma array contendo:
     * - [0]: Produto (a entidade)
     * - [1]: totalVezesPedido (Long — total de vezes que foi pedido, somando quantidades)
     *
     * @param restauranteId Identificador único do restaurante
     * @return Opcional contendo [Produto, totalVezesPedido], vazio se restaurante não tem pedidos
     */
    public java.util.Optional<Object[]> produtoMaisPedidoDoRestaurante(Long restauranteId) {
        List<Object[]> resultados = produtoPedidoRepository.findProdutoMaisPedidoDoRestaurante(restauranteId);
        return resultados.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(resultados.get(0));
    }

    /**
     * FASE 1 — Query 4: Produtos mais vendidos da plataforma.
     * Calcula a quantidade total de cada produto vendido (em pedidos DELIVERED).
     * @return lista de arrays [Produto, quantidadeTotal] ordenada por quantidade DESC
     */
    public List<Object[]> produtosMaisVendidos() {
        return produtoRepository.findProdutosMaisVendidos();
    }
}