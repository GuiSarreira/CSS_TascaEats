package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um entregador da plataforma TascaEats.
 *
 * Subclasse de {@link User} com papel {@code "ENTREGADOR"}. Um entregador
 * recebe {@link Entrega entregas} atribuídas quando está {@code disponivel = true}.
 *
 * Herança — JOINED
 * Os atributos comuns ficam na tabela {@code users}. Esta classe adiciona
 * as colunas {@code veiculo}, {@code zona_atuacao} e {@code disponivel}
 * na tabela {@code entregador}.
 */
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Entregador extends User {

    /** Tipo de veículo utilizado para as entregas. */
    @Column(nullable = false)
    private String veiculo;

    /** Zona geográfica de atuação do entregador. */
    @Column(nullable = false)
    private String zonaAtuacao;

    /**
     * Indica se o entregador está disponível para receber novas entregas.
     * {@code true} por defeito; mudar para {@code false} ao atribuir uma entrega;
     * reposto a {@code true} quando a entrega é concluída ou cancelada.
     */
    @Column(nullable = false)
    private boolean disponivel = true;

    /**
     * Entregas realizadas por este entregador.
     * Lado fraco da relação — o dono é {@link Entrega}.
     */
    @OneToMany(mappedBy = "entregador")
    private List<Entrega> entregas = new ArrayList<>();

    /** Construtor protegido exigido pelo JPA.*/
    protected Entregador() {}

    /**
     * Cria um novo entregador, disponível por defeito.
     *
     * @param email        endereço de email; deve ser único
     * @param nome         nome completo
     * @param password     password em plain text
     * @param veiculo      tipo de veículo utilizado
     * @param zonaAtuacao  zona geográfica de atuação
     */
    public Entregador(String email, String nome, String password, String veiculo, String zonaAtuacao) {
        super(email, nome, password);
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

    /**
     * Adiciona uma entrega a este entregador, mantendo a consistência bidirecional.
     *
     * @param entrega a entrega a associar
     */
    public void addEntrega(Entrega entrega) {
        this.entregas.add(entrega);
        entrega.setEntregador(this);
    }

    /**
     * Indica se o entregador pode receber uma nova entrega.
     * Requer que esteja disponível ({@code disponivel = true}) e com a conta ativa.
     *
     * @return {@code true} se pode receber entrega
     */
    public boolean podeReceberEntrega() {
        return this.disponivel && this.isAtivo();
    }
}