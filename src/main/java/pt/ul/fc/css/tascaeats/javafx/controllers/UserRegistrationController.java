package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;

/**
 * Controller para registar novos Administradores e Entregadores (Requisito C)
 */
public class UserRegistrationController {

    @FXML
    private ComboBox<String> comboUserType;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtZoneArea; // Para entregadores

    @FXML
    private Label lblStatus;
    @FXML
    private Button btnRegister;
    @FXML
    private Button btnCancel;

    @FXML
    public void initialize() {
        System.out.println("[UserRegistrationController] Inicializando...");
        comboUserType.getItems().addAll("ADMIN", "ENTREGADOR");
        comboUserType.setValue("ADMIN");
        comboUserType.setOnAction(e -> updateFieldsForUserType());
    }

    private void updateFieldsForUserType() {
        String type = comboUserType.getValue();
        if ("ENTREGADOR".equals(type)) {
            txtZoneArea.setVisible(true);
            lblStatus.setText("Registar novo entregador");
        } else {
            txtZoneArea.setVisible(false);
            lblStatus.setText("Registar novo administrador");
        }
    }

    @FXML
    private void register() {
        String type = comboUserType.getValue();
        String name = txtName.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String zoneArea = txtZoneArea.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Preencha todos os campos obrigatorios");
            return;
        }

        lblStatus.setText("Registando " + type + "...");

        Thread thread = new Thread(() -> {
            try {
                // Conectar ao gRPC se necessario
                TascaEatsGrpcClient grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                grpcClient.connect();

                // Construir request de registo
                RegistarUserRequest.Builder requestBuilder = RegistarUserRequest.newBuilder()
                        .setNome(name)
                        .setEmail(email)
                        .setPassword(password)
                        .setTipo(type);

                if (!txtPhone.getText().trim().isEmpty()) {
                    requestBuilder.setTelemovel(txtPhone.getText().trim());
                }

                // Adicionar zona de atuacao se for entregador
                if ("ENTREGADOR".equals(type) && !zoneArea.isEmpty()) {
                    requestBuilder.setZonaAtuacao(zoneArea);
                }

                RegistarUserRequest request = requestBuilder.build();
                grpcClient.registarUser(request);

                Platform.runLater(() -> {
                    lblStatus.setText(type + " '" + name + "' registado com sucesso!");
                    clearFields();
                });

                grpcClient.disconnect();

            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro: " + e.getMessage()));
                System.err.println("[UserRegistrationController] Erro: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void cancel() {
        clearFields();
        lblStatus.setText("Pronto para novo registo");
    }

    private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtPhone.clear();
        txtZoneArea.clear();
    }
}
