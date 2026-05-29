package pt.ul.fc.css.entrega.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.entrega.entities.Entrega;
import pt.ul.fc.css.entrega.entities.EntregaStatus;
import pt.ul.fc.css.entrega.entities.Entregador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de {@link Entrega} no microserviço.
 *
 * Ao contrário do repositório no monólito, as queries não referenciam
 * entidades como {@code Pedido} ou {@code Restaurante} (que pertencem ao
 * monólito). A relação com o pedido é feita apenas por {@code pedidoId} (Long).
 */
@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

       /**
        * Procura uma entrega pelo ID do pedido (referência lógica ao monólito).
        * Como a relação é 1:1, no máximo uma entrega será retornada.
        *
        * @param pedidoId o identificador do pedido no monólito
        * @return Optional com a entrega, ou vazio se não existir
        */
       Optional<Entrega> findByPedidoId(Long pedidoId);

       /**
        * Lista todas as entregas realizadas por um determinado entregador.
        *
        * @param entregador o entregador cujas entregas se pretendem listar
        * @return lista de entregas associadas ao entregador
        */
       List<Entrega> findByEntregador(Entregador entregador);

       /**
        * Lista entregas com um determinado estado.
        *
        * @param status o estado das entregas a filtrar
        * @return lista de entregas no estado especificado
        */
       List<Entrega> findByStatus(EntregaStatus status);

       /**
        * Lista entregas de um entregador com um determinado estado.
        *
        * @param entregador o entregador a filtrar
        * @param status     o estado das entregas a filtrar
        * @return lista de entregas que satisfazem ambos os critérios
        */
       List<Entrega> findByEntregadorAndStatus(Entregador entregador, EntregaStatus status);

       /**
        * Verifica se um entregador tem entregas ativas (ATRIBUIDA ou A_CAMINHO).
        * Regra de negócio: um entregador não pode ter duas entregas ativas.
        *
        * @param entregadorId o identificador do entregador
        * @return lista de entregas ativas do entregador
        */
       @Query("SELECT e FROM Entrega e WHERE e.entregador.id = :entregadorId " +
                     "AND e.status IN (pt.ul.fc.css.entrega.entities.EntregaStatus.ATRIBUIDA, " +
                     "pt.ul.fc.css.entrega.entities.EntregaStatus.A_CAMINHO)")
       List<Entrega> findEntregasAtivasByEntregadorId(@Param("entregadorId") Long entregadorId);

       /**
        * Verifica se já existe uma entrega associada a um determinado pedido.
        *
        * @param pedidoId o identificador do pedido
        * @return {@code true} se já existir uma entrega para o pedido
        */
       boolean existsByPedidoId(Long pedidoId);

       /**
        * Conta quantas entregas concluídas um entregador realizou.
        *
        * @param entregadorId o identificador do entregador
        * @return número total de entregas concluídas
        */
       @Query("SELECT COUNT(e) FROM Entrega e WHERE e.entregador.id = :entregadorId " +
                     "AND e.status = pt.ul.fc.css.entrega.entities.EntregaStatus.CONCLUIDA")
       Long countEntregasConcluidasByEntregadorId(@Param("entregadorId") Long entregadorId);

       /**
        * Lista entregas concluídas dentro de um período de tempo.
        *
        * @param inicio data/hora de início do período
        * @param fim    data/hora de fim do período
        * @return lista de entregas concluídas no período
        */
       @Query("SELECT e FROM Entrega e WHERE e.horaEntrega BETWEEN :inicio AND :fim")
       List<Entrega> findEntregasByPeriodo(@Param("inicio") LocalDateTime inicio,
                     @Param("fim") LocalDateTime fim);

       /**
        * Lista entregas de um entregador cujo estado esteja numa lista de valores.
        *
        * @param entregador o entregador a filtrar
        * @param statuses   lista de estados a considerar
        * @return lista de entregas que satisfazem os critérios
        */
       @Query("SELECT e FROM Entrega e WHERE e.entregador = :entregador AND e.status IN :statuses")
       List<Entrega> findByEntregadorAndStatusIn(@Param("entregador") Entregador entregador,
                     @Param("statuses") List<EntregaStatus> statuses);

       /**
        * Lista entregas filtradas opcionalmente por entregador.
        *
        * @param entregadorId o identificador do entregador (pode ser null)
        * @return lista de entregas ordenadas por pedidoId descendente
        */
       @Query("SELECT e FROM Entrega e " +
                     "WHERE (:entregadorId IS NULL OR e.entregador.id = :entregadorId) " +
                     "ORDER BY e.pedidoId DESC")
       List<Entrega> findEntregasComFiltros(@Param("entregadorId") Long entregadorId);
}
