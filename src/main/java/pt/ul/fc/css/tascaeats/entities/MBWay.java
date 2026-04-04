package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;

/**
 * Subclasse de {@link Pagamento} que representa um pagamento por MB WAY.
 *
 * O MB WAY é identificado pela coluna discriminadora
 * {@code tipo_pagamento = 'MBWAY'}
 * na tabela {@code pagamento}, partilhada com as restantes subclasses
 * ({@link Multibanco}, {@link Dinheiro}).
 *
 * O campo específico desta subclasse é o {@code telemovel} associado à conta MB
 * WAY
 * do cliente, para onde o pedido de pagamento é enviado.
 */
@Entity
@DiscriminatorValue("MBWAY")
public class MBWay extends Pagamento {

    /**
     * Número de telemóvel associado à conta MB WAY do cliente.
     * É para este número que o pedido de confirmação de pagamento é enviado.
     */
    @Column(name = "telemovel_mbway")
    private String telemovel;

    /**
     * Construtor protegido exigido pelo JPA.
     */
    protected MBWay() {
    }

    /**
     * Cria um novo pagamento por MB WAY.
     *
     * @param pedido    o pedido que este pagamento liquida; não pode ser
     *                  {@code null}
     * @param preco     o montante a pagar em euros
     * @param telemovel o número de telemóvel MB WAY do cliente
     */
    public MBWay(Pedido pedido, Double preco, String telemovel) {
        super(pedido, preco);
        this.telemovel = telemovel;
    }

    /**
     * Devolve o número de telemóvel MB WAY associado a este pagamento.
     *
     * @return número de telemóvel
     */
    public String getTelemovel() {
        return telemovel;
    }

    /**
     * Define o número de telemóvel MB WAY deste pagamento.
     *
     * @param telemovel novo número de telemóvel
     */
    public void setTelemovel(String telemovel) {
        this.telemovel = telemovel;
    }
}
