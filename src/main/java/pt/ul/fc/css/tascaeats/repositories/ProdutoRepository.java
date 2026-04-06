package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Produto;
import java.util.List;

/**
 * Repositório para gestão da persistência de objetos do tipo Produto.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Procura produtos cujo nome contenha a sequência de caracteres fornecida.
     * @param nome Parte do nome do produto a pesquisar.
     * @return Uma lista de produtos que satisfazem o critério de pesquisa (case-insensitive).
     */
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    /**
     * Procura todos os produtos associados a um restaurante que não foram marcados como eliminados.
     * @param restauranteId O identificador único do restaurante.
     * @return Lista de produtos ativos (não eliminados) do restaurante.
     */
    List<Produto> findByRestauranteIdAndEliminadoFalse(Long restauranteId);

    /**
     * Procura o menu atual de um restaurante (produtos disponíveis para venda imediata).
     * @param restauranteId O identificador único do restaurante.
     * @return Lista de produtos que estão marcados como disponíveis e não eliminados.
     */
    List<Produto> findByRestauranteIdAndDisponivelTrueAndEliminadoFalse(Long restauranteId);

    /**
     * Procura produtos com base num limite de preço, ignorando produtos eliminados.
     * @param maxPreco O valor máximo do preço do produto.
     * @return Lista de produtos com preço inferior ou igual ao valor especificado.
     */
    List<Produto> findByPrecoLessThanEqualAndEliminadoFalse(Double maxPreco);
}