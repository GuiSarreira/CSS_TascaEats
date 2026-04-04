package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entregadores")
@PrimaryKeyJoinColumn(name = "user_id")
public class Entregador extends User {

    @Column(nullable = false)
    private String veiculo;

    @Column(nullable = false)
    private String zonaAtuacao;

    @Column(nullable = false)
    private boolean disponivel = true;

    @OneToMany(mappedBy = "entregador")
    private List<Entrega> entregas = new ArrayList<>();

    protected Entregador() {}

    public Entregador(String email, String nome, String password, String veiculo, String zonaAtuacao) {
        super(email, nome, password, "ENTREGADOR");
        this.veiculo = veiculo;
        this.zonaAtuacao = zonaAtuacao;
        this.disponivel = true;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    public String getZonaAtuacao() {
        return zonaAtuacao;
    }

    public void setZonaAtuacao(String zonaAtuacao) {
        this.zonaAtuacao = zonaAtuacao;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public List<Entrega> getEntregas() {
        return entregas;
    }

    public void setEntregas(List<Entrega> entregas) {
        this.entregas = entregas;
    }

    public void addEntrega(Entrega entrega) {
        this.entregas.add(entrega);
        entrega.setEntregador(this);
    }

    public boolean podeReceberEntrega() {
        return this.disponivel && this.isAtivo();
    }
}