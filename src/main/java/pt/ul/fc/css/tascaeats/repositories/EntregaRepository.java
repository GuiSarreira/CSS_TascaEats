package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Entrega;
import pt.ul.fc.css.tascaeats.entities.EntregaStatus;
import pt.ul.fc.css.tascaeats.entities.Entregador;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Entrega.
 * Fornece métodos para consulta de entregas por estado, entregador,
 * e validação de regras de negócio como "entregador não pode ter duas entregas ativas".
 */
@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    /**
     * Procura uma entrega pelo ID do pedido associado.
     * Como a relação é 1:1, no máximo uma entrega será retornada.
     * @param pedidoId O identificador único do pedido.
     * @return Um Optional contendo a entrega correspondente, ou vazio se não existir.
     */
    Optional<Entrega> findByPedidoId(Long pedidoId);

    /**
     * Lista todas as entregas realizadas por um determinado entregador.
     * @param entregador O entregador cujas entregas se pretendem listar.
     * @return Lista de entregas associadas ao entregador.
     */
    List<Entrega> findByEntregador(Entregador entregador);

    /**
     * Lista entregas com um determinado estado.
     * Estados possíveis: {@link EntregaStatus#ATRIBUIDA}, {@link EntregaStatus#A_CAMINHO},
     * {@link EntregaStatus#CONCLUIDA}, {@link EntregaStatus#CANCELADA}.
     * @param status O estado das entregas a filtrar.
     * @return Lista de entregas no estado especificado.
     */
    List<Entrega> findByStatus(EntregaStatus status);

    /**
     * Lista entregas de um entregador com um determinado estado.
     * @param entregador O entregador a filtrar.
     * @param status     O estado das entregas a filtrar.
     * @return Lista de entregas que satisfazem ambos os critérios.
     */
    List<Entrega> findByEntregadorAndStatus(Entregador entregador, EntregaStatus status);

    /**
     * Lista entregas de um entregador cujo estado esteja numa lista de valores.
     * Útil para verificar se um entregador tem entregas ativas (ATRIBUIDA ou A_CAMINHO).
     * @param entregador O entregador a filtrar.
     * @param statuses   Lista de estados a considerar.
     * @return Lista de entregas que satisfazem os critérios.
     */
    @Query("SELECT e FROM Entrega e WHERE e.entregador = :entregador AND e.status IN :statuses")
    List<Entrega> findByEntregadorAndStatusIn(@Param("entregador") Entregador entregador,
                                               @Param("statuses") List<EntregaStatus> statuses);

    /**
     * Verifica se um entregador tem entregas ativas (ATRIBUIDA ou A_CAMINHO).
     * Regra de negócio: um entregador não pode ter duas entregas ativas ao mesmo tempo.
     * @param entregadorId O identificador do entregador.
     * @return Lista de entregas ativas do entregador (deve ter no máximo 1 para ser válido).
     */
    @Query("SELECT e FROM Entrega e WHERE e.entregador.id = :entregadorId " +
           "AND e.status IN (pt.ul.fc.css.tascaeats.entities.EntregaStatus.ATRIBUIDA, " +
           "pt.ul.fc.css.tascaeats.entities.EntregaStatus.A_CAMINHO)")
    List<Entrega> findEntregasAtivasByEntregadorId(@Param("entregadorId") Long entregadorId);

    /**
     * Ranking de entregadores pelo número de entregas concluídas.
     * Query de negócio para responder a: "Qual o entregador com mais entregas?"
     * @return Lista de arrays contendo [Entregador, totalEntregas] ordenada por total decrescente.
     */
    @Query("SELECT e.entregador, COUNT(e) AS totalEntregas FROM Entrega e " +
           "WHERE e.status = pt.ul.fc.css.tascaeats.entities.EntregaStatus.CONCLUIDA " +
           "GROUP BY e.entregador ORDER BY totalEntregas DESC")
    List<Object[]> findEntregadoresPorNumeroEntregas();

    /**
     * Conta quantas entregas concluídas um entregador realizou.
     * @param entregadorId O identificador do entregador.
     * @return Número total de entregas concluídas pelo entregador.
     */
    @Query("SELECT COUNT(e) FROM Entrega e WHERE e.entregador.id = :entregadorId " +
           "AND e.status = pt.ul.fc.css.tascaeats.entities.EntregaStatus.CONCLUIDA")
    Long countEntregasConcluidasByEntregadorId(@Param("entregadorId") Long entregadorId);

    /**
     * Lista entregas concluídas dentro de um período de tempo.
     * @param inicio Data/hora de início do período.
     * @param fim    Data/hora de fim do período.
     * @return Lista de entregas concluídas no período especificado.
     */
    @Query("SELECT e FROM Entrega e WHERE e.horaEntrega BETWEEN :inicio AND :fim")
    List<Entrega> findEntregasByPeriodo(@Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);

    /**
     * Verifica se já existe uma entrega associada a um determinado pedido.
     * @param pedidoId O identificador do pedido.
     * @return true se já existir uma entrega para o pedido, false caso contrário.
     */
    boolean existsByPedidoId(Long pedidoId);
}