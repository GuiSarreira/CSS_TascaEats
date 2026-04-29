package pt.ul.fc.css.tascaeats.dto;

import java.util.List;

/**
 * DTO de request para os endpoints de criação e atualização de menus.
 *
 * @param nome            nome do menu
 * @param descricao       descrição do menu
 * @param produtoIds      lista de IDs dos produtos a associar (pode ser vazia ou null)
 * @param restauranteIds  lista de IDs dos restaurantes a associar (pode ser vazia ou null)
 */
public record MenuRequest(
        String nome,
        String descricao,
        List<Long> produtoIds,
        List<Long> restauranteIds) {
}
