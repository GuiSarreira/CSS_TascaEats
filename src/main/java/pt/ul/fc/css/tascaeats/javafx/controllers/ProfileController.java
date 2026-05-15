package pt.ul.fc.css.tascaeats.javafx.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller para a vista de perfil nativa.
 */
public class ProfileController {

    private static final String USERS_BASE_URL = "http://localhost:8082/api/users";
    private static final String RESTAURANTES_BASE_URL = "http://localhost:8082/api/restaurantes";

    @FXML
    private Label lblProfileEmail;
    @FXML
    private Label lblProfileRole;
    @FXML
    private Label lblProfileUserId;
    @FXML
    private Label lblProfilePhone;
    @FXML
    private VBox adminSection;
    @FXML
    private ListView<RestaurantItem> listAdminRestaurantes;
    @FXML
    private VBox entregadorSection;
    @FXML
    private Label lblDisponibilidadeAtual;
    @FXML
    private CheckBox chkDisponivel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode currentUserDetails;

    @FXML
    public void initialize() {
        AuthenticationService authService = AuthenticationService.getInstance();
        AuthenticationService.CurrentUser currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            lblProfileEmail.setText("Sem sessão ativa");
            lblProfileRole.setText("-");
            lblProfileUserId.setText("-");
            lblProfilePhone.setText("-");
            setSectionsVisible(false, false);
            return;
        }

