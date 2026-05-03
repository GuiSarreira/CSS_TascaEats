package pt.ul.fc.css.tascaeats.javafx.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar utilizadores (Requisito D)
 * - Listar utilizadores com filtro por tipo
 * - Ver detalhes
 * - Editar utilizador
 * - Remover utilizador
 */
public class UserManagementController {

    private static final String USERS_BASE_URL = "http://localhost:8081/api/users";
    private static final String RESTAURANTES_BASE_URL = "http://localhost:8081/api/restaurantes";

    @FXML
    private ComboBox<String> comboUserType;
    @FXML
    private TextField txtFilterName;
    @FXML
    private TextField txtMinPedidos;
    @FXML
    private TextField txtMinEntregas;
    @FXML
    private TableView<UserTableItem> tblUsers;
    @FXML
    private TableColumn<UserTableItem, String> colUserName;
    @FXML
    private TableColumn<UserTableItem, String> colUserType;
    @FXML
    private TableColumn<UserTableItem, String> colUserEmail;

    @FXML
    private Label lblStatus;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("[UserManagementController] Inicializando...");

        // Setup combobox
        ObservableList<String> tipos = FXCollections.observableArrayList("Todos", "CLIENTE", "ADMIN", "ENTREGADOR");
        comboUserType.setItems(tipos);
        comboUserType.setValue("Todos");

