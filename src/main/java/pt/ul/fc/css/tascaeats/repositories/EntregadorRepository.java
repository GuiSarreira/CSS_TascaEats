package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Entregador;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Entregador.
 * Fornece métodos para pesquisa por disponibilidade, zona de atuação,
 * e validação de regras de negócio como "não atribuir entregador indisponível".
 */
@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    /**
     * Procura um entregador pelo seu email.
     * 
     * @param email O email do entregador a pesquisar.
     * @return Um Optional contendo o entregador correspondente, ou vazio se não
     *         existir.
     */
    Optional<Entregador> findByEmail(String email);

    /**
     * Lista todos os entregadores que estão disponíveis para novas entregas.
     * Um entregador disponível tem disponivel = true e não possui entregas ativas.
     * 
     * @return Lista de entregadores com campo disponivel = true.
     */
    List<Entregador> findByDisponivelTrue();

    /**
     * Lista entregadores disponíveis e ativos (não removidos via soft-delete).
     * Esta é a condição completa para um entregador poder receber uma nova entrega.
     * 
     * @return Lista de entregadores com disponivel = true e ativo = true.
     */
    List<Entregador> findByDisponivelTrueAndAtivoTrue();

    /**
     * Procura entregadores pela sua zona de atuação.
     * 
     * @param zonaAtuacao A zona onde o entregador opera (ex: "Lisboa", "Porto").
     * @return Lista de entregadores que atuam na zona especificada.
     */
    List<Entregador> findByZonaAtuacao(String zonaAtuacao);

    /**
     * Procura entregadores disponíveis numa determinada zona de atuação.
     * 
     * @param zonaAtuacao A zona onde se procura entregadores disponíveis.
     * @return Lista de entregadores disponíveis na zona especificada.
     */
    List<Entregador> findByZonaAtuacaoAndDisponivelTrue(String zonaAtuacao);

    /**
     * Procura um entregador pelo ID, verificando se está disponível..
     * 
     * @param id O identificador único do entregador.
     * @return Um Optional contendo o entregador se estiver disponível, ou vazio.
     */
    Optional<Entregador> findByIdAndDisponivelTrue(Long id);

    /**
     * Procura entregadores disponíveis, ativos e numa zona específica.
     * 
     * @param zona A zona onde se procura entregadores.
     * @return Lista de entregadores que cumprem todas as condições.
     */
    @Query("SELECT e FROM Entregador e WHERE e.disponivel = true AND e.ativo = true AND e.zonaAtuacao = :zona")
    List<Entregador> findEntregadoresDisponiveisPorZona(@Param("zona") String zona);

    /**
     * Lista entregadores que nunca realizaram nenhuma entrega.
     * 
     * @return Lista de entregadores sem entregas associadas.
     */
    @Query("SELECT e FROM Entregador e WHERE SIZE(e.entregas) = 0")
    List<Entregador> findEntregadoresSemEntregas();
}