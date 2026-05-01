package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Produto;
import java.util.List;

/**
 * Repositório para gestão da persistência de objetos do tipo Produto.
 * Suporta filtragem dinâmica via JPA Specifications (ProdutoSpecifications).
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

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

    /**
     * Query de negócio (Fase 1): Produtos mais vendidos da plataforma.
     * Calcula o total de unidades vendidas para cada produto,
     * ordenado de forma decrescente (mais vendidos primeiro).
     *
     * Apenas inclui pedidos com status DELIVERED (concluídos).
     * Quantidade total: SUM(ProdutoPedido.quantity) para cada produto.
     *
     * @return lista de arrays [Produto, quantidadeTotal] ordenada por quantidade DESC
     */
    @Query("SELECT p, SUM(ip.quantity) AS quantidadeTotal " +
           "FROM Produto p " +
           "JOIN p.itensPedido ip " +
           "JOIN ip.pedido ped " +
           "WHERE ped.status = pt.ul.fc.css.tascaeats.entities.PedidoStatus.DELIVERED " +
           "GROUP BY p.id " +
           "ORDER BY quantidadeTotal DESC")
    List<Object[]> findProdutosMaisVendidos();
}