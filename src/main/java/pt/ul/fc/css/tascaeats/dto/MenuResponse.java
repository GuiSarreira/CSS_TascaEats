package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Menu;

/**
 * DTO de response para dados de um menu.
 *
 * Usado nos endpoints de criação, atualização e listagem de menus.
 * Inclui contagens de produtos e restaurantes associados para evitar
 * expor grafos inteiros de entidades JPA.
 */
public record MenuResponse(
        Long id,
        String nome,
        String descricao,
        int quantidadeProdutos,
        int quantidadeRestaurantes) {

    /**
     * Cria um {@code MenuResponse} a partir de uma entidade {@link Menu}.
     *
     * @param m entidade menu
     * @return DTO preenchido com os campos do menu
     */
    public static MenuResponse from(Menu m) {
        return new MenuResponse(
                m.getId(),
                m.getNome(),
                m.getDescricao(),
                m.getProdutos().size(),
                m.getRestaurantes().size());
    }
}
