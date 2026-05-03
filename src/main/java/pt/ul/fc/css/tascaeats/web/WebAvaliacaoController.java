package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoRequest;
import pt.ul.fc.css.tascaeats.entities.Avaliacao;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
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
            Model model,
            RedirectAttributes redirectAttributes) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);

        if (avaliacaoService.obterAvaliacoesPorCliente(clienteId != null ? clienteId : pedido.getCliente().getId())
                .stream()
                .anyMatch(a -> a.getPedido() != null && a.getPedido().getId().equals(pedidoId))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Este pedido já tem uma avaliação.");
            return "redirect:/pedidos?clienteId=" + (clienteId != null ? clienteId : pedido.getCliente().getId());
        }

        Restaurante restaurante = pedido.getProdutosPedido().stream()
                .flatMap(pp -> pp.getProduto().getMenus().stream())
                .flatMap(menu -> menu.getRestaurantes().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Não foi possível resolver o restaurante do pedido"));

        AvaliacaoRequest request = new AvaliacaoRequest();
        request.setPedidoId(pedido.getId());
        request.setClienteId(clienteId != null ? clienteId : pedido.getCliente().getId());
        request.setRestauranteId(restaurante.getId());
        request.setNota(5);

        model.addAttribute("pedido", pedido);
        model.addAttribute("restauranteNome", restaurante.getNome());
        model.addAttribute("avaliacaoRequest", request);
        return "avaliacoes/form";
    }

    @PostMapping
    public String submeterAvaliacao(@RequestParam Long clienteId,
            @RequestParam Long pedidoId,
            @RequestParam Long restauranteId,
            @RequestParam int nota,
            @RequestParam(required = false) String comentario,
            RedirectAttributes redirectAttributes) {
        try {
            avaliacaoService.criarAvaliacao(clienteId, restauranteId, pedidoId, nota, comentario);
            redirectAttributes.addFlashAttribute("successMessage", "Avaliação submetida com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage() != null ? e.getMessage() : "Não foi possível submeter a avaliação.");
        }
        return "redirect:/pedidos?clienteId=" + clienteId;
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
