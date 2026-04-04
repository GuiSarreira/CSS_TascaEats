package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;

/**
 * Subclasse de {@link Pagamento} que representa um pagamento por Multibanco.
 *
 * O Multibanco é identificado pela coluna discriminadora
 * {@code tipo_pagamento = 'MULTIBANCO'}
 * na tabela {@code pagamento}, partilhada com as restantes subclasses
 * ({@link MBWay}, {@link Dinheiro}).
 *
 * O campo específico desta subclasse é a {@code referencia} Multibanco
 * (entidade + referência),
 * utilizada para identificar o pagamento no sistema bancário.
 */
@Entity
@DiscriminatorValue("MULTIBANCO")
public class Multibanco extends Pagamento {

    /**
     * Referência Multibanco fornecida ao cliente para efetuar o pagamento.
     */
    @Column(name = "referencia")
    private String referencia;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected Multibanco() {
    }

    /**
     * Cria um novo pagamento por Multibanco.
     *
     * @param pedido     o pedido que este pagamento liquida; não pode ser
     *                   {@code null}
     * @param preco      o montante a pagar em euros
     * @param referencia a referência Multibanco gerada para este pagamento
     */
    public Multibanco(Pedido pedido, Double preco, String referencia) {
        super(pedido, preco);
        this.referencia = referencia;
    }

    /**
     * Devolve a referência Multibanco associada a este pagamento.
     *
     * @return referência Multibanco
     */
    public String getReferencia() {
        return referencia;
    }

    /**
     * Define a referência Multibanco deste pagamento.
     *
     * @param referencia nova referência Multibanco
     */
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
