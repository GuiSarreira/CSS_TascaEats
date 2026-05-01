package pt.ul.fc.css.tascaeats.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Cliente;
import pt.ul.fc.css.tascaeats.entities.Endereco;
import pt.ul.fc.css.tascaeats.services.PedidoService;
import pt.ul.fc.css.tascaeats.services.UserService;

import java.time.LocalDateTime;

/**
 * Controller MVC (Thymeleaf) para gestão das moradas do cliente.
 *
 * Permite ao cliente listar, adicionar e remover as suas moradas guardadas.
 * Estas moradas podem ser usadas como endereço de entrega ao criar um pedido.
 */
@Controller
@RequestMapping("/cliente")
public class WebClienteController {

    private final UserService userService;
    private final PedidoService pedidoService;

    public WebClienteController(UserService userService, PedidoService pedidoService) {
        this.userService = userService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{clienteId}/moradas")
    public String listarMoradas(@PathVariable Long clienteId, Model model) {
        Cliente cliente = userService.buscarClientePorId(clienteId);
        model.addAttribute("cliente", cliente);
        model.addAttribute("moradas", cliente.getMoradas());
        return "cliente/moradas";
    }

    @PostMapping("/{clienteId}/moradas")
    public String adicionarMorada(@PathVariable Long clienteId,
            @RequestParam String rua,
            @RequestParam String codigoPostal,
            @RequestParam String cidade) {
        userService.adicionarMorada(clienteId, new Endereco(rua, codigoPostal, cidade));
        return "redirect:/cliente/" + clienteId + "/moradas";
    }

    @PostMapping("/{clienteId}/moradas/{index}/remover")
    public String removerMorada(@PathVariable Long clienteId,
            @PathVariable int index) {
        userService.removerMorada(clienteId, index);
        return "redirect:/cliente/" + clienteId + "/moradas";
    }

    // ─── Query 5: Cliente com Mais Pedidos num Intervalo ─────────────────────

    /**
     * Formulário para consultar o cliente com mais pedidos num intervalo de tempo.
     * Query de negócio: "Qual é o cliente que mais pedidos fez num intervalo de tempo?"
     */
    @GetMapping("/mais-pedidos")
    public String clienteMaisPedidosForm(Model model) {
        model.addAttribute("titulo", "Cliente com Mais Pedidos");
        model.addAttribute("descricao", "Qual é o cliente que mais pedidos fez num intervalo de tempo?");
        return "cliente/mais-pedidos";
    }

    /**
     * Resultado — cliente com mais pedidos num intervalo.
     */
    @PostMapping("/mais-pedidos")
    public String clienteMaisPedidosResultado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Model model) {
        var resultado = pedidoService.clienteComMaisPedidosNoIntervalo(dataInicio, dataFim);

        if (resultado.isPresent()) {
            Object[] dados = resultado.get();
            model.addAttribute("cliente", dados[0]);
            model.addAttribute("totalPedidos", dados[1]);
            model.addAttribute("encontrado", true);
        } else {
            model.addAttribute("encontrado", false);
            model.addAttribute("mensagem", "Sem pedidos neste período");
        }

        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("titulo", "Cliente com Mais Pedidos");
        return "cliente/mais-pedidos-resultado";
    }

    // ─── Query 6: Clientes Sem Compras ──────────────────────────────────────

    /**
     * FASE 1 — Query 6: Clientes registados sem compras
     * Página que lista todos os clientes que ainda não realizaram qualquer pedido.
     */
    @GetMapping("/sem-compras")
    public String clientesSemCompras(Model model) {
        var clientes = userService.buscarClientesSemCompras();
        model.addAttribute("clientes", clientes);
        model.addAttribute("titulo", "Clientes Sem Compras");
        model.addAttribute("descricao", "Clientes registados que ainda não realizaram qualquer pedido");
        return "cliente/sem-compras";
    }
}
