package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller MVC (Thymeleaf) para gestão de Pedidos.
 *
 * Expõe páginas web para criar, listar, consultar e cancelar pedidos.
 * A lógica de negócio é delegada nos serviços existentes (não usa
 * RestTemplate).
 */
@Controller
@RequestMapping("/pedidos")
public class WebPedidoController {

    private final PedidoService pedidoService;
    private final UserService userService;
    private final RestauranteService restauranteService;

    public WebPedidoController(PedidoService pedidoService,
            UserService userService,
            RestauranteService restauranteService) {
        this.pedidoService = pedidoService;
        this.userService = userService;
        this.restauranteService = restauranteService;
    }

    // ─── Novo Pedido ─────────────────────────────────────────────────────────

    @GetMapping("/novo")
    public String novoPedido(@RequestParam Long clienteId, Model model) {
        Cliente cliente = userService.buscarClientePorId(clienteId);
        List<Restaurante> restaurantes = restauranteService.listarTodos();
        model.addAttribute("cliente", cliente);
        model.addAttribute("moradas", cliente.getMoradas());
        model.addAttribute("restaurantes", restaurantes);
        return "pedidos/novo";
    }

    @PostMapping
    public String submeterPedido(@RequestParam Long clienteId,
            @RequestParam(required = false) Integer moradaIndex,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam(required = false) String cidade,
            @RequestParam Map<String, String> allParams) {
        Map<Long, Integer> itens = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("qty_")) {
                try {
                    Long produtoId = Long.parseLong(entry.getKey().substring(4));
                    int qty = Integer.parseInt(entry.getValue());
                    if (qty > 0) {
                        itens.put(produtoId, qty);
                    }
                } catch (NumberFormatException ignored) {
                    // ignora parâmetros inválidos
                }
            }
        }

        Endereco novaModara = (rua != null && !rua.isBlank())
                ? new Endereco(rua, codigoPostal, cidade)
                : null;

        pedidoService.criarPedido(clienteId, moradaIndex, novaModara, itens);
        return "redirect:/pedidos?clienteId=" + clienteId;
    }

    // ─── Listagem ─────────────────────────────────────────────────────────────

    @GetMapping
    public String listarPedidos(@RequestParam Long clienteId,
            @RequestParam(required = false) PedidoStatus status,
            Model model) {
        List<Pedido> pedidos = pedidoService.buscarPorCliente(clienteId, status);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("statusAtual", status);
        model.addAttribute("todosStatus", PedidoStatus.values());
        return "pedidos/lista";
    }

    // ─── Detalhe ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);
        return "pedidos/detalhe";
    }

    @GetMapping("/{id}/estado")
    public String estado(@PathVariable Long id) {
        return "redirect:/pedidos/" + id;
    }

    // ─── Cancelar ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
            @RequestParam Long clienteId) {
        pedidoService.cancelarPedido(id);
        return "redirect:/pedidos?clienteId=" + clienteId;
    }

    // ─── Query 3: Média de Pedidos por Cliente por Mês ───────────────────────

    /**
     * FASE 1 — Query 3: Média de pedidos por cliente por mês
     * Página que mostra estatísticas de pedidos agrupados por cliente e período.
     */
    @GetMapping("/media-por-cliente-mes")
    public String mediaePedidosPorClientePorMes(Model model) {
        var resultados = pedidoService.mediaPedidosPorClientePorMes();
        model.addAttribute("resultados", resultados);
        model.addAttribute("titulo", "Média de Pedidos por Cliente por Mês");
        model.addAttribute("descricao", "Estatísticas de pedidos agrupados por cliente e período mensal");
        return "pedidos/media-por-cliente-mes";
    }
}
