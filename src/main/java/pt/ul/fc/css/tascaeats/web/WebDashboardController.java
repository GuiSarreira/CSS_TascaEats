package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ul.fc.css.tascaeats.dto.UserResponse;

@Controller
public class WebDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object rawUser = session.getAttribute("user");
        if (rawUser instanceof UserResponse user) {
            model.addAttribute("userName", user.getNome());
        }
        return "dashboard";
    }
}
