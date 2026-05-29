package pt.ul.fc.css.entrega.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um entregador no microserviço de entregas.
 *
 * Ao contrário do monólito, esta entidade não herda de {@code User}.
 * É uma "cópia leve" que mantém apenas os campos necessários para a lógica de
 * atribuição de entregas. Não possui password, dataRegisto, nem relação com a
 * hierarquia de utilizadores do monólito.
 *
 * Campos:
 * - {@code id} — chave primária gerada automaticamente
 * - {@code nome} — nome completo do entregador
 * - {@code email} — email único, usado para identificação
 * - {@code veiculo} — tipo de veículo utilizado
 * - {@code zonaAtuacao} — zona geográfica de atuação
 * - {@code disponivel} — flag de disponibilidade para novas entregas
 */
@Entity
@Table(name = "entregador")
public class Entregador {

    /** Identificador único, gerado automaticamente pela base de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do entregador. */
    @Column(nullable = false)
    private String nome;

    /** Endereço de email do entregador. Deve ser único. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Tipo de veículo utilizado para as entregas (ex: moto, bicicleta, carro). */
    @Column(nullable = false)
    private String veiculo;

    /** Zona geográfica de atuação do entregador (ex: Lisboa, Porto). */
    @Column(nullable = false)
    private String zonaAtuacao;

    /**
     * Indica se o entregador está disponível para receber novas entregas.
     * {@code true} por defeito; muda para {@code false} ao atribuir uma entrega;
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

    /** Construtor protegido exigido pelo JPA. */
    protected Entregador() {
    }

    /**
     * Cria um novo entregador, disponível por defeito.
     *
     * @param nome        nome completo
     * @param email       endereço de email; deve ser único
     * @param veiculo     tipo de veículo utilizado
     * @param zonaAtuacao zona geográfica de atuação
     */
    public Entregador(String nome, String email, String veiculo, String zonaAtuacao) {
        this.nome = nome;
        this.email = email;
        this.veiculo = veiculo;
        this.zonaAtuacao = zonaAtuacao;
        this.disponivel = true;
    }

    // ── Getters e Setters ────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // ── Métodos de negócio ───────────────────────────────────────────

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
     * No microserviço, basta estar disponível (não há conceito de
     * {@code ativo/User}).
     *
     * @return {@code true} se pode receber entrega
     */
    public boolean podeReceberEntrega() {
        return this.disponivel;
    }
}
