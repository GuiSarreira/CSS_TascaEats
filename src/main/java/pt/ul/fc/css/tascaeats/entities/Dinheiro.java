package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;

/**
 * Subclasse de {@link Pagamento} que representa um pagamento em dinheiro.
 *
 * O pagamento em dinheiro é identificado pela coluna discriminadora
 * {@code tipo_pagamento = 'DINHEIRO'} na tabela {@code pagamento}, partilhada
 * com
 * as restantes subclasses ({@link Multibanco}, {@link MBWay}).
 *
 * Esta subclasse não possui campos adicionais relativamente à classe pai
 * {@link Pagamento},
 * uma vez que o pagamento em dinheiro não requer dados extra (não há referência
 * bancária
 * nem número de telemóvel). O processamento é confirmado manualmente pelo
 * restaurante
 * através do método {@link Pagamento#processar()}.
 */
@Entity
@DiscriminatorValue("DINHEIRO")
public class Dinheiro extends Pagamento {

    private Double troco;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected Dinheiro() {
    }

    /**
     * Cria um novo pagamento em dinheiro.
     *
     * @param pedido o pedido que este pagamento liquida; não pode ser {@code null}
     * @param preco  o montante a pagar em euros
     * @param troco  o troco a devolver ao cliente
     */
    public Dinheiro(Pedido pedido, Double preco, Double troco) {
        super(pedido, preco);
        this.troco = troco;
    }

    public Double getTroco() {
        return troco;
    }

    public void setTroco(Double troco) {
        this.troco = troco;
    }
}
