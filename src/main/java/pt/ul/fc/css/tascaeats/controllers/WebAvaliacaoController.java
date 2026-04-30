package pt.ul.fc.css.tascaeats.controllers;

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
 * Ao contrário do {@link AvaliacaoController} REST, este controller retorna
 * views Thymeleaf e não JSON.
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

    /**
     * Exibe o formulário para criar uma nova avaliação a partir de um pedido entregue.
     *
     * @param pedidoId  ID do pedido que fundamenta a avaliação.
     * @param clienteId ID do cliente (opcional — para pré-preencher o formulário).
     * @param model     Modelo Thymeleaf.
     * @return Vista {@code avaliacoes/form}.
     */
    @GetMapping("/novo")
    public String formAvaliacao(@RequestParam Long pedidoId,
                                @RequestParam(required = false) Long clienteId,
                                Model model) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        model.addAttribute("pedido", pedido);
        model.addAttribute("clienteId", clienteId != null ? clienteId : pedido.getCliente().getId());
        return "avaliacoes/form";
    }

    /**
     * Submete uma nova avaliação via formulário web.
     *
     * @param clienteId   ID do cliente.
     * @param pedidoId    ID do pedido.
     * @param restauranteId ID do restaurante.
     * @param nota        Nota de 1 a 5.
     * @param comentario  Comentário opcional.
     * @return Redireccionamento para a lista de avaliações do cliente.
     */
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

    /**
     * Lista avaliações filtradas por cliente ou por restaurante.
     *
     * @param clienteId     ID do cliente (opcional).
     * @param restauranteId ID do restaurante (opcional).
     * @param model         Modelo Thymeleaf.
     * @return Vista {@code avaliacoes/lista}.
     */
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
