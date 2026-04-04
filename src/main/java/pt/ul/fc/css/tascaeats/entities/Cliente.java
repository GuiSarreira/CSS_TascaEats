package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "user_id")
public class Cliente extends User {

    @Column(nullable = false)
    private String morada;

    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    protected Cliente() {}

    public Cliente(String email, String nome, String password, String morada) {
        super(email, nome, password, "CLIENTE");
        this.morada = morada;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public void addPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        pedido.setCliente(this);
    }
}