        // Setup table columns
        setupTableColumns();
        setupRowActions();
        searchUsers();
    }

    private void setupTableColumns() {
        tblUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colUserName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUserType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void setupRowActions() {
        tblUsers.setRowFactory(table -> {
            TableRow<UserTableItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    showUserActions(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void loadUsers(String nameFilter, String typeFilter, Integer minPedidos, Integer minEntregas) {
        Thread thread = new Thread(() -> {
            try {
                String url = buildFilterUrl(nameFilter, typeFilter, minPedidos, minEntregas);
                HttpResponse<String> response = sendGet(url);

                // Compatibilidade: alguns runtimes antigos não expõem /api/users/filtros.
                if (response.statusCode() == 404) {
                    String fallbackUrl = USERS_BASE_URL;
                    response = sendGet(fallbackUrl);
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        HttpResponse<String> finalResponse = response;
                        Platform.runLater(() -> lblStatus.setText(
                                "Aviso: backend sem /api/users/filtros; a aplicar filtros básicos."));
                        response = finalResponse;
                    }
                }

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP " + response.statusCode() + " ao listar utilizadores");
                }

                ObservableList<UserTableItem> users = FXCollections.observableArrayList();
                JsonNode root = objectMapper.readTree(response.body());
                for (JsonNode userNode : root) {
                    String role = userNode.path("role").asText("");
                    String nome = userNode.path("nome").asText("");
                    String email = userNode.path("email").asText("");
                    long userId = userNode.path("id").asLong(0L);

                    if (!matchesBasicFilters(nome, role, nameFilter, typeFilter)) {
                        continue;
                    }

                    if (!matchesAdvancedFilters(userId, role, minPedidos, minEntregas)) {
                        continue;
                    }

                    users.add(new UserTableItem(
                            userId,
                            nome,
                            role,
                            email,
                            userNode.path("ativo").asBoolean(true)));
                }

                Platform.runLater(() -> {
                    tblUsers.setItems(users);
                    lblStatus.setText(users.size() + " utilizadores carregados");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private HttpResponse<String> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(6))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean matchesBasicFilters(String nome, String role, String nameFilter, String typeFilter) {
        String normalizedNameFilter = nameFilter == null ? "" : nameFilter.trim().toLowerCase();
        String normalizedTypeFilter = typeFilter == null ? "Todos" : typeFilter.trim();

        boolean nameOk = normalizedNameFilter.isBlank() || nome.toLowerCase().contains(normalizedNameFilter);
        boolean typeOk = "Todos".equalsIgnoreCase(normalizedTypeFilter)
                || normalizedTypeFilter.equalsIgnoreCase(role);

        return nameOk && typeOk;
    }

    private boolean matchesAdvancedFilters(long userId, String role, Integer minPedidos, Integer minEntregas) {
        try {
            if (minPedidos != null) {
                if (!"CLIENTE".equalsIgnoreCase(role)) {
                    return false;
                }
                int pedidosCount = loadArrayCountFromUserDetails(userId, "pedidos");
                if (pedidosCount < minPedidos) {
                    return false;
                }
            }

            if (minEntregas != null) {
                if (!"ENTREGADOR".equalsIgnoreCase(role)) {
                    return false;
                }
                int entregasCount = loadArrayCountFromUserDetails(userId, "entregas");
                if (entregasCount < minEntregas) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int loadArrayCountFromUserDetails(long userId, String arrayFieldName) throws Exception {
        JsonNode details = getJson(USERS_BASE_URL + "/" + userId);
        JsonNode arrayNode = details.path(arrayFieldName);
        if (arrayNode.isArray()) {
            return arrayNode.size();
        }
        return 0;
    }

    @FXML
    private void searchUsers() {
        String name = txtFilterName.getText().trim();
        String type = comboUserType.getValue();
        Integer minPedidos = parseOptionalPositiveInteger(txtMinPedidos.getText(), "mínimo de pedidos");
        if (minPedidos == null && txtMinPedidos.getText() != null && !txtMinPedidos.getText().trim().isEmpty()) {
            return;
        }

        Integer minEntregas = parseOptionalPositiveInteger(txtMinEntregas.getText(), "mínimo de entregas");
        if (minEntregas == null && txtMinEntregas.getText() != null && !txtMinEntregas.getText().trim().isEmpty()) {
            return;
        }

        if (("Todos".equalsIgnoreCase(type) || type == null || type.isBlank()) && minPedidos != null
                && minEntregas == null) {
            type = "CLIENTE";
        } else if (("Todos".equalsIgnoreCase(type) || type == null || type.isBlank()) && minEntregas != null
                && minPedidos == null) {
            type = "ENTREGADOR";
        }

        if ("ADMIN".equalsIgnoreCase(type) && (minPedidos != null || minEntregas != null)) {
            lblStatus.setText("Filtro de pedidos/entregas nao se aplica a ADMIN");
            return;
        }

        lblStatus.setText("A filtrar utilizadores...");
        loadUsers(name, type, minPedidos, minEntregas);
    }

    private Integer parseOptionalPositiveInteger(String rawValue, String fieldLabel) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            if (value < 0) {
                lblStatus.setText("O " + fieldLabel + " deve ser maior ou igual a 0");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            lblStatus.setText("Valor inválido para " + fieldLabel);
            return null;
        }
    }

    private String buildFilterUrl(String nome, String tipo, Integer minPedidos, Integer minEntregas) {
        StringBuilder sb = new StringBuilder(USERS_BASE_URL).append("/filtros");
        List<String> params = new ArrayList<>();

        if (nome != null && !nome.isBlank()) {
            params.add("nome=" + encode(nome));
        }
        if (tipo != null && !tipo.isBlank() && !"Todos".equalsIgnoreCase(tipo)) {
            params.add("tipo=" + encode(tipo));
        }
        if (minPedidos != null) {
            params.add("minPedidos=" + minPedidos);
        }
        if (minEntregas != null) {
            params.add("minEntregas=" + minEntregas);
        }

        if (!params.isEmpty()) {
            sb.append("?").append(String.join("&", params));
        }
        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void showUserActions(UserTableItem user) {
        boolean canUpdate = canUpdateUser(user);
        boolean canRemove = canRemoveUser(user);

        Alert actions = new Alert(Alert.AlertType.CONFIRMATION);
        actions.setTitle("Ações do Utilizador");
        actions.setHeaderText(user.getName());
        actions.setContentText("Escolha uma ação para este utilizador.");

        ButtonType btnVerificar = new ButtonType("Verificar", ButtonBar.ButtonData.LEFT);
        ButtonType btnAtualizar = new ButtonType("Atualizar", ButtonBar.ButtonData.LEFT);
        ButtonType btnRemover = new ButtonType("Remover", ButtonBar.ButtonData.LEFT);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        List<ButtonType> availableButtons = new ArrayList<>();
        availableButtons.add(btnVerificar);
        if (canUpdate) {
            availableButtons.add(btnAtualizar);
        }
        if (canRemove) {
            availableButtons.add(btnRemover);
        }
        availableButtons.add(btnCancelar);
        actions.getButtonTypes().setAll(availableButtons);

        Optional<ButtonType> result = actions.showAndWait();
        if (result.isEmpty() || result.get() == btnCancelar) {
            return;
        }

        if (result.get() == btnVerificar) {
            showUserDetails(user);
            return;
        }

        if (result.get() == btnAtualizar) {
            openUpdateDialog(user);
            return;
        }

        if (result.get() == btnRemover) {
            removeUser(user);
        }
    }

    private boolean canUpdateUser(UserTableItem targetUser) {
        String currentRole = AuthenticationService.getInstance().getUserType();
        String currentEmail = getCurrentUserEmail();

        if ("ADMIN".equalsIgnoreCase(currentRole)) {
            return !"CLIENTE".equalsIgnoreCase(targetUser.getType());
        }

        if ("ENTREGADOR".equalsIgnoreCase(currentRole)) {
            return "ENTREGADOR".equalsIgnoreCase(targetUser.getType())
                    && currentEmail != null
                    && currentEmail.equalsIgnoreCase(targetUser.getEmail());
        }

        return false;
    }

    private boolean canRemoveUser(UserTableItem targetUser) {
        String currentRole = AuthenticationService.getInstance().getUserType();
        String currentEmail = getCurrentUserEmail();

        if ("ADMIN".equalsIgnoreCase(currentRole)) {
            return !"CLIENTE".equalsIgnoreCase(targetUser.getType())
                    && (currentEmail == null || !currentEmail.equalsIgnoreCase(targetUser.getEmail()));
        }

        return false;
    }

    private String getCurrentUserEmail() {
        AuthenticationService.CurrentUser currentUser = AuthenticationService.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.email : null;
    }

    private void showUserDetails(UserTableItem user) {
        Thread thread = new Thread(() -> {
            try {
                JsonNode userDetails = getJson(USERS_BASE_URL + "/" + user.getUserId());

                StringBuilder detailText = new StringBuilder();
                detailText.append("Nome: ").append(userDetails.path("nome").asText("-")).append("\n");
                detailText.append("Email: ").append(userDetails.path("email").asText("-")).append("\n");
                detailText.append("Tipo: ").append(userDetails.path("role").asText("-")).append("\n");
                detailText.append("Ativo: ").append(userDetails.path("ativo").asBoolean(true) ? "Sim" : "Não");

                if ("ADMIN".equalsIgnoreCase(user.getType())) {
                    List<RestaurantItem> restaurantes = loadRestaurantsByAdmin(user.getUserId());
                    detailText.append("\n\nRestaurantes geridos: ");
                    if (restaurantes.isEmpty()) {
                        detailText.append("nenhum");
                    } else {
                        for (RestaurantItem restaurante : restaurantes) {
                            detailText.append("\n- ").append(restaurante.getNome());
                        }
                    }
                } else if ("ENTREGADOR".equalsIgnoreCase(user.getType())) {
                    boolean disponivel = userDetails.path("disponivel").asBoolean(false);
                    detailText.append("\nDisponível para entregas: ").append(disponivel ? "Sim" : "Não");
                }

                Platform.runLater(() -> {
                    Alert detail = new Alert(Alert.AlertType.INFORMATION);
                    detail.setTitle("Detalhes do Utilizador");
                    detail.setHeaderText("Verificação de utilizador");
                    detail.setContentText(detailText.toString());
                    detail.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao verificar utilizador: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void openUpdateDialog(UserTableItem user) {
        if ("ADMIN".equalsIgnoreCase(user.getType())) {
            openAdminRestaurantsUpdateDialog(user);
            return;
        }

        if ("ENTREGADOR".equalsIgnoreCase(user.getType())) {
            openEntregadorAvailabilityDialog(user);
            return;
        }

        lblStatus.setText("Cliente apenas permite verificação.");
    }

    private void openAdminRestaurantsUpdateDialog(UserTableItem user) {
        List<RestaurantItem> restaurantes = loadRestaurantsByAdmin(user.getUserId());

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Atualizar Admin");
        dialog.setHeaderText("Gestão de restaurantes de " + user.getName());

        StringBuilder message = new StringBuilder("Restaurantes atuais: ");
        if (restaurantes.isEmpty()) {
            message.append("nenhum");
        } else {
            for (RestaurantItem restaurante : restaurantes) {
                message.append("\n- ").append(restaurante.getNome());
            }
        }
        dialog.setContentText(message.toString());

        ButtonType btnAdd = new ButtonType("Acrescentar restaurante", ButtonBar.ButtonData.LEFT);
        ButtonType btnRemoveRestaurant = new ButtonType("Retirar restaurante", ButtonBar.ButtonData.LEFT);
        ButtonType btnCancel = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(btnAdd, btnRemoveRestaurant, btnCancel);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == btnCancel) {
            return;
        }

        if (result.get() == btnAdd) {
            createRestaurantForAdmin(user);
            return;
        }

        if (result.get() == btnRemoveRestaurant) {
            removeRestaurantFromAdmin(user, restaurantes);
        }
    }

    private void createRestaurantForAdmin(UserTableItem user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Restaurante");
        dialog.setHeaderText("Acrescentar restaurante ao admin " + user.getName());

        ButtonType saveButton = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");
        TextField txtNif = new TextField();
        txtNif.setPromptText("NIF");
        TextField txtRua = new TextField();
        txtRua.setPromptText("Rua");
        TextField txtCodigoPostal = new TextField();
        txtCodigoPostal.setPromptText("Código Postal");
        TextField txtCidade = new TextField();
        txtCidade.setPromptText("Cidade");
        TextField txtTipoCozinha = new TextField();
        txtTipoCozinha.setPromptText("Tipo de cozinha (opcional)");

        VBox content = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("NIF:"), txtNif,
                new Label("Rua:"), txtRua,
                new Label("Código Postal:"), txtCodigoPostal,
                new Label("Cidade:"), txtCidade,
                new Label("Tipo de cozinha:"), txtTipoCozinha);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        String nome = txtNome.getText() != null ? txtNome.getText().trim() : "";
        String nif = txtNif.getText() != null ? txtNif.getText().trim() : "";
        String rua = txtRua.getText() != null ? txtRua.getText().trim() : "";
        String codigoPostal = txtCodigoPostal.getText() != null ? txtCodigoPostal.getText().trim() : "";
        String cidade = txtCidade.getText() != null ? txtCidade.getText().trim() : "";
        String tipoCozinha = txtTipoCozinha.getText() != null ? txtTipoCozinha.getText().trim() : "";

        if (nome.isEmpty() || nif.isEmpty() || rua.isEmpty() || codigoPostal.isEmpty() || cidade.isEmpty()) {
            lblStatus.setText("Preencha nome, NIF e morada completa para criar restaurante.");
            return;
        }

        lblStatus.setText("A criar restaurante...");
        Thread thread = new Thread(() -> {
            try {
                JsonNode payload = objectMapper.createObjectNode()
                        .put("nome", nome)
                        .put("nif", nif)
                        .put("tipoCozinha", tipoCozinha)
                        .put("adminId", user.getUserId())
                        .set("morada", objectMapper.createObjectNode()
                                .put("rua", rua)
                                .put("codigoPostal", codigoPostal)
                                .put("cidade", cidade));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(RESTAURANTES_BASE_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .timeout(Duration.ofSeconds(8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode() + " ao criar restaurante");
                }

                Platform.runLater(() -> lblStatus.setText("Restaurante criado para " + user.getName()));
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao criar restaurante: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void removeRestaurantFromAdmin(UserTableItem user, List<RestaurantItem> restaurantes) {
        if (restaurantes.isEmpty()) {
            lblStatus.setText("Este admin não tem restaurantes para retirar.");
            return;
        }

        ChoiceDialog<RestaurantItem> choice = new ChoiceDialog<>(restaurantes.get(0), restaurantes);
        choice.setTitle("Retirar Restaurante");
        choice.setHeaderText("Seleciona o restaurante a retirar de " + user.getName());
        choice.setContentText("Restaurante:");

        Optional<RestaurantItem> selected = choice.showAndWait();
        if (selected.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover restaurante \"" + selected.get().getNome() + "\" do sistema?");
        Optional<ButtonType> confirmResult = confirm.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        lblStatus.setText("A retirar restaurante...");
        Thread thread = new Thread(() -> {
            try {
                String url = RESTAURANTES_BASE_URL + "/" + selected.get().getId() + "?adminId=" + user.getUserId();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .timeout(Duration.ofSeconds(8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode() + " ao retirar restaurante");
                }

                Platform.runLater(() -> lblStatus.setText("Restaurante retirado com sucesso."));
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao retirar restaurante: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void openEntregadorAvailabilityDialog(UserTableItem user) {
        Thread thread = new Thread(() -> {
            try {
                JsonNode userDetails = getJson(USERS_BASE_URL + "/" + user.getUserId());
                boolean disponivelAtual = userDetails.path("disponivel").asBoolean(false);

                Platform.runLater(() -> {
                    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
                    dialog.setTitle("Atualizar Entregador");
                    dialog.setHeaderText(user.getName());
                    dialog.setContentText("Estado atual: " + (disponivelAtual ? "Disponível" : "Indisponível")
                            + "\nPretende alternar o estado?");

                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }

                    updateEntregadorDisponibilidade(user.getUserId(), !disponivelAtual);
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao obter dados do entregador: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void updateEntregadorDisponibilidade(Long userId, boolean disponivel) {
        lblStatus.setText("A atualizar disponibilidade...");

        Thread thread = new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(USERS_BASE_URL + "/" + userId + "/disponibilidade-entregador?disponivel=" +
                                disponivel))
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(6))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode() + " ao atualizar disponibilidade");
                }

                Platform.runLater(() -> {
                    lblStatus.setText("Disponibilidade atualizada com sucesso");
                    searchUsers();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao atualizar disponibilidade: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void removeUser(UserTableItem user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover " + user.getName() + "?");
        Optional<ButtonType> confirmation = confirm.showAndWait();
        if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
            return;
        }

        lblStatus.setText("A remover utilizador...");
        Thread thread = new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(USERS_BASE_URL + "/" + user.getUserId()))
                        .DELETE()
                        .timeout(Duration.ofSeconds(6))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode() + " ao remover utilizador");
                }

                Platform.runLater(() -> {
                    lblStatus.setText("Utilizador removido com sucesso");
                    searchUsers();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao remover: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void refresh() {
        txtFilterName.clear();
        txtMinPedidos.clear();
        txtMinEntregas.clear();
        comboUserType.setValue("Todos");
        searchUsers();
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

    private List<RestaurantItem> loadRestaurantsByAdmin(Long adminId) {
        List<RestaurantItem> restaurantes = new ArrayList<>();
        try {
            JsonNode response = getJson(RESTAURANTES_BASE_URL);
            if (response.isArray()) {
                for (JsonNode restaurante : response) {
                    if (restaurante.path("adminId").asLong(-1L) == adminId) {
                        JsonNode moradaNode = restaurante.path("morada");
                        restaurantes.add(new RestaurantItem(
                                restaurante.path("id").asLong(),
                                restaurante.path("nome").asText(""),
                                restaurante.path("nif").asText(""),
                                moradaNode.path("rua").asText(""),
                                moradaNode.path("codigoPostal").asText(""),
                                moradaNode.path("cidade").asText("")));
                    }
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> lblStatus.setText("Erro ao carregar restaurantes do admin: " + e.getMessage()));
        }
        return restaurantes;
    }

    // Inner class for table items
    public static class UserTableItem {
        private Long userId;
        private String name;
        private String type;
        private String email;
        private boolean ativo;

        public UserTableItem(Long userId, String name, String type, String email, boolean ativo) {
            this.userId = userId;
            this.name = name;
            this.type = type;
            this.email = email;
            this.ativo = ativo;
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getEmail() {
            return email;
        }

        public boolean isAtivo() {
            return ativo;
        }
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
            return nome + " (" + nif + ")";
        }
    }
}
