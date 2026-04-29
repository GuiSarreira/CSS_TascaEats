package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
