package pt.ul.fc.css.tascaeats.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.entities.Endereco;
import pt.ul.fc.css.tascaeats.services.UserService;

@Controller
@RequestMapping
public class WebRegisterController {

    private final UserService userService;

    public WebRegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/register", "/users/registar/cliente"})
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterClienteForm());
        return "register";
    }

    @PostMapping({"/register", "/users/registar/cliente"})
    public String register(
            @ModelAttribute RegisterClienteForm registerRequest,
            HttpSession session,
            Model model) {

        if (registerRequest.getNome() == null || registerRequest.getNome().isBlank()
                || registerRequest.getEmail() == null || registerRequest.getEmail().isBlank()
                || registerRequest.getPassword() == null || registerRequest.getPassword().isBlank()
                || registerRequest.getConfirmPassword() == null || registerRequest.getConfirmPassword().isBlank()
                || registerRequest.getRua() == null || registerRequest.getRua().isBlank()
                || registerRequest.getCodigoPostal() == null || registerRequest.getCodigoPostal().isBlank()
                || registerRequest.getCidade() == null || registerRequest.getCidade().isBlank()) {
            model.addAttribute("error", "Preenche todos os campos obrigatorios.");
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "As palavras-passe nao coincidem.");
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }

        try {
            var cliente = userService.registarCliente(
                    registerRequest.getEmail().trim(),
                    registerRequest.getNome().trim(),
                    registerRequest.getPassword(),
                    new Endereco(
                            registerRequest.getRua().trim(),
                            registerRequest.getCodigoPostal().trim(),
                            registerRequest.getCidade().trim()));

            session.setAttribute("user", UserResponse.from(cliente));
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Nao foi possivel registar.");
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }
    }

    public static class RegisterClienteForm {
        private String nome;
        private String email;
        private String password;
        private String confirmPassword;
        private String rua;
        private String codigoPostal;
        private String cidade;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getRua() {
            return rua;
        }

        public void setRua(String rua) {
            this.rua = rua;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public void setCodigoPostal(String codigoPostal) {
            this.codigoPostal = codigoPostal;
        }

        public String getCidade() {
            return cidade;
        }

        public void setCidade(String cidade) {
            this.cidade = cidade;
        }
    }
}
