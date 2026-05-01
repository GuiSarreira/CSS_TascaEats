package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Cliente.
 * Fornece métodos específicos para clientes, incluindo consultas de negócio
 * como clientes sem compras e análise de pedidos.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Procura um cliente pelo seu email.
     * 
     * @param email O email do cliente a pesquisar.
     * @return Um Optional contendo o cliente correspondente, ou vazio se não
     *         existir.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Lista todos os clientes que estão ativos.
     * 
     * @return Lista de clientes com campo ativo = true.
     */
    List<Cliente> findByAtivoTrue();

    /**
     * Identifica clientes que se registaram mas ainda não realizaram nenhuma
     * compra.
     * 
     * @return Lista de clientes que não possuem nenhum pedido associado.
     */
    @Query("SELECT c FROM Cliente c WHERE c.id NOT IN (SELECT DISTINCT p.cliente.id FROM Pedido p)")
    List<Cliente> findClientesSemCompras();



    /**
     * Lista todos os clientes com o respetivo total de pedidos realizados.
     * Inclui clientes que nunca fizeram pedidos (total = 0).
     * 
     * @return Lista de arrays contendo [Cliente, totalPedidos] ordenada por total
     *         decrescente.
     */
    @Query("SELECT c, COUNT(p.id) AS totalPedidos FROM Cliente c LEFT JOIN c.pedidos p GROUP BY c.id ORDER BY totalPedidos DESC")
    List<Object[]> findAllClientesComTotalPedidos();

    /**
     * Encontra o cliente com mais pedidos num intervalo de tempo.
     * Query de negócio (Fase 2): "Qual é o cliente que mais pedidos fez num intervalo de tempo?"
     *
     * Agrupa por cliente, filtra por data/hora, e retorna ordenado por total de pedidos descendente.
     *
     * @param dataInicio data/hora inicial do intervalo (inclusivo)
     * @param dataFim data/hora final do intervalo (inclusivo)
     * @return lista de arrays {@code [Cliente, totalPedidos]} ordenada por totalPedidos DESC
     */
    @Query("SELECT p.cliente, COUNT(p) AS totalPedidos " +
           "FROM Pedido p " +
           "WHERE p.dataHora >= :dataInicio AND p.dataHora <= :dataFim " +
           "GROUP BY p.cliente.id " +
           "ORDER BY totalPedidos DESC")
    List<Object[]> findClienteComMaisPedidosNoIntervalo(
           @Param("dataInicio") java.time.LocalDateTime dataInicio,
           @Param("dataFim") java.time.LocalDateTime dataFim);
}