package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.PedidoStatus;

import java.util.List;

/**
 * Repositório para gestão da persistência de objetos do tipo {@link Pedido}.
 *
 * Para além dos métodos CRUD herdados de {@link JpaRepository}, expõe queries
 * para suportar os casos de uso do sistema (listagem por cliente, por
 * restaurante,
 * por estado) e queries de negócio (volume de vendas, média de pedidos por
 * mês).
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

       /**
        * Lista todos os pedidos de um determinado cliente.
        *
        * @param cliente o cliente cujos pedidos se pretendem listar
        * @return lista de pedidos do cliente, ordenada por data decrescente
        */
       List<Pedido> findByClienteIdOrderByDataHoraDesc(Long clienteId);

       /**
        * Lista todos os pedidos de um cliente com um determinado estado.
        *
        * @param clienteId o identificador do cliente
        * @param status    o estado a filtrar
        * @return lista de pedidos do cliente naquele estado
        */
       List<Pedido> findByClienteIdAndStatus(Long clienteId, PedidoStatus status);

       /**
        * Lista todos os pedidos recebidos por um restaurante.
        *
        * @param restauranteId o identificador único do restaurante
        * @return lista de pedidos do restaurante, por ordem cronológica decrescente
        */
       List<Pedido> findByRestauranteIdOrderByDataHoraDesc(Long restauranteId);

       /**
        * Lista pedidos de um restaurante com um determinado estado.
        *
        * @param restauranteId o identificador do restaurante
        * @param status        o estado a filtrar
        * @return lista de pedidos do restaurante naquele estado
        */
       List<Pedido> findByRestauranteIdAndStatus(Long restauranteId, PedidoStatus status);

       /**
        * Lista todos os pedidos com um determinado estado.
        *
        * @param status o estado a filtrar (ex: {@link PedidoStatus#READY} para
        *               atribuir entregadores)
        * @return lista de pedidos naquele estado
        */
       List<Pedido> findByStatus(PedidoStatus status);

       /**
        * Query de negócio — Média de pedidos por cliente por mês.
        * Responde à query: "Qual é a média de pedidos por cliente por mês?"
        *
        * Agrupa por cliente, ano e mês e calcula a contagem média de pedidos.
        *
        * @return lista de arrays {@code [cliente_id, ano, mes, totalPedidos]}
        */
       @Query("SELECT p.cliente.id, FUNCTION('YEAR', p.dataHora), FUNCTION('MONTH', p.dataHora), COUNT(p) "
                     + "FROM Pedido p " +
                     "GROUP BY p.cliente.id, FUNCTION('YEAR', p.dataHora), FUNCTION('MONTH', p.dataHora)")
       List<Object[]> findMediaPedidosPorClientePorMes();

       /**
        * Verifica se um cliente tem algum pedido num dos estados ativos
        * (não cancelado, não entregue). Útil para validar se um utilizador
        * pode ser removido.
        *
        * @param clienteId o identificador do cliente
        * @return {@code true} se existir pelo menos um pedido ativo
        */
       @Query("SELECT COUNT(p) > 0 FROM Pedido p WHERE p.cliente.id = :clienteId " +
                     "AND p.status NOT IN (pt.ul.fc.css.tascaeats.entities.PedidoStatus.DELIVERED, " +
                     "pt.ul.fc.css.tascaeats.entities.PedidoStatus.CANCELLED)")
       boolean existsPedidoAtivoByClienteId(@Param("clienteId") Long clienteId);
}
