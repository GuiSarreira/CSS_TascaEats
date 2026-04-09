package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo Restaurante.
 * Fornece métodos para pesquisa baseada em identificadores únicos, localização e estado.
 */
@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    /**
     * Procura um restaurante pelo seu Número de Identificação Fiscal (NIF).
     * Como o NIF é único, devolve um Optional para evitar NullPointerException.
     * @param nif O NIF de 9 dígitos do restaurante.
     * @return Um Optional contendo o restaurante correspondente, ou vazio se não existir.
     */
    Optional<Restaurante> findByNif(String nif);

    /**
     * Procura restaurantes pelo seu nome exato.
     * @param nome O nome exato do restaurante a pesquisar.
     * @return Uma lista de restaurantes com o nome especificado.
     */
    List<Restaurante> findByNome(String nome);

    /**
     * Procura restaurantes cujo nome contenha uma sequência de caracteres (pesquisa parcial).
     * @param nome Parte do nome ou sequência a procurar (ex: "tasca").
     * @return Lista de restaurantes que contêm a sequência, ignorando capitalização.
     */
    List<Restaurante> findByNomeContainingIgnoreCase(String nome);

    /**
     * Filtra restaurantes situados numa cidade específica.
     * @param cidade O nome da cidade para filtrar os resultados.
     * @return Lista de restaurantes localizados na cidade fornecida.
     */
    List<Restaurante> findByCidade(String cidade);

    /**
     * Procura todos os restaurantes que estão atualmente abertos numa determinada cidade.
     * @param cidade O nome da cidade para filtrar a pesquisa.
     * @return Lista de restaurantes abertos (aberto = true) na cidade especificada.
     */
    List<Restaurante> findByCidadeAndAbertoTrue(String cidade);

    /**
     * Procura restaurantes por cidade de forma flexível, ignorando maiúsculas/minúsculas.
     * @param cidade O nome da cidade (ex: "PORTO", "porto" ou "Porto").
     * @return Lista de restaurantes encontrados na cidade.
     */
    List<Restaurante> findByCidadeIgnoreCase(String cidade);

    /**
     * Query de negócio — Restaurantes com maior volume de vendas.
     *
     * Agrega o {@code precoTotal} de todos os pedidos por restaurante
     * e devolve os resultados por ordem decrescente de volume.
     *
     * @return lista de arrays {@code [nome_restaurante, totalVendas]} ordenada por
     *         total decrescente
     */
    @Query("SELECT p.restaurante.nome, SUM(p.precoTotal) AS totalVendas "
        + "FROM Pedido p "
        + "WHERE p.status = pt.ul.fc.css.tascaeats.entities.PedidoStatus.DELIVERED "
        + "GROUP BY p.restaurante.id, p.restaurante.nome "
        + "ORDER BY totalVendas DESC")
    List<Object[]> findRestaurantesPorVolumeVendas();
}