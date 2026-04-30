package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Avaliacao;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;
import pt.ul.fc.css.tascaeats.services.PedidoService;

import java.util.List;

/**
 * Controller MVC (Thymeleaf) para gestão de Avaliações.
 *
 * Expõe páginas web para criar, listar e visualizar avaliações de restaurantes.
 * Ao contrário do
 * {@link pt.ul.fc.css.tascaeats.controllers.AvaliacaoController} REST,
 * este controller retorna views Thymeleaf e não JSON.
 */
@Controller
@RequestMapping("/avaliacoes")
public class WebAvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final PedidoService pedidoService;

    public WebAvaliacaoController(AvaliacaoService avaliacaoService, PedidoService pedidoService) {
        this.avaliacaoService = avaliacaoService;
        this.pedidoService = pedidoService;
    }

    // ─── Formulário de nova avaliação ─────────────────────────────────────────

    @GetMapping("/novo")
    public String formAvaliacao(@RequestParam Long pedidoId,
            @RequestParam(required = false) Long clienteId,
            Model model) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        model.addAttribute("pedido", pedido);
        model.addAttribute("clienteId", clienteId != null ? clienteId : pedido.getCliente().getId());
        return "avaliacoes/form";
    }

    @PostMapping
    public String submeterAvaliacao(@RequestParam Long clienteId,
            @RequestParam Long pedidoId,
            @RequestParam Long restauranteId,
            @RequestParam int nota,
            @RequestParam(required = false) String comentario) {
        avaliacaoService.criarAvaliacao(clienteId, restauranteId, pedidoId, nota, comentario);
        return "redirect:/avaliacoes?clienteId=" + clienteId;
    }

    // ─── Listagem ─────────────────────────────────────────────────────────────

    @GetMapping
    public String listarAvaliacoes(@RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long restauranteId,
            Model model) {
        List<Avaliacao> avaliacoes;
        String tipo;
        if (clienteId != null) {
            avaliacoes = avaliacaoService.obterAvaliacoesPorCliente(clienteId);
            tipo = "cliente";
            model.addAttribute("clienteId", clienteId);
        } else if (restauranteId != null) {
            avaliacoes = avaliacaoService.obterAvaliacoesPorRestaurante(restauranteId);
            tipo = "restaurante";
            model.addAttribute("restauranteId", restauranteId);
        } else {
            avaliacoes = List.of();
            tipo = "nenhum";
        }
        model.addAttribute("avaliacoes", avaliacoes);
        model.addAttribute("tipo", tipo);
        return "avaliacoes/lista";
    }
}
