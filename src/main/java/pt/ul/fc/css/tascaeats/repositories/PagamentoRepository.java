package pt.ul.fc.css.tascaeats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.entities.Pagamento;
import pt.ul.fc.css.tascaeats.entities.PagamentoStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gestão da persistência de objetos do tipo {@link Pagamento}.
 *
 * {@code Pagamento} usa herança {@code SINGLE_TABLE} com subtipo discriminado
 * pela coluna {@code tipo_pagamento} (valores: {@code MULTIBANCO},
 * {@code MBWAY},
 * {@code DINHEIRO}). Em JPQL usa-se {@code TYPE(p)} para filtrar por subtipo,
 * em vez de aceder à coluna discriminadora diretamente.
 */
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    /**
     * Procura o pagamento associado a um determinado pedido.
     * Como a relação é 1:1, devolve um {@link Optional}.
     *
     * @param pedidoId o identificador único do pedido
     * @return o pagamento associado, ou vazio se o pedido ainda não foi pago
     */
    Optional<Pagamento> findByPedidoId(Long pedidoId);

    /**
     * Lista todos os pagamentos com um determinado estado.
     *
     * @param status o estado a filtrar (ex: {@link PagamentoStatus#PENDING})
     * @return lista de pagamentos naquele estado
     */
    List<Pagamento> findByStatus(PagamentoStatus status);

    /**
     * Query de negócio — Método de pagamento mais utilizado.
     * Responde à query: "Qual o método de pagamento mais utilizado na plataforma?"
     *
     * Usa {@code TYPE(p)} para obter o nome da classe concreta do subtipo
     * ({@code Multibanco}, {@code MBWay}, {@code Dinheiro}) sem aceder à coluna
     * discriminadora directamente.
     *
     * @return lista de arrays {@code [tipoSimples, contagem]} ordenada por contagem
     *         decrescente,
     *         onde {@code tipoSimples} é o nome simples da classe Java (ex:
     *         {@code "Multibanco"})
     */
    @Query("SELECT TYPE(p), COUNT(p) AS contagem " + "FROM Pagamento p " +
            "WHERE p.status = pt.ul.fc.css.tascaeats.entities.PagamentoStatus.COMPLETED " +
            "GROUP BY TYPE(p) " + "ORDER BY contagem DESC")
    List<Object[]> findMetodoPagamentoMaisUtilizado();

    /**
     * Verifica se já existe um pagamento associado a um pedido.
     * Útil para garantir que um pedido só tem um pagamento (regra de negócio).
     *
     * @param pedidoId o identificador do pedido
     * @return {@code true} se já existir um pagamento para o pedido
     */
    boolean existsByPedidoId(Long pedidoId);
}
