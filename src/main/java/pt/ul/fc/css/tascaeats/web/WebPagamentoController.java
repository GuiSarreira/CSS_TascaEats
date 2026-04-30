package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Pagamento;
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
}
