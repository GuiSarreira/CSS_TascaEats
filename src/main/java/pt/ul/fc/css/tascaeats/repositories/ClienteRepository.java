package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * Devolve o cliente com o maior número de pedidos.
     * Query de negócio para responder a: "Qual é o cliente com mais pedidos?"
     *
     * Deve ser chamado com {@code PageRequest.of(0, 1)} para obter apenas o primeiro resultado:
     * clienteRepository.findClienteComMaisPedidos(PageRequest.of(0, 1))
     *                  .stream().findFirst()
     *                  .map(obj -> (Cliente) obj[0]);
     *
     * @param pageable use {@code PageRequest.of(0, 1)} para limitar ao top 1
     * @return lista com no máximo um array {@code [Cliente, totalPedidos]}
     */
    @Query("SELECT c, COUNT(p.id) AS totalPedidos FROM Cliente c JOIN c.pedidos p GROUP BY c.id ORDER BY totalPedidos DESC")
    List<Object[]> findClienteComMaisPedidos(Pageable pageable);

    /**
     * Lista todos os clientes com o respetivo total de pedidos realizados.
     * Inclui clientes que nunca fizeram pedidos (total = 0).
     * 
     * @return Lista de arrays contendo [Cliente, totalPedidos] ordenada por total
     *         decrescente.
     */
    @Query("SELECT c, COUNT(p.id) AS totalPedidos FROM Cliente c LEFT JOIN c.pedidos p GROUP BY c.id ORDER BY totalPedidos DESC")
    List<Object[]> findAllClientesComTotalPedidos();
}