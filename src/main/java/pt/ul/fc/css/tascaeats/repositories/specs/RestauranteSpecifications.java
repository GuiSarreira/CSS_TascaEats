package pt.ul.fc.css.tascaeats.repositories.specs;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.Produto;
import pt.ul.fc.css.tascaeats.entities.ProdutoPedido;
import pt.ul.fc.css.tascaeats.entities.Restaurante;

import java.time.LocalTime;

/**
 * Especificações JPA para pesquisa dinâmica de {@link Restaurante}.
 */
public class RestauranteSpecifications {

    /**
     * Filtra restaurantes cujo nome contenha a sequência indicada (insensível a
     * maiúsculas).
     */
    public static Specification<Restaurante> comNome(String nome) {
        return (root, query, cb) -> nome == null ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    /**
     * Filtra restaurantes por tipo de cozinha (insensível a maiúsculas).
     */
    public static Specification<Restaurante> comTipoCozinha(String tipo) {
        return (root, query, cb) -> tipo == null ? null
                : cb.equal(cb.lower(root.get("tipoCozinha")), tipo.toLowerCase());
    }

    /**
     * Filtra restaurantes que estão abertos num determinado horário.
     */
    public static Specification<Restaurante> abertoNoHorario(LocalTime hora) {
        return (root, query, cb) -> {
            if (hora == null)
                return null;
            return cb.and(
                    cb.lessThanOrEqualTo(root.get("horarioAbertura"), hora),
                    cb.greaterThanOrEqualTo(root.get("horarioFecho"), hora));
        };
    }

    /**
     * Filtra restaurantes cujo preço médio dos seus produtos (via menu) está dentro
     * do intervalo indicado.
     * Restaurante N:1 Menu N:N Produto — a FK está em Restaurante.menu.
     */
    public static Specification<Restaurante> precoMedioEntre(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null)
                return null;

            // Subquery: AVG(preco) dos produtos do menu deste restaurante
            Subquery<Double> subquery = query.subquery(Double.class);
            var subRoot = subquery.from(Restaurante.class);
            Join<Restaurante, Menu> menuJoin = subRoot.join("menu");
            Join<Menu, Produto> produtoJoin = menuJoin.join("produtos");
            subquery.select(cb.avg(produtoJoin.get("preco")));
            subquery.where(cb.equal(subRoot, root));

            if (min != null && max != null)
                return cb.between(subquery, min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(subquery, min);
            return cb.lessThanOrEqualTo(subquery, max);
        };
    }

    /**
     * Filtra restaurantes com um número mínimo de avaliações recebidas.
     */
    public static Specification<Restaurante> comMinimoAvaliacoes(Integer n) {
        return (root, query, cb) -> n == null ? null
                : cb.greaterThanOrEqualTo(cb.size(root.get("avaliacoes")), n);
    }

    /**
     * Filtra restaurantes por cidade (insensível a maiúsculas).
     */
    public static Specification<Restaurante> comCidade(String cidade) {
        return (root, query, cb) -> cidade == null ? null
                : cb.equal(cb.lower(root.get("morada").get("cidade")), cidade.toLowerCase());
    }

    /**
     * Filtra restaurantes com um número mínimo de pedidos realizados.
     * Conta pedidos cujos produtos (via ProdutoPedido → Produto → Menu → Restaurante)
     * pertencem a este restaurante.
     */
    public static Specification<Restaurante> comMinimoPedidos(Integer minPedidos) {
        return (root, query, cb) -> {
            if (minPedidos == null)
                return null;

            // Subquery: COUNT(DISTINCT pedido) cujos itens têm produtos deste restaurante
            Subquery<Long> subquery = query.subquery(Long.class);
            var ppRoot = subquery.from(ProdutoPedido.class);
            Join<ProdutoPedido, Produto> produtoJoin = ppRoot.join("produto");
            Join<Produto, Menu> menuJoin = produtoJoin.join("menus");
            Join<Menu, Restaurante> restauranteJoin = menuJoin.join("restaurantes");
            Join<ProdutoPedido, Pedido> pedidoJoin = ppRoot.join("pedido");
            subquery.select(cb.countDistinct(pedidoJoin));
            subquery.where(cb.equal(restauranteJoin, root));

            return cb.greaterThanOrEqualTo(subquery, (long) minPedidos);
        };
    }
}
