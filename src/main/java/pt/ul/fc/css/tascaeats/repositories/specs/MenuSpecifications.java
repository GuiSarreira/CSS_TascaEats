package pt.ul.fc.css.tascaeats.repositories.specs;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import pt.ul.fc.css.tascaeats.entities.Menu;
import pt.ul.fc.css.tascaeats.entities.Produto;

/**
 * Especificações JPA para pesquisa dinâmica de {@link Menu}.
 */
public class MenuSpecifications {

    /**
     * Filtra menus cujo nome contenha a sequência indicada (insensível a maiúsculas).
     * Retorna sem restrição se {@code nome} for {@code null}.
     */
    public static Specification<Menu> comNome(String nome) {
        return (root, query, cb) -> nome == null ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    /**
     * Filtra menus pelo número de produtos associados.
     * Qualquer extremo {@code null} é ignorado.
     */
    public static Specification<Menu> quantidadeProdutosEntre(Integer min, Integer max) {
        return (root, query, cb) -> {
            var size = cb.size(root.get("produtos"));
            if (min != null && max != null)
                return cb.between(size, min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(size, min);
            if (max != null)
                return cb.lessThanOrEqualTo(size, max);
            return null;
        };
    }

    /**
     * Filtra menus cujo preço médio dos produtos associados cai dentro do intervalo.
     * Qualquer extremo {@code null} é ignorado.
     */
    public static Specification<Menu> precoMedioEntre(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null)
                return null;

            Subquery<Double> subquery = query.subquery(Double.class);
            var subRoot = subquery.from(Menu.class);
            Join<Menu, Produto> produtoJoin = subRoot.join("produtos");
            subquery.select(cb.avg(produtoJoin.get("preco")));
            subquery.where(cb.equal(subRoot, root));

            if (min != null && max != null)
                return cb.between(subquery, min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(subquery, min);
            return cb.lessThanOrEqualTo(subquery, max);
        };
    }
}
