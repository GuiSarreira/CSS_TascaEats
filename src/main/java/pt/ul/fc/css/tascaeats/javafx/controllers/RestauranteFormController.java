package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller para CRUD de Restaurantes (Requisito F)
 */
public class RestauranteFormController {

    private static final String RESTAURANTES_BASE_URL = "http://localhost:8081/api/restaurantes";
    private static final String USERS_BY_EMAIL_URL = "http://localhost:8081/api/users/email?email=";
    private static final String ADMINS_FILTER_URL = "http://localhost:8081/api/users/filtros?tipo=ADMIN";

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtNif;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtPostalCode;
    @FXML
    private TextField txtCity;
    @FXML
    private ComboBox<String> comboCuisineType;
    @FXML
    private TextField txtOpenTime; // HH:mm
    @FXML
    private TextField txtCloseTime; // HH:mm
    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblStatus;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Integer restauranteId;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("[RestauranteFormController] Inicializando...");
        comboCuisineType.getItems().addAll(
                "Portuguesa", "Italiana", "Chinesa", "Japonesa",
                "Mexicana", "Indiana", "Tailandesa", "Americana", "Outra");
        comboCuisineType.setValue("Portuguesa");
    }

    public void setMode(RestauranteTableItem restaurante) {
        if (restaurante != null) {
            this.restauranteId = restaurante.getId();
            txtName.setText(restaurante.getName());
            txtAddress.setText(restaurante.getAddress());
            txtCity.setText(restaurante.getCity());
            lblStatus.setText("A editar restaurante: " + restaurante.getName());
        } else {
            lblStatus.setText("Novo restaurante");
        }
    }

    @FXML
    private void save() {
        String userType = AuthenticationService.getInstance().getUserType();
        if (!"ADMIN".equalsIgnoreCase(userType)) {
            lblStatus.setText("Apenas ADMINs podem criar ou editar restaurantes");
            return;
        }

        String name = txtName.getText();
        String nif = txtNif.getText();
        String address = txtAddress.getText();
        String postalCode = txtPostalCode.getText();
        String city = txtCity.getText();
        String openTimeRaw = txtOpenTime.getText();
        String closeTimeRaw = txtCloseTime.getText();

        if (name.isBlank() || address.isBlank() || city.isBlank()) {
            lblStatus.setText("Preencha todos os campos obrigatorios");
            return;
        }

        if (restauranteId == null && nif.isBlank()) {
            lblStatus.setText("NIF e obrigatorio para criar restaurante");
            return;
        }

        if (restauranteId == null && postalCode.isBlank()) {
            lblStatus.setText("Codigo postal e obrigatorio para criar restaurante");
            return;
        }

        String openTimeNormalized = normalizeTime(openTimeRaw, "09:00", "hora de abertura");
        if (openTimeNormalized == null) {
            return;
        }

        String closeTimeNormalized = normalizeTime(closeTimeRaw, "23:00", "hora de fecho");
        if (closeTimeNormalized == null) {
            return;
        }

        lblStatus.setText("A guardar...");
        btnSave.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                String cuisine = comboCuisineType.getValue() != null ? comboCuisineType.getValue() : "Portuguesa";
                String description = txtDescription.getText().isEmpty() ? "" : txtDescription.getText();
                Long adminId = resolveCurrentAdminId();
                String postalCodeToSend = (postalCode == null || postalCode.isBlank()) ? "0000-000" : postalCode.trim();

                if (adminId == null || adminId <= 0) {
                    throw new RuntimeException("Nao foi possivel identificar o admin dono");
                }

                String payload = "{" +
                        "\"nome\":\"" + escapeJson(name.trim()) + "\"," +
                        "\"nif\":\"" + escapeJson(nif.trim()) + "\"," +
                        "\"morada\":{" +
                        "\"rua\":\"" + escapeJson(address.trim()) + "\"," +
                        "\"codigoPostal\":\"" + escapeJson(postalCodeToSend) + "\"," +
                        "\"cidade\":\"" + escapeJson(city.trim()) + "\"}," +
                        "\"adminId\":" + adminId + "," +
                        "\"tipoCozinha\":\"" + escapeJson(cuisine) + "\"," +
                        "\"horarioAbertura\":\"" + escapeJson(openTimeNormalized) + "\"," +
                        "\"horarioFecho\":\"" + escapeJson(closeTimeNormalized) + "\"" +
                        "}";

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10));

                if (restauranteId == null) {
                    builder.uri(URI.create(RESTAURANTES_BASE_URL))
                            .POST(HttpRequest.BodyPublishers.ofString(payload));
                } else {
                    builder.uri(URI.create(RESTAURANTES_BASE_URL + "/" + restauranteId + "?adminId=" + adminId))
                            .PUT(HttpRequest.BodyPublishers.ofString(payload));
                }

                HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP " + response.statusCode() + " ao guardar restaurante");
                }

                Platform.runLater(() -> {
                    lblStatus.setText("Restaurante '" + name + "' guardado com sucesso!");
                    btnSave.setDisable(false);
                    fecharAposDelay();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro: " + e.getMessage());
                    btnSave.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Fechar dialog apos sucesso
     */
    private void fecharAposDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(this::cancel);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        if (stage != null) {
            stage.close();
            return;
        }
        clearFields();
    }

    private void clearFields() {
        txtName.clear();
        txtNif.clear();
        txtAddress.clear();
        txtPostalCode.clear();
        txtCity.clear();
        txtOpenTime.clear();
        txtCloseTime.clear();
        txtDescription.clear();
    }

    private Long resolveCurrentAdminId() {
        try {
            AuthenticationService auth = AuthenticationService.getInstance();
            if (auth.getCurrentUser() != null) {
                String rawId = auth.getCurrentUser().userId;
                if (rawId != null && rawId.matches("\\d+")) {
                    return Long.parseLong(rawId);
                }

                String email = auth.getCurrentUser().email;
                Long idByEmail = findUserIdByEmail(email);
                if (idByEmail != null) {
                    return idByEmail;
                }

                if ("admin@example.com".equalsIgnoreCase(email)) {
                    Long fallbackDemo = findUserIdByEmail("admin@tascaeats.pt");
                    if (fallbackDemo != null) {
                        return fallbackDemo;
                    }
                }
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ADMINS_FILTER_URL))
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray() && !root.isEmpty()) {
                    long id = root.get(0).path("id").asLong(0L);
                    if (id > 0) {
                        return id;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            String encoded = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USERS_BY_EMAIL_URL + encoded))
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                long id = root.path("id").asLong(0L);
                if (id > 0) {
                    return id;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizeTime(String raw, String defaultValue, String fieldLabel) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return defaultValue;
        }

        Pattern hourOnly = Pattern.compile("^(\\d{1,2})$");
        Matcher hourOnlyMatcher = hourOnly.matcher(value);
        if (hourOnlyMatcher.matches()) {
            int hour = Integer.parseInt(hourOnlyMatcher.group(1));
            if (hour >= 0 && hour <= 23) {
                return String.format("%02d:00", hour);
            }
            lblStatus.setText("Valor invalido para " + fieldLabel + " (00:00-23:59)");
            return null;
        }

        Pattern hourMinute = Pattern.compile("^(\\d{1,2})[:hH](\\d{1,2})$");
        Matcher hourMinuteMatcher = hourMinute.matcher(value);
        if (hourMinuteMatcher.matches()) {
            int hour = Integer.parseInt(hourMinuteMatcher.group(1));
            int minute = Integer.parseInt(hourMinuteMatcher.group(2));
            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                return String.format("%02d:%02d", hour, minute);
            }
        }

        lblStatus.setText("Formato invalido para " + fieldLabel + " (ex: 10 ou 10:30)");
        return null;
    }

    // Inner class para items da tabela
    public static class RestauranteTableItem {
        private Integer id;
        private String name;
        private String address;
        private String city;
        private String cuisineType;

        public RestauranteTableItem(Integer id, String name, String address, String city, String cuisineType) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.city = city;
            this.cuisineType = cuisineType;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getCity() {
            return city;
        }

        public String getCuisineType() {
            return cuisineType;
        }
    }
}
