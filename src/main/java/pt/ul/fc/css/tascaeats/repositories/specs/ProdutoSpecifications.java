package pt.ul.fc.css.tascaeats.repositories.specs;

import org.springframework.data.jpa.domain.Specification;
import pt.ul.fc.css.tascaeats.entities.Produto;
import java.time.LocalDateTime;

/**
 * Especificações JPA para filtragem dinâmica de Produtos.
 * Utiliza a Criteria API para construir queries SQL baseadas em filtros
 * opcionais.
 *
 * Filtros suportados:
 * - Nome: busca parcial (case-insensitive)
 * - Preço: intervalo (min/max)
 * - Categoria: correspondência exata
 * - Disponibilidade: booleano
 * - Popularidade: número mínimo de vezes pedido em intervalo de tempo
 * - Não eliminados: apenas produtos ativos
 */
public class ProdutoSpecifications {

    /**
     * Especificação para filtrar produtos por nome (substring, case-insensitive).
     *
     * @param nome Parte do nome a procurar
     * @return Specification que verifica se nome contém a sequência
     */
    public static Specification<Produto> comNome(String nome) {
        return (root, query, cb) -> {
            if (nome == null || nome.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("nome")),
                    "%" + nome.toLowerCase() + "%");
        };
    }

    /**
     * Especificação para filtrar produtos por intervalo de preço.
     *
     * @param precoMin Preço mínimo (inclusivo); null para ignorar limite mínimo
     * @param precoMax Preço máximo (inclusivo); null para ignorar limite máximo
     * @return Specification que valida o intervalo de preço
     */
    public static Specification<Produto> comPreco(Double precoMin, Double precoMax) {
        return (root, query, cb) -> {
            if (precoMin == null && precoMax == null) {
                return cb.conjunction();
            }
            if (precoMin != null && precoMax != null) {
                return cb.between(root.get("preco"), precoMin, precoMax);
            }
            if (precoMin != null) {
                return cb.greaterThanOrEqualTo(root.get("preco"), precoMin);
            }
            return cb.lessThanOrEqualTo(root.get("preco"), precoMax);
        };
    }

    /**
     * Especificação para filtrar produtos por categoria.
     *
     * @param categoria Categoria exata a procurar (ex: "Entrada", "Prato
     *                  Principal")
     * @return Specification que verifica correspondência de categoria
     */
    public static Specification<Produto> comCategoria(String categoria) {
        return (root, query, cb) -> {
            if (categoria == null || categoria.isBlank()) {
                return cb.conjunction();
            }

            String normalizedInput = categoria.trim().toLowerCase().replace('_', ' ');
            return cb.equal(
                    cb.lower(cb.function("replace", String.class, root.get("categoria"), cb.literal("_"),
                            cb.literal(" "))),
                    normalizedInput);
        };
    }

    /**
     * Especificação para filtrar produtos por disponibilidade.
     *
     * @param disponivel true para produtos disponíveis, false para esgotados
     * @return Specification que verifica o estado de disponibilidade
     */
    public static Specification<Produto> comDisponibilidade(Boolean disponivel) {
        return (root, query, cb) -> {
            if (disponivel == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("disponivel"), disponivel);
        };
    }

    /**
     * Especificação para filtrar produtos por popularidade (número de vezes
     * pedido).
     * Conta quantas vezes o produto foi incluído em pedidos dentro de um intervalo
     * de tempo.
     *
     * @param minVezes   Número mínimo de vezes que o produto deve ter sido pedido
     * @param dataInicio Data/hora inicial do intervalo (null para ignorar)
     * @param dataFim    Data/hora final do intervalo (null para ignorar)
     * @return Specification que filtra por popularidade com GROUP BY e HAVING
     */
    public static Specification<Produto> comPopularidade(Integer minVezes, LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        return (root, query, cb) -> {
            if (minVezes == null || minVezes <= 0) {
                return cb.conjunction();
            }

            // JOIN com ProdutoPedido
            var itensPedidoJoin = root.join("itensPedido");

            // Se há filtro de datas, aplicar WHERE com data do Pedido
            if (dataInicio != null || dataFim != null) {
                var pedidoJoin = itensPedidoJoin.join("pedido");
                var dataPedido = pedidoJoin.<LocalDateTime>get("dataHora");

                if (dataInicio != null && dataFim != null) {
                    query.having(cb.ge(cb.count(itensPedidoJoin), (long) minVezes))
                            .where(cb.between(dataPedido, dataInicio, dataFim));
                } else if (dataInicio != null) {
                    query.having(cb.ge(cb.count(itensPedidoJoin), (long) minVezes))
                            .where(cb.greaterThanOrEqualTo(dataPedido, dataInicio));
                } else {
                    query.having(cb.ge(cb.count(itensPedidoJoin), (long) minVezes))
                            .where(cb.lessThanOrEqualTo(dataPedido, dataFim));
                }
            } else {
                // Sem filtro de datas: contar todos os itens
                query.having(cb.ge(cb.count(itensPedidoJoin), (long) minVezes));
            }

            query.groupBy(root.get("id"));
            return cb.conjunction(); // WHERE já foi aplicada no query object
        };
    }

    /**
     * Especificação para filtrar apenas produtos não eliminados.
     *
     * @return Specification que valida eliminado = false
     */
    public static Specification<Produto> naoEliminado() {
        return (root, query, cb) -> cb.equal(root.get("eliminado"), false);
    }
}
