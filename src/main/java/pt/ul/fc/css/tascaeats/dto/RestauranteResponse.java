package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Endereco;
import pt.ul.fc.css.tascaeats.entities.Restaurante;

/**
 * DTO de response para dados de restaurante.
 *
 * Usado nos endpoints de listagem e detalhe de restaurantes
 * ({@code GET /api/restaurantes}, {@code GET /api/restaurantes/{id}},
 * {@code GET /api/restaurantes?nome=X},
 * {@code GET /api/restaurantes?cidade=X}).
 *
 * Não expõe a lista de pedidos nem produtos — esses têm os seus próprios
 * endpoints dedicados.
 */
public class RestauranteResponse {

    /** ID do restaurante. */
    private Long id;

    /** Nome do restaurante. */
    private String nome;

    /** NIF do restaurante. */
    private String nif;

    /** Morada do restaurante (rua, código postal e cidade). */
    private Endereco morada;

    /** Se o restaurante está atualmente aberto para receber pedidos. */
    private boolean aberto;

    /** Construtor vazio para uso interno. */
    public RestauranteResponse() {
    }

    private RestauranteResponse(Long id, String nome, String nif, Endereco morada, boolean aberto) {
        this.id = id;
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.aberto = aberto;
    }

    /**
     * Cria um {@code RestauranteResponse} a partir de uma entidade
     * {@link Restaurante}.
     *
     * @param r entidade restaurante
     * @return DTO preenchido com os campos do restaurante
     */
    public static RestauranteResponse from(Restaurante r) {
        return new RestauranteResponse(
                r.getId(),
                r.getNome(),
                r.getNif(),
                r.getMorada(),
                r.isAberto());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNif() {
        return nif;
    }

    public Endereco getMorada() {
        return morada;
    }

    public boolean isAberto() {
        return aberto;
    }
}
