package pt.ul.fc.css.tascaeats.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Classe que representa uma morada estruturada.
 *
 * Anotado com {@link Embeddable}: não tem tabela própria. As suas colunas
 * ficam inline na tabela da entidade que o embute ({@code cliente},
 * {@code restaurante}, {@code pedido}).
 *
 * Utilizado por:
 * - {@link Cliente#morada} — morada principal do cliente
 * - {@link Restaurante#morada} — localização física do restaurante
 * - {@link Pedido#enderecoEntrega} — morada de entrega do pedido
 */
@Embeddable
public class Endereco {

    /** Rua, número de porta e andar. */
    @Column(nullable = false)
    private String rua;

    /** Código postal em formato XXXX-YYY. */
    @Column(nullable = false)
    private String codigoPostal;

    /** Cidade onde a morada está localizada. */
    @Column(nullable = false)
    private String cidade;

    /** Construtor vazio exigido para deserialização JSON. */
    public Endereco() {
    }

    /**
     * Cria uma morada completa.
     *
     * @param rua          rua e número de porta
     * @param codigoPostal código postal
     * @param cidade       cidade
     */
    public Endereco(String rua, String codigoPostal, String cidade) {
        this.rua = rua;
        this.codigoPostal = codigoPostal;
        this.cidade = cidade;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
