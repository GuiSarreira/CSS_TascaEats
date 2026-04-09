package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.User;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo User.
 * Fornece métodos para pesquisa baseada em email, role e estado de atividade.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Procura um utilizador pelo seu email.
     * O email é único na base de dados, pelo que no máximo um utilizador será retornado.
     * @param email O email do utilizador a pesquisar.
     * @return Um Optional contendo o utilizador correspondente, ou vazio se não existir.
     */
    Optional<User> findByEmail(String email);

    /**
     * Procura um utilizador pelo seu email, considerando apenas utilizadores ativos.
     * @param email O email do utilizador a pesquisar.
     * @return Um Optional contendo o utilizador ativo correspondente, ou vazio se não existir.
     */
    Optional<User> findByEmailAndAtivoTrue(String email);

    /**
     * Lista todos os utilizadores que estão ativos (não foram removidos via soft-delete).
     * @return Lista de utilizadores com campo ativo = true.
     */
    List<User> findByAtivoTrue();

    /**

     * Verifica se já existe um utilizador registado com o email fornecido.
     * Útil para validações antes de criar um novo utilizador.
     * @param email O email a verificar.
     * @return true se o email já estiver registado, false caso contrário.
     */
    boolean existsByEmail(String email);

    /**
     * Procura utilizadores cujo nome contenha uma sequência de caracteres (pesquisa parcial).
     * @param nome Parte do nome ou sequência a procurar.
     * @return Lista de utilizadores cujo nome contém a sequência fornecida.
     */
    @Query("SELECT u FROM User u WHERE u.nome LIKE %:nome%")
    List<User> buscarPorNome(@Param("nome") String nome);

}