        loadProfile(currentUser.email, authService.getUserType(), currentUser.userId);
    }

    private void loadProfile(String email, String fallbackRole, String fallbackUserId) {
        try {
            JsonNode byEmail = getJson(USERS_BASE_URL + "/email?email=" + encode(email));
            currentUserDetails = byEmail;

            String role = byEmail.path("role").asText(fallbackRole);
            String userId = byEmail.path("id").asText(fallbackUserId);

            lblProfileEmail.setText(byEmail.path("email").asText(email));
            lblProfileRole.setText(role);
            lblProfileUserId.setText(userId);
            lblProfilePhone.setText(byEmail.path("telemovel").asText("-"));

            if ("ADMIN".equalsIgnoreCase(role)) {
                setSectionsVisible(true, false);
                carregarRestaurantesAdmin();
            } else if ("ENTREGADOR".equalsIgnoreCase(role)) {
                setSectionsVisible(false, true);
                boolean disponivel = byEmail.path("disponivel").asBoolean(false);
                chkDisponivel.setSelected(disponivel);
                lblDisponibilidadeAtual.setText("Estado atual: " + (disponivel ? "Disponível" : "Indisponível"));
            } else {
                setSectionsVisible(false, false);
            }
        } catch (Exception ex) {
            lblProfileEmail.setText(email);
            lblProfileRole.setText(fallbackRole);
            lblProfileUserId.setText(fallbackUserId);
            lblProfilePhone.setText("-");
            setSectionsVisible(false, false);
        }
    }

    private void setSectionsVisible(boolean adminVisible, boolean entregadorVisible) {
        adminSection.setVisible(adminVisible);
        adminSection.setManaged(adminVisible);
        entregadorSection.setVisible(entregadorVisible);
        entregadorSection.setManaged(entregadorVisible);
    }

    @FXML
    public void recarregarRestaurantes() {
        carregarRestaurantesAdmin();
    }

    private void carregarRestaurantesAdmin() {
        if (currentUserDetails == null) {
            return;
        }

        long adminId = currentUserDetails.path("id").asLong(-1);
        if (adminId <= 0) {
            return;
        }

        List<RestaurantItem> restaurantes = new ArrayList<>();
        try {
            JsonNode response = getJson(RESTAURANTES_BASE_URL);
            if (response.isArray()) {
                for (JsonNode restaurante : response) {
                    if (restaurante.path("adminId").asLong(-1) == adminId) {
                        JsonNode morada = restaurante.path("morada");
                        restaurantes.add(new RestaurantItem(
                                restaurante.path("id").asLong(),
                                restaurante.path("nome").asText(""),
                                restaurante.path("nif").asText(""),
                                morada.path("rua").asText(""),
                                morada.path("codigoPostal").asText(""),
                                morada.path("cidade").asText("")));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        listAdminRestaurantes.getItems().setAll(restaurantes);
    }

    @FXML
    public void atualizarRestauranteSelecionado() {
        RestaurantItem selected = listAdminRestaurantes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Seleciona um restaurante primeiro.");
            info.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Atualizar Restaurante");
        dialog.setHeaderText("Atualizar " + selected.getNome());

        ButtonType saveButton = new ButtonType("Guardar", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField txtNome = new TextField(selected.getNome());
        TextField txtRua = new TextField(selected.getRua());
        TextField txtCodigoPostal = new TextField(selected.getCodigoPostal());
        TextField txtCidade = new TextField(selected.getCidade());

        VBox content = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Rua:"), txtRua,
                new Label("Código Postal:"), txtCodigoPostal,
                new Label("Cidade:"), txtCidade);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        long adminId = currentUserDetails.path("id").asLong(-1);
        if (adminId <= 0) {
            return;
        }

        try {
            JsonNode payload = objectMapper.createObjectNode()
                    .put("nome", txtNome.getText().trim())
                    .put("nif", selected.getNif())
                    .set("morada", objectMapper.createObjectNode()
                            .put("rua", txtRua.getText().trim())
                            .put("codigoPostal", txtCodigoPostal.getText().trim())
                            .put("cidade", txtCidade.getText().trim()));

            String url = RESTAURANTES_BASE_URL + "/" + selected.getId() + "?adminId=" + adminId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP " + response.statusCode() + " ao atualizar restaurante");
            }

            carregarRestaurantesAdmin();
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Erro ao atualizar restaurante: " + ex.getMessage());
            error.showAndWait();
        }
    }

    @FXML
    public void removerRestauranteSelecionado() {
        RestaurantItem selected = listAdminRestaurantes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Seleciona um restaurante primeiro.");
            info.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover o restaurante \"" + selected.getNome() + "\"?");
        Optional<ButtonType> confirmed = confirm.showAndWait();
        if (confirmed.isEmpty() || confirmed.get() != ButtonType.OK) {
            return;
        }

        long adminId = currentUserDetails.path("id").asLong(-1);
        if (adminId <= 0) {
            return;
        }

        try {
            String url = RESTAURANTES_BASE_URL + "/" + selected.getId() + "?adminId=" + adminId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP " + response.statusCode() + " ao remover restaurante");
            }

            carregarRestaurantesAdmin();
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Erro ao remover restaurante: " + ex.getMessage());
            error.showAndWait();
        }
    }

    @FXML
    public void guardarDisponibilidade() {
        if (currentUserDetails == null) {
            return;
        }

        long userId = currentUserDetails.path("id").asLong(-1);
        if (userId <= 0) {
            return;
        }

        boolean novoEstado = chkDisponivel.isSelected();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USERS_BASE_URL + "/" + userId + "/disponibilidade-entregador?disponivel=" +
                            novoEstado))
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(6))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP " + response.statusCode() + " ao atualizar disponibilidade");
            }

            lblDisponibilidadeAtual.setText("Estado atual: " + (novoEstado ? "Disponível" : "Indisponível"));
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Erro ao guardar disponibilidade: " + ex.getMessage());
            error.showAndWait();
        }
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(6))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " para " + url);
        }
        return objectMapper.readTree(response.body());
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static class RestaurantItem {
        private final Long id;
        private final String nome;
        private final String nif;
        private final String rua;
        private final String codigoPostal;
        private final String cidade;

        public RestaurantItem(Long id, String nome, String nif, String rua, String codigoPostal, String cidade) {
            this.id = id;
            this.nome = nome;
            this.nif = nif;
            this.rua = rua;
            this.codigoPostal = codigoPostal;
            this.cidade = cidade;
        }

        public Long getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getNif() {
            return nif;
        }

        public String getRua() {
            return rua;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public String getCidade() {
            return cidade;
        }

        @Override
        public String toString() {
            return nome + " (" + cidade + ")";
        }
    }
}