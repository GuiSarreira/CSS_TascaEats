package pt.ul.fc.css.tascaeats.repositories.specs;

import org.springframework.data.jpa.domain.Specification;
import pt.ul.fc.css.tascaeats.entities.*;

/**
 * Especificações JPA para pesquisa dinâmica de {@link User}.
 * Suporta filtragem por nome, tipo de utilizador, número de pedidos (clientes)
 * e número de entregas (entregadores).
 */
public class UserSpecifications {

    /**
     * Filtra utilizadores cujo nome contenha a sequência indicada (insensível a
     * maiúsculas).
     * Retorna sem restrição se {@code nome} for {@code null}.
     */
    public static Specification<User> comNome(String nome) {
        return (root, query, cb) -> nome == null ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    /**
     * Filtra utilizadores pelo tipo: CLIENTE, ENTREGADOR ou ADMIN.
     * Com JOINED inheritance, faz teste usando joins opcionais.
     * Retorna sem restrição se {@code tipo} for {@code null} ou em branco.
     *
     * @param tipo tipo de utilizador (case-insensitive): "CLIENTE", "ENTREGADOR",
     *             "ADMIN"
     */
    public static Specification<User> comTipo(String tipo) {
        return (root, query, cb) -> {
            if (tipo == null || tipo.isBlank())
                return null;

            return switch (tipo.toUpperCase()) {
                case "CLIENTE" -> {
                    // Verifica se existe um join com Cliente (ou equivalente com TREAT)
                    yield cb.isNotNull(cb.treat(root, Cliente.class));
                }
                case "ENTREGADOR" -> {
                    yield cb.isNotNull(cb.treat(root, Entregador.class));
                }
                case "ADMIN" -> {
                    yield cb.isNotNull(cb.treat(root, Admin.class));
                }
                default -> null;
            };
        };
    }

    /**
     * Filtra clientes pelo número mínimo de pedidos realizados.
     * Apenas aplicável a utilizadores do tipo {@link Cliente}.
     * Retorna sem restrição se {@code minPedidos} for {@code null}.
     *
     * @param minPedidos número mínimo de pedidos
     */
    public static Specification<User> comMinimoPedidos(Integer minPedidos) {
        return (root, query, cb) -> {
            if (minPedidos == null)
                return null;

            // Usa TREAT para fazer downcast seguro em herança JOINED
            var cliente = cb.treat(root, Cliente.class);
            var pedidosJoin = cliente.join("pedidos", jakarta.persistence.criteria.JoinType.LEFT);
            query.groupBy(root);
            return cb.greaterThanOrEqualTo(cb.count(pedidosJoin), minPedidos.longValue());
        };
    }

    /**
     * Filtra entregadores pelo número mínimo de entregas realizadas.
     * Apenas aplicável a utilizadores do tipo {@link Entregador}.
     * Retorna sem restrição se {@code minEntregas} for {@code null}.
     *
     * @param minEntregas número mínimo de entregas
     */
    public static Specification<User> comMinimoEntregas(Integer minEntregas) {
        return (root, query, cb) -> {
            if (minEntregas == null)
                return null;

            // Usa TREAT para fazer downcast seguro em herança JOINED
            var entregador = cb.treat(root, Entregador.class);
            var entregasJoin = entregador.join("entregas", jakarta.persistence.criteria.JoinType.LEFT);
            query.groupBy(root);
            return cb.greaterThanOrEqualTo(cb.count(entregasJoin), minEntregas.longValue());
        };
    }

    /**
     * Filtra apenas utilizadores ativos (não removidos via soft-delete).
     */
    public static Specification<User> ativo() {
        return (root, query, cb) -> cb.equal(root.get("ativo"), true);
    }
}
