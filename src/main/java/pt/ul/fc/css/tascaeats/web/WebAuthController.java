// pt.ul.fc.css.tascaeats.web.WebAuthController
package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.LoginRequest;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.services.AuthService;

@Controller
@RequestMapping("/")
public class WebAuthController {

    private final AuthService authService;

    public WebAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            UserResponse user = UserResponse
                    .from(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
            session.setAttribute("user", user);
            return "redirect:/dashboard";
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
