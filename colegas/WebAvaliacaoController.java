package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoRequest;
import pt.ul.fc.css.tascaeats.dto.AvaliacaoResponse;
import pt.ul.fc.css.tascaeats.dto.PedidoResponse;

@Controller
@RequestMapping("/avaliacoes")
public class WebAvaliacaoController {

    private final RestTemplate restTemplate;
    private final String API_BASE = "http://localhost:8080/api";

    public WebAvaliacaoController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/novo")
    public String formAvaliacao(@RequestParam Long pedidoId,
                                @RequestParam(required = false) Long clienteId,
                                Model model) {
        // Buscar dados do pedido (pode ser necessário obter o clienteId da sessão)
        PedidoResponse pedido = restTemplate.getForObject(
                API_BASE + "/pedidos/" + pedidoId, PedidoResponse.class);
        model.addAttribute("pedido", pedido);
        model.addAttribute("avaliacaoRequest", new AvaliacaoRequest());
        // Preencher clienteId se não vier na requisição
        if (clienteId != null) {
            ((AvaliacaoRequest) model.getAttribute("avaliacaoRequest")).setClienteId(clienteId);
        }
        return "avaliacoes/form";
    }

    @PostMapping
    public String submeterAvaliacao(@ModelAttribute AvaliacaoRequest request) {
        restTemplate.postForObject(API_BASE + "/avaliacoes", request, AvaliacaoResponse.class);
        return "redirect:/avaliacoes?clienteId=" + request.getClienteId();
    }

    @GetMapping
    public String listarAvaliacoes(@RequestParam(required = false) Long clienteId,
                                   @RequestParam(required = false) Long restauranteId,
                                   Model model) {
        if (clienteId != null) {
            String url = API_BASE + "/avaliacoes?clienteId=" + clienteId;
            AvaliacaoResponse[] avaliacoes = restTemplate.getForObject(url, AvaliacaoResponse[].class);
            model.addAttribute("avaliacoes", avaliacoes);
            model.addAttribute("tipo", "cliente");
        } else if (restauranteId != null) {
            String url = API_BASE + "/avaliacoes?restauranteId=" + restauranteId;
            AvaliacaoResponse[] avaliacoes = restTemplate.getForObject(url, AvaliacaoResponse[].class);
            model.addAttribute("avaliacoes", avaliacoes);
            model.addAttribute("tipo", "restaurante");
        }
        return "avaliacoes/lista";
    }
}