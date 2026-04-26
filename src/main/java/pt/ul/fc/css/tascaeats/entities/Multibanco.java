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

    private String referencia;
    private String bandeira;

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
     * @param bandeira   a bandeira do cartão (ex: "Visa", "Mastercard")
     */
    public Multibanco(Pedido pedido, Double preco, String referencia, String bandeira) {
        super(pedido, preco);
        this.referencia = referencia;
        this.bandeira = bandeira;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }
}
