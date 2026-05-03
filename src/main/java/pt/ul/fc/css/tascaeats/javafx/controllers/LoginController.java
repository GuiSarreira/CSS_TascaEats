package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.util.WindowManager;

/**
 * Controller para a tela de Login
 * Responsável por autenticar utilizadores com JWT
 */
public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnTogglePassword;
    @FXML
    private Label lblErro;

    private AuthenticationService authService;

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[LoginController] Inicializar...");
        authService = AuthenticationService.getInstance();
        lblErro.setText("");

        txtPasswordVisible.managedProperty().bind(txtPasswordVisible.visibleProperty());
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        // Demo credentials
        txtEmail.setText("admin@example.com");
        txtPassword.setText("password123");
    }

    /**
     * Fazer login com JWT
     */
    @FXML
    public void fazerLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validação básica
        if (email.isEmpty() || password.isEmpty()) {
            lblErro.setText("Por favor, preencha todos os campos");
            return;
        }

        lblErro.setText("Autenticando...");

        // Usar AuthenticationService com JWT
        if (authService.authenticate(email, password)) {
            System.out.println("[LoginController] Login bem-sucedido para: " + email);
            System.out.println("[LoginController] Token: " + authService.getToken().substring(0, 20) + "...");

            try {
                abrirPaginaPrincipal();
            } catch (Exception e) {
                lblErro.setText(
                        "Erro ao abrir aplicacao: " + (e.getMessage() != null ? e.getMessage() : "detalhes no log"));
                System.err.println("[LoginController] Erro: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            lblErro.setText("Login invalido - email incorreto ou conta cliente");
            System.err.println("[LoginController] Autenticacao falhou para: " + email);
        }
    }

    /**
     * Abrir página principal após login bem-sucedido
     */
    private void abrirPaginaPrincipal() throws Exception {
        java.net.URL fxmlUrl = resolveResource("fxml/main.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Recurso FXML nao encontrado: fxml/main.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1000, 800);

        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("TascaEats - Sistema de Gestão");
        WindowManager.configureMainStage(stage, scene);
        stage.show();
    }

    @FXML
    public void alternarVisibilidadePassword() {
        boolean mostrarTexto = !txtPasswordVisible.isVisible();
        txtPasswordVisible.setVisible(mostrarTexto);
        txtPassword.setVisible(!mostrarTexto);
        txtPassword.setManaged(!mostrarTexto);
    }

    private java.net.URL resolveResource(String path) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            java.net.URL url = contextClassLoader.getResource(path);
            if (url != null) {
                return url;
            }
        }

        java.net.URL url = LoginController.class.getResource("/" + path);
        if (url != null) {
            return url;
        }

        url = LoginController.class.getClassLoader().getResource(path);
        if (url != null) {
            return url;
        }

        java.nio.file.Path fromTarget = java.nio.file.Paths.get("target", "classes", path);
        if (java.nio.file.Files.exists(fromTarget)) {
            try {
                return fromTarget.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        java.nio.file.Path fromSource = java.nio.file.Paths.get("src", "main", "resources", path);
        if (java.nio.file.Files.exists(fromSource)) {
            try {
                return fromSource.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        return null;
    }

    /**
     * Abrir formulário de registo
     */
    @FXML
    public void abrirRegisto() {
        try {
            java.net.URL fxmlUrl = resolveResource("fxml/userRegistration.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Recurso FXML nao encontrado: fxml/userRegistration.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 760, 620);

            Stage dialog = new Stage();
            dialog.initOwner((Stage) txtEmail.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("TascaEats - Registo");
            dialog.setScene(scene);
            WindowManager.configureDialogStage(dialog);
            dialog.showAndWait();
        } catch (Exception e) {
            lblErro.setText("Erro ao abrir registo");
            System.err.println("[LoginController] Erro ao abrir registo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obter utilizador autenticado (para compatibilidade)
     */
    public static String getUsuarioAutenticado() {
        AuthenticationService authService = AuthenticationService.getInstance();
        if (authService.isAuthenticated()) {
            return authService.getCurrentUser().email;
        }
        return null;
    }
}
