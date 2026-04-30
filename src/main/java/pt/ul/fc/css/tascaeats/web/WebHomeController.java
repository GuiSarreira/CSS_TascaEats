package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para a página inicial (home) da interface web.
 * Redireciona a raiz (/) para a listagem de restaurantes.
 */
@Controller
public class WebHomeController {

    /**
     * Redireciona a raiz para a listagem de restaurantes.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/restaurantes";
    }
}
