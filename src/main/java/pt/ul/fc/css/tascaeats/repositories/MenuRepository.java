package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Restaurante;

import java.util.List;

/**
 * Repositório para gestão da persistência de objetos do tipo {@link Menu}.
 * Suporta queries por nome e por restaurante, bem como filtragem dinâmica
 * via {@link JpaSpecificationExecutor}.
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    /**
     * Procura menus cujo nome contenha a sequência indicada (insensível a maiúsculas).
     *
     * @param nome parte do nome a pesquisar
     * @return lista de menus correspondentes
     */
    List<Menu> findByNomeContainingIgnoreCase(String nome);

    /**
     * Procura menus associados a um determinado restaurante.
     *
     * @param restaurante o restaurante a filtrar
     * @return lista de menus do restaurante
     */
    List<Menu> findByRestaurantesContaining(Restaurante restaurante);

    /**
     * Query de negócio — Restaurante mais popular de uma franquia (menu partilhado).
     * Responde à query: "Qual o restaurante mais popular de uma franquia?"
     *
     * Como {@code Pedido} já não tem campo {@code restaurante} (modelo multi-restaurante),
     * não é possível associar diretamente um pedido a um restaurante específico da franquia.
     * A métrica usada é o número de avaliações recebidas: o restaurante com mais avaliações
     * é considerado o mais popular da franquia.
     *
     * @param menuId o identificador do menu partilhado (franquia)
     * @return lista de arrays {@code [Restaurante, totalAvaliacoes]} ordenada por total
     *         decrescente; o primeiro elemento é o restaurante mais popular da franquia
     */
    @Query("SELECT r, COUNT(a) AS totalAvaliacoes " +
           "FROM Restaurante r " +
           "JOIN r.avaliacoes a " +
           "WHERE r.menu.id = :menuId " +
           "GROUP BY r " +
           "ORDER BY totalAvaliacoes DESC")
    List<Object[]> findRestauranteMaisPopularDoMenu(@Param("menuId") Long menuId);
}
