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
     * Procura todos os produtos que não foram eliminados logicamente.
     */
    List<Produto> findByEliminadoFalse();
}