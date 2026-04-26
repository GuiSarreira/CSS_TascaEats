package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe que representa um cliente da plataforma TascaEats.
 *
 * Subclasse de {@link User} com papel {@code "CLIENTE"}. Um cliente
 * pode criar pedidos em restaurantes abertos.
 *
 * Herança — JOINED
 * Os atributos comuns ficam na tabela {@code users}. Esta classe adiciona
 * a coluna {@code morada} na tabela {@code cliente}.
 */
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Cliente extends User {

    /** Lista de moradas do cliente, usadas como sugestão de endereço de entrega. */
    @ElementCollection
    @CollectionTable(name = "cliente_moradas", joinColumns = @JoinColumn(name = "cliente_id"))
    private List<Endereco> moradas = new ArrayList<>();

    /**
     * Pedidos efetuados por este cliente.
     * Lado inverso da relação — o dono é {@link Pedido#getCliente()}.
     */
    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA.*/
    protected Cliente() {}

    /**
     * Cria um novo cliente sem morada.
     *
     * @param email    endereço de email; deve ser único
     * @param nome     nome completo
     * @param password password em plain text
     */
    public Cliente(String email, String nome, String password) {
        super(email, nome, password);
    }

    /**
     * Cria um novo cliente.
     *
     * @param email    endereço de email; deve ser único
     * @param nome     nome completo
     * @param password password em plain text
     * @param morada   morada principal do cliente
     */
    public Cliente(String email, String nome, String password, Endereco morada) {
        super(email, nome, password);
        this.moradas.add(morada);
    }

    public List<Endereco> getMoradas() {
        return Collections.unmodifiableList(moradas);
    }

    public void adicionarMorada(Endereco morada) {
        this.moradas.add(morada);
    }

    public void removerMorada(Endereco morada) {
        this.moradas.remove(morada);
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    /**
     * Adiciona um pedido a este cliente, mantendo a consistência bidirecional.
     *
     * @param pedido o pedido a associar
     */
    public void addPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        pedido.setCliente(this);
    }
}