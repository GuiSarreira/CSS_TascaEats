package pt.ul.fc.css.tascaeats.services;

import pt.ul.fc.css.tascaeats.repositories.*;
import pt.ul.fc.css.tascaeats.entities.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     * @param produtoRepository Repositório para persistência de produtos.
     * @param restauranteRepository Repositório para consulta de restaurantes.
     */
    public ProdutoService(ProdutoRepository produtoRepository, RestauranteRepository restauranteRepository) {
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
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

        if (produtoRepository.existsByNomeIgnoreCaseAndRestauranteIdAndEliminadoFalse(produto.getNome(), restauranteId)) {
            throw new RuntimeException("Já existe um produto com o nome '" + produto.getNome() + "' neste restaurante.");
        }

        produto.setRestaurante(restaurante);
        return produtoRepository.save(produto);
    }

    /**
     * Retorna o menu ativo do restaurante (apenas produtos não eliminados).
     * @param restauranteId Identificador único do restaurante.
     * @return Lista de produtos ativos associados ao restaurante.
     */
    public List<Produto> listarMenuDoRestaurante(Long restauranteId) {
        return produtoRepository.findByRestauranteIdAndEliminadoFalse(restauranteId);
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
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
            .stream()
            .filter(p -> p.getRestaurante().getId().equals(restauranteId) && !p.isEliminado())
            .toList();
    }

    /**
     * Lista produtos de um restaurante que cabem no orçamento definido pelo cliente.
     * @param restauranteId Identificador único do restaurante.
     * @param precoMaximo Valor de corte para o preço do produto.
     * @return Lista de produtos ativos do restaurante com preço inferior ou igual ao máximo.
     */
    public List<Produto> listarPorPreco(Long restauranteId, Double precoMaximo) {
        return produtoRepository.findByPrecoLessThanEqualAndEliminadoFalse(precoMaximo)
            .stream()
            .filter(p -> p.getRestaurante().getId().equals(restauranteId))
            .toList();
    }
}