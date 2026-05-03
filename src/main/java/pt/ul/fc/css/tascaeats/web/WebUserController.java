package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.services.UserService;

@Controller
@RequestMapping("/users")
public class WebUserController {

    private final UserService userService;

    public WebUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer minPedidos,
            @RequestParam(required = false) Integer minEntregas,
            Model model) {

        var utilizadores = userService.filtrarUtilizadores(nome, tipo, minPedidos, minEntregas);

        model.addAttribute("utilizadores", utilizadores);
        model.addAttribute("filtroNome", nome);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroMinPedidos", minPedidos);
        model.addAttribute("filtroMinEntregas", minEntregas);

        return "users/index";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        var utilizador = userService.buscarPorId(id);
        model.addAttribute("utilizador", utilizador);
        return "users/detalhe";
    }

    @GetMapping("/me")
    public String meuPerfil(HttpSession session, Model model) {
        Object rawUser = session.getAttribute("user");
        if (!(rawUser instanceof UserResponse userSession)) {
            return "redirect:/login";
        }

        var utilizador = userService.buscarPorId(userSession.getId());
        model.addAttribute("utilizador", utilizador);
        return "users/detalhe";
    }
}
