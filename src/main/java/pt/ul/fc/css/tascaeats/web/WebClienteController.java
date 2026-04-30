package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.entities.Cliente;
import pt.ul.fc.css.tascaeats.entities.Endereco;
import pt.ul.fc.css.tascaeats.services.UserService;

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

    public WebClienteController(UserService userService) {
        this.userService = userService;
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
}
