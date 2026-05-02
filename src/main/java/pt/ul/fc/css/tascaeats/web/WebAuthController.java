// pt.ul.fc.css.tascaeats.web.WebAuthController
package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pt.ul.fc.css.tascaeats.dto.LoginRequest;
import pt.ul.fc.css.tascaeats.dto.UserResponse;

@Controller
@RequestMapping("/")
public class WebAuthController {

    private final RestTemplate restTemplate;
    private final String API_BASE = "http://localhost:8080/api";

    public WebAuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            UserResponse user = restTemplate.postForObject(
                    API_BASE + "/auth/login", loginRequest, UserResponse.class);
            session.setAttribute("user", user);
            return "redirect:/restaurantes";
        } catch (Exception e) {
            model.addAttribute("error", "Credenciais inválidas ou utilizador inativo");
            model.addAttribute("loginRequest", loginRequest);
            return "login";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

