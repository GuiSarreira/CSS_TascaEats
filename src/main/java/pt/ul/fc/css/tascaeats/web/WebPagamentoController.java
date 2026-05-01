package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.services.PagamentoService;
import pt.ul.fc.css.tascaeats.services.PedidoService;

/**
 * Controller MVC (Thymeleaf) para gestão de Pagamentos.
 *
 * Expõe o formulário de pagamento e a submissão do mesmo.
 * O formulário apresenta campos dinâmicos consoante o tipo de pagamento
 * selecionado (MBWAY, MULTIBANCO, DINHEIRO).
 */
@Controller
@RequestMapping("/pagamentos")
public class WebPagamentoController {

    private final PagamentoService pagamentoService;
    private final PedidoService pedidoService;

    public WebPagamentoController(PagamentoService pagamentoService, PedidoService pedidoService) {
        this.pagamentoService = pagamentoService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/novo")
    public String formPagamento(@RequestParam Long pedidoId,
            @RequestParam Long clienteId,
            Model model) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        model.addAttribute("pedido", pedido);
        model.addAttribute("clienteId", clienteId);
        return "pagamentos/form";
    }

    @PostMapping
    public String submeterPagamento(@RequestParam Long pedidoId,
            @RequestParam Long clienteId,
            @RequestParam String tipoPagamento,
            @RequestParam(required = false) String dadosExtra,
            @RequestParam(required = false) String bandeira,
            @RequestParam(required = false) Double troco) {
        pagamentoService.processarPagamento(pedidoId, tipoPagamento, dadosExtra, bandeira, troco);
        return "redirect:/pedidos/" + pedidoId;
    }

    // ─── Query 1: Média do Troco ─────────────────────────────────────────────

    /**
     * Query 1: Média do troco em pagamentos a dinheiro concluídos.
     * Exibe a estatística de media de troco devolvido em pagamentos à conta.
     */
    @GetMapping("/media-troco")
    public String mediaTroco(Model model) {
        Double media = pagamentoService.calcularMediaTroco();
        model.addAttribute("media", media);
        model.addAttribute("titulo", "Média do Troco");
        model.addAttribute("descricao", "Valor médio do troco em pagamentos a dinheiro concluídos");
        return "pagamentos/media-troco";
    }

    // ─── Query 5: Método de Pagamento Mais Utilizado ──────────────────────

    /**
     * FASE 1 — Query 5: Método de pagamento mais utilizado
     * Página que mostra estatísticas sobre quais métodos de pagamento são mais utilizados.
     */
    @GetMapping("/metodo-mais-utilizado")
    public String metodoMaisUtilizado(Model model) {
        var resultados = pagamentoService.metodosPagamentoMaisUtilizados();
        model.addAttribute("resultados", resultados);
        model.addAttribute("titulo", "Método de Pagamento Mais Utilizado");
        model.addAttribute("descricao", "Estatísticas de utilização de métodos de pagamento");
        return "pagamentos/metodo-mais-utilizado";
    }
}
