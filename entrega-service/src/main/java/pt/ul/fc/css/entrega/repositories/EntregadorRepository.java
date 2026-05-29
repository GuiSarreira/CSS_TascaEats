package pt.ul.fc.css.entrega.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.entrega.entities.Entregador;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de {@link Entregador} no
 * microserviço.
 *
 * Ao contrário do repositório homólogo no monólito, não possui queries que
 * referenciam {@code ativo} (campo herdado de {@code User}), uma vez que o
 * {@code Entregador} do microserviço é uma entidade independente.
 */
@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    /**
     * Procura um entregador pelo seu email.
     *
     * @param email o email do entregador
     * @return Optional com o entregador, ou vazio se não existir
     */
    Optional<Entregador> findByEmail(String email);

    /**
     * Lista todos os entregadores disponíveis para novas entregas.
     *
     * @return lista de entregadores com {@code disponivel = true}
     */
    List<Entregador> findByDisponivelTrue();

    /**
     * Procura entregadores pela sua zona de atuação.
     *
     * @param zonaAtuacao a zona geográfica (ex: "Lisboa", "Porto")
     * @return lista de entregadores que atuam na zona
     */
    List<Entregador> findByZonaAtuacao(String zonaAtuacao);

    /**
     * Procura entregadores disponíveis numa determinada zona de atuação.
     * Query principal para a atribuição automática de entregas.
     *
     * @param zonaAtuacao a zona onde se procura entregadores
     * @return lista de entregadores disponíveis na zona
     */
    List<Entregador> findByZonaAtuacaoAndDisponivelTrue(String zonaAtuacao);

    /**
     * Retorna o primeiro entregador disponível.
     * Útil para atribuição automática rápida quando não se filtra por zona.
     *
     * @return Optional com um entregador disponível, ou vazio
     */
    Optional<Entregador> findFirstByDisponivelTrue();

    /**
     * Procura um entregador pelo ID, verificando se está disponível.
     *
     * @param id o identificador do entregador
     * @return Optional com o entregador se disponível, ou vazio
     */
    Optional<Entregador> findByIdAndDisponivelTrue(Long id);
}
