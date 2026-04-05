package pt.ul.fc.css.tascaeats.entities;

/**
 * Enumeração que representa os papéis possíveis de um {@link User}.
 *
 * Cada utilizador tem exatamente um papel, definido no momento do registo e
 * imutável. O papel determina as operações que o utilizador pode realizar:
 * - {@link #CLIENTE} — pode criar e gerir os seus próprios pedidos.
 * - {@link #ADMIN} — pode criar e gerir restaurantes.
 * - {@link #ENTREGADOR} — pode ser atribuído a entregas de pedidos.
 *
 * Armazenado na coluna {@code role} da tabela {@code users} como {@code STRING}
 * via {@code @Enumerated(EnumType.STRING)}.
 */
public enum UserTypes {

    /** Utilizador final que cria pedidos na plataforma. */
    CLIENTE,

    /** Administrador responsável pela gestão de um ou mais restaurantes. */
    ADMIN,

    /** Estafeta que realiza a entrega física dos pedidos ao cliente. */
    ENTREGADOR
}
