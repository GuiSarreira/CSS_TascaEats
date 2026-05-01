package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.ProdutoPedido;
import java.util.List;

/**
 * Repositório para gestão da persistência de objetos do tipo {@link ProdutoPedido}.
 *
 * Fornece queries para análise de itens de pedidos, incluindo
 * consultas de negócio sobre produtos mais vendidos.
 */
@Repository
public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Long> {

    /**
     * Encontra o produto mais pedido (mais vezes encomendado) para um restaurante específico.
     *
     * Query de negócio: "Qual é o item mais pedido de um restaurante?"
     *
     * Junta ProdutoPedido com Produto, Menu e Restaurante para filtrar por restaurante,
     * agrupa por produto, e calcula o total de vezes que cada produto foi pedido.
     * Retorna ordenado por popularidade descendente (produto mais vendido primeiro).
     *
     * O restaurante é identificado via Produto → Menu → Restaurante.
     *
     * @param restauranteId o identificador único do restaurante
     * @return lista de arrays {@code [Produto, totalVezesPedido]} ordenada por popularidade DESC
     */
    @Query("SELECT pp.produto, SUM(pp.quantity) AS totalVezesPedido " +
           "FROM ProdutoPedido pp " +
           "JOIN pp.produto p " +
           "JOIN p.menus m " +
           "JOIN m.restaurantes r " +
           "WHERE r.id = :restauranteId " +
           "GROUP BY p.id, p.nome " +
           "ORDER BY totalVezesPedido DESC")
    List<Object[]> findProdutoMaisPedidoDoRestaurante(@Param("restauranteId") Long restauranteId);
}
