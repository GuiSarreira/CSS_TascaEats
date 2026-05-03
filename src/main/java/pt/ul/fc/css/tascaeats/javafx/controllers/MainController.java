package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.util.WindowManager;

import java.net.URL;

/**
 * Controller principal da aplicação TascaEats
 * Responsável pela navegação entre vistas principais
 */
public class MainController {

    @FXML
    private BorderPane root;
    @FXML
    private StackPane contentContainer;
    @FXML
    private ScrollPane dashboardScroll;
    @FXML
    private VBox dashboardView;
    @FXML
    private Label lblHeaderUser;
    @FXML
    private Label lblHeaderRole;
    @FXML
    private Button btnPerfil;
    @FXML
    private Label lblWelcomeTitle;
    @FXML
    private Label lblWelcomeSubtitle;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ENTREGADOR = "ENTREGADOR";

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[MainController] Inicializando...");
        atualizarDashboard();
        abrirDashboard();
        if (root != null) {
            root.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    WindowManager.configureMainStage(getStage(), newScene);
                }
            });
        }
    }

    @FXML
    public void abrirDashboard() {
        atualizarDashboard();
        contentContainer.getChildren().setAll(dashboardScroll);
    }

    /**
     * Abrir vista de Restaurantes
     */
    @FXML
    public void abrirRestaurantes() {
        carregarVista("/fxml/restaurantes.fxml");
    }

    /**
     * Abrir vista de Gestão de Restaurantes
     */
    @FXML
    public void abrirRestauranteForm() {
        carregarVista("/fxml/restauranteForm.fxml");
    }

    /**
     * Abrir vista de Menus
     */
    @FXML
    public void abrirMenus() {
        carregarVista("/fxml/menus.fxml");
    }

    /**
     * Novo menu
     */
    @FXML
    public void novoMenu() {
        carregarVista("/fxml/menuForm.fxml");
    }

    /**
     * Abrir vista de Produtos
     */
    @FXML
    public void abrirProdutos() {
        carregarVista("/fxml/product.fxml");
    }

    /**
     * Novo produto
     */
    @FXML
    public void novoProduto() {
        carregarVista("/fxml/productForm.fxml");
    }

    /**
     * Abrir vista de Pedidos
     */
    @FXML
    public void abrirPedidos() {
        carregarVista("/fxml/pedidos.fxml");
    }

    /**
     * Novo pedido
     */
    @FXML
    public void novoPedido() {
        carregarVista("/fxml/pedidos.fxml");
    }

    /**
     * Abrir vista de Pagamentos
     */
    @FXML
    public void abrirPagamentos() {
        carregarVista("/fxml/pagamentos.fxml");
    }

    /**
     * Novo pagamento
     */
    @FXML
    public void novoPagamento() {
        carregarVista("/fxml/pagamentos.fxml");
    }

    /**
     * Abrir vista de Entregas
     */
    @FXML
    public void abrirEntregas() {
        carregarVista("/fxml/entregas.fxml");
    }

    /**
     * Rastrear entrega
     */
    @FXML
    public void rastrearEntrega() {
        carregarVista("/fxml/entregas.fxml");
    }

    /**
     * Abrir vista de Gestão de Utilizadores
     */
    @FXML
    public void abrirUtilizadores() {
        carregarVista("/fxml/userManagement.fxml");
    }

    /**
     * Novo utilizador
     */
    @FXML
    public void novoUtilizador() {
        carregarVista("/fxml/userRegistration.fxml");
    }

    /**
     * Abrir vista de Avaliações
     */
    @FXML
    public void abrirAvaliacoes() {
        carregarVista("/fxml/avaliacoes.fxml");
    }

    /**
     * Abrir perfil
     */
    @FXML
    public void abrirPerfil() {
        carregarVista("/fxml/profile.fxml");
    }

    @FXML
    public void alternarMaximizado() {
        Stage stage = getStage();
        if (stage != null) {
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML
    public void alternarFullscreen() {
        Stage stage = getStage();
        if (stage != null) {
            stage.setFullScreenExitHint("Pressione ESC ou F11 para sair do ecrã inteiro");
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    /**
     * Abrir definições
     */
    @FXML
    public void abrirDefinicoes() {
        System.out.println("[MainController] Abrindo definicoes...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Definições");
        alert.setHeaderText("Configurações da Aplicação");
        alert.setContentText("Funcionalidade em desenvolvimento...");
        alert.showAndWait();
    }

    /**
     * Logout
     */
    @FXML
    public void logout() {
        System.out.println("[MainController] Logout...");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Confirmar logout?");
        confirm.setContentText("Tem a certeza que deseja sair?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            AuthenticationService.getInstance().logout();
            voltarParaLogin();
        }
    }

    /**
     * Abrir vista Sobre
     */
    @FXML
    public void abrirSobre() {
        System.out.println("[MainController] Abrindo sobre...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("TascaEats v1.2");
        alert.setContentText("Aplicação de entrega de comida.\nDesenvolvido com JavaFX e gRPC.");
        alert.showAndWait();
    }

    /**
     * Sair aplicação
     */
    @FXML
    public void sair() {
        System.out.println("[MainController] Saindo...");
        System.exit(0);
    }

    /**
     * Abrir nova avaliação
     */
    @FXML
    public void novaAvaliacao() {
        carregarVista("/fxml/avaliacoes.fxml");
    }

    /**
     * Carregar vista FXML dinamicamente
     */
    private void carregarVista(String fxmlPath) {
        try {
            URL fxmlUrl = resolveResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("Recurso FXML não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            contentContainer.getChildren().setAll(view);
            System.out.println("[MainController] Vista carregada: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("[MainController] Erro ao carregar vista: " + fxmlPath);
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de navegação");
            alert.setHeaderText("Não foi possível abrir a vista");
            alert.setContentText(fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    private void atualizarDashboard() {
        AuthenticationService authService = AuthenticationService.getInstance();
        AuthenticationService.CurrentUser currentUser = authService.getCurrentUser();

        String email = currentUser != null ? currentUser.email : "guest@tascaeats.pt";
        String nome = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String role = authService.getUserType();

        lblHeaderUser.setText(email);
        lblHeaderRole.setText(role);
        if (btnPerfil != null) {
            btnPerfil.setText(nome);
        }
        lblWelcomeTitle.setText("Bem-vindo, " + nome);
        if (ROLE_ADMIN.equals(role) || ROLE_ENTREGADOR.equals(role)) {
            lblWelcomeSubtitle.setText(
                    "Escolhe para onde queres ir e gere a operação a partir daqui. As avaliações aparecem em modo de consulta na interface nativa.");
        } else {
            lblWelcomeSubtitle.setText("Escolhe para onde queres ir e gere a operação a partir daqui.");
        }
    }

    private void voltarParaLogin() {
        try {
            URL fxmlUrl = resolveResource("/fxml/login.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Recurso FXML não encontrado: /fxml/login.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 500, 600);

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TascaEats - Login");
            WindowManager.configureLoginStage(stage, scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("[MainController] Erro ao voltar para login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private URL resolveResource(String path) {
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            URL url = contextClassLoader.getResource(normalizedPath);
            if (url != null) {
                return url;
            }
        }

        URL url = MainController.class.getResource(path.startsWith("/") ? path : "/" + path);
        if (url != null) {
            return url;
        }

        url = MainController.class.getClassLoader().getResource(normalizedPath);
        if (url != null) {
            return url;
        }

        java.nio.file.Path fromTarget = java.nio.file.Paths.get("target", "classes", normalizedPath);
        if (java.nio.file.Files.exists(fromTarget)) {
            try {
                return fromTarget.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        java.nio.file.Path fromSource = java.nio.file.Paths.get("src", "main", "resources", normalizedPath);
        if (java.nio.file.Files.exists(fromSource)) {
            try {
                return fromSource.toUri().toURL();
            } catch (java.net.MalformedURLException ignored) {
            }
        }

        return null;
    }

    private Stage getStage() {
        Scene scene = root != null ? root.getScene() : null;
        return scene != null && scene.getWindow() instanceof Stage ? (Stage) scene.getWindow() : null;
    }
}
