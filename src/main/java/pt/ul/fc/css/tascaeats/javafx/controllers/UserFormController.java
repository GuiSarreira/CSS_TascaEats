package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * Controller para dialog de criar/editar utilizadores
 */
public class UserFormController {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private CheckBox chkAtivo;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblErro;

    private Stage stage;
    private Integer userIdEdicao = null;
    private Runnable callbackRefresh;

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[UserFormController] Inicializando...");
        lblErro.setText("");
        chkAtivo.setSelected(true);

        // Preencher combo de tipos de utilizador
        ObservableList<String> tipos = FXCollections.observableArrayList("CLIENTE", "ADMIN", "ENTREGADOR");
        cmbTipo.setItems(tipos);
        cmbTipo.setValue("CLIENTE");
    }

    /**
     * Configurar para modo CRIAR
     */
    public void configurarCriar(Runnable refresh) {
        this.callbackRefresh = refresh;
        this.userIdEdicao = null;

        lblTitulo.setText("Novo Utilizador");
        txtPassword.setVisible(true);
    }

    /**
     * Configurar para modo EDITAR
     */
    public void configurarEditar(int userId, String nome,
            String email, String tipo, boolean ativo, Runnable refresh) {
        this.callbackRefresh = refresh;
        this.userIdEdicao = userId;

        lblTitulo.setText("Editar Utilizador");
        txtNome.setText(nome);
        txtEmail.setText(email);
        cmbTipo.setValue(tipo);
        chkAtivo.setSelected(ativo);
        txtPassword.setVisible(false); // Não permitir mudar password na edição
    }

    /**
     * Guardar utilizador (criar ou editar)
     */
    @FXML
    public void guardarUser() {
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validação
        if (nome.isEmpty()) {
            lblErro.setText("Nome é obrigatório");
            return;
        }

        if (email.isEmpty()) {
            lblErro.setText("Email é obrigatório");
            return;
        }

        if (userIdEdicao == null && password.isEmpty()) {
            lblErro.setText("Password é obrigatória para novo utilizador");
            return;
        }

        btnGuardar.setDisable(true);
        lblErro.setText("Guardando...");

        if (userIdEdicao == null) {
            // Criar novo utilizador - por enquanto apenas mock
            Platform.runLater(() -> {
                lblErro.setText("Utilizador '" + nome + "' criado com sucesso!");
                fecharAposDelay();
            });
        } else {
            // Editar utilizador existente - por enquanto apenas mock
            Platform.runLater(() -> {
                lblErro.setText("Utilizador '" + nome + "' atualizado com sucesso!");
                fecharAposDelay();
            });
        }
    }

    /**
     * Fechar dialog após 1 segundo
     */
    private void fecharAposDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    if (callbackRefresh != null) {
                        callbackRefresh.run();
                    }
                    cancelar();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Cancelar dialog
     */
    @FXML
    public void cancelar() {
        stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
