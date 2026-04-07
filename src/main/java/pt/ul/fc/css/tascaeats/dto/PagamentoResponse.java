package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.entities.Pagamento;
import pt.ul.fc.css.tascaeats.entities.PagamentoStatus;
import pt.ul.fc.css.tascaeats.entities.Multibanco;
import pt.ul.fc.css.tascaeats.entities.MBWay;

import java.time.LocalDateTime;

/**
 * DTO de response para dados de pagamento.
 *
 * Usado na resposta do endpoint {@code POST /api/pedidos/{id}/pagamento}
 * e em consultas de pagamento de um pedido.
 *
 * O campo {@code tipoPagamento} é determinado pelo subtipo concreto da
 * entidade ({@link Multibanco}, {@link MBWay} ou {@code Dinheiro})
 * através de {@code instanceof}.
 */
public class PagamentoResponse {

    /** ID do pagamento. */
    private Long id;

    /** Montante pago em euros. */
    private Double preco;

    /**
     * Data e hora em que o pagamento foi processado ({@code null} se PENDING ou
     * FAILED).
     */
    private LocalDateTime dataPagamento;

    /** Estado atual do pagamento. */
    private PagamentoStatus status;

    /** Tipo de pagamento: {@code MULTIBANCO}, {@code MBWAY} ou {@code DINHEIRO}. */
    private String tipoPagamento;

    /** Construtor vazio para uso interno. */
    public PagamentoResponse() {
    }

    private PagamentoResponse(Long id, Double preco, LocalDateTime dataPagamento,
            PagamentoStatus status, String tipoPagamento) {
        this.id = id;
        this.preco = preco;
        this.dataPagamento = dataPagamento;
        this.status = status;
        this.tipoPagamento = tipoPagamento;
    }

    /**
     * Cria um {@code PagamentoResponse} a partir de uma entidade {@link Pagamento}.
     *
     * O tipo é determinado pelo subtipo concreto da entidade.
     *
     * @param p entidade pagamento
     * @return DTO preenchido com os campos do pagamento
     */
    public static PagamentoResponse from(Pagamento p) {
        String tipo;
        if (p instanceof Multibanco) {
            tipo = "MULTIBANCO";
        } else if (p instanceof MBWay) {
            tipo = "MBWAY";
        } else {
            tipo = "DINHEIRO";
        }
        return new PagamentoResponse(p.getId(), p.getPreco(), p.getDataPagamento(), p.getStatus(), tipo);
    }

    public Long getId() {
        return id;
    }

    public Double getPreco() {
        return preco;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public PagamentoStatus getStatus() {
        return status;
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }
}
