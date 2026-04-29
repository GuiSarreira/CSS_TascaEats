package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Avaliacao;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Avaliacao.
 */
@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByClienteIdAndRestauranteId(Long clienteId, Long restauranteId);

    List<Avaliacao> findByRestauranteId(Long restauranteId);

    List<Avaliacao> findByClienteId(Long clienteId);

    Optional<Avaliacao> findByPedidoId(Long pedidoId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.restaurante.id = :restauranteId")
    Double calcularMediaNotasPorRestaurante(@Param("restauranteId") Long restauranteId);
}
