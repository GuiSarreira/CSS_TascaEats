package pt.ul.fc.css.tascaeats.dto;

/**
 * DTO de request para o endpoint {@code POST /api/pedidos/{id}/pagamento}.
 *
 * Contém o tipo de pagamento e, quando aplicável, um dado específico
 * do método escolhido:
 * - {@code MULTIBANCO} — {@code dadosExtra} com a referência Multibanco>
 * - {@code MBWAY} — {@code dadosExtra} com o número de telemóvel
 * - {@code DINHEIRO} — {@code dadosExtra} pode ser {@code null}
 *
 * Exemplos de JSON:
 * 
 * {@code
 *  { "tipoPagamento": "MBWAY", "dadosExtra": "912345678" }
 *  { "tipoPagamento": "MULTIBANCO", "dadosExtra": "123 456 789" }
 *  { "tipoPagamento": "DINHEIRO" }
 * }
 */
public class PagamentoRequest {

    /**
     * Tipo de pagamento. Valores aceites (case-insensitive):
     * {@code MULTIBANCO}, {@code MBWAY}, {@code DINHEIRO}.
     */
    private String tipoPagamento;

    /**
     * Dado específico do método de pagamento.
     * Referência Multibanco, telemóvel MB WAY, ou {@code null} para Dinheiro.
     */
    private String dadosExtra;

    /** Construtor vazio exigido para deserialização do JSON. */
    public PagamentoRequest() {
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public String getDadosExtra() {
        return dadosExtra;
    }

    public void setDadosExtra(String dadosExtra) {
        this.dadosExtra = dadosExtra;
    }
}
