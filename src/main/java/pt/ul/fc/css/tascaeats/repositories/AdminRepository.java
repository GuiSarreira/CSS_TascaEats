package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Admin;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Admin.
 * Fornece métodos para pesquisa de administradores e análise dos restaurantes que gerem.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Procura um administrador pelo seu email.
     * @param email O email do administrador a pesquisar.
     * @return Um Optional contendo o administrador correspondente, ou vazio se não existir.
     */
    Optional<Admin> findByEmail(String email);

    /**
     * Lista todos os administradores que estão ativos.
     * @return Lista de administradores com campo ativo = true.
     */
    List<Admin> findByAtivoTrue();

    /**
     * Identifica administradores que já criaram pelo menos um restaurante.
     * @return Lista de administradores que possuem restaurantes associados.
     */
    @Query("SELECT a FROM Admin a WHERE SIZE(a.restaurantes) > 0")
    List<Admin> findAdminsComRestaurantes();
}