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

       List<Pedido> findAllByOrderByDataHoraDesc();

       /**
        * Lista todos os pedidos de um cliente com um determinado estado.
        *
        * @param clienteId o identificador do cliente
        * @param status    o estado a filtrar
        * @return lista de pedidos do cliente naquele estado
        */
       List<Pedido> findByClienteIdAndStatus(Long clienteId, PedidoStatus status);

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

       /**
        * Lista pedidos de um cliente com filtros opcionais de estado e período.
        * Utilizado para filtros avançados de utilizador (nº pedidos em intervalo de tempo).
        *
        * @param clienteId o identificador do cliente
        * @param status    o estado a filtrar (pode ser null para sem filtro)
        * @param dataMin   data/hora mínima (pode ser null para sem filtro)
        * @param dataMax   data/hora máxima (pode ser null para sem filtro)
        * @return lista de pedidos que satisfazem os critérios, ordenada por data decrescente
        */
       @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId " +
              "AND (:status IS NULL OR p.status = :status) " +
              "AND (:dataMin IS NULL OR p.dataHora >= :dataMin) " +
              "AND (:dataMax IS NULL OR p.dataHora <= :dataMax) " +
              "ORDER BY p.dataHora DESC")
       List<Pedido> findPedidosComFiltros(@Param("clienteId") Long clienteId,
                                          @Param("status") PedidoStatus status,
                                          @Param("dataMin") java.time.LocalDateTime dataMin,
                                          @Param("dataMax") java.time.LocalDateTime dataMax);

       /**
        * Lista pedidos no estado {@code READY} que ainda não têm entrega atribuída.
        * Utilizado para atribuição automática de entregadores.
        *
        * @return lista de pedidos prontos para entrega mas sem entregador
        */
       @Query("SELECT p FROM Pedido p WHERE p.status = pt.ul.fc.css.tascaeats.entities.PedidoStatus.READY " +
              "AND p.entrega IS NULL ORDER BY p.dataHora ASC")
       List<Pedido> findPedidosReadyWithoutEntrega();


}
