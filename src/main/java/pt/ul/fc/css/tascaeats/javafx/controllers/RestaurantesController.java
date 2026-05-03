package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.stage.Stage;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Controller para a vista de Restaurantes
 * ResponsÃ¡vel por listar e gerir restaurantes
 */
public class RestaurantesController {

    private static final String RESTAURANTES_BASE_URL = "http://localhost:8081/api/restaurantes";
    private static final String USERS_BY_EMAIL_URL = "http://localhost:8081/api/users/email?email=";
    private static final String ADMINS_FILTER_URL = "http://localhost:8081/api/users/filtros?tipo=ADMIN";

    // FXML Elements
    @FXML
    private TableView<RestauranteTableItem> tblRestaurantes;
    @FXML
    private TableColumn<RestauranteTableItem, String> colNome;
    @FXML
    private TableColumn<RestauranteTableItem, String> colLocalizacao;
    @FXML
    private TableColumn<RestauranteTableItem, String> colTipoCozinha;
    @FXML
    private TableColumn<RestauranteTableItem, Double> colAvaliacao;
    @FXML
    private TableColumn<RestauranteTableItem, String> colStatus;
    @FXML
    private TableColumn<RestauranteTableItem, Void> colAcoes;

    @FXML
    private Label lblStatus;
    @FXML
    private Label lblTotalRestaurantes;
    @FXML
    private Button btnRecarregar;
    @FXML
    private Button btnNovoRestaurante;
    @FXML
    private Button btnCarrinho;
    @FXML
    private TextField txtFiltroNome;
    @FXML
    private TextField txtFiltroMinPedidos;
    @FXML
    private TextField txtFiltroMinAvaliacoes;
    @FXML
    private TextField txtFiltroMorada;
    @FXML
    private ComboBox<String> comboFiltroCozinha;
    @FXML
    private ComboBox<String> comboFiltroHorario;
    @FXML
    private TextField txtFiltroMinPrecoMedio;
    @FXML
    private TextField txtFiltroMaxPrecoMedio;

    private TascaEatsGrpcClient grpcClient;
    private String perfilAtual;
    private final List<RestauranteTableItem> restaurantesCache = new ArrayList<>();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[RestaurantesController] Inicializando...");
        perfilAtual = AuthenticationService.getInstance().getUserType();
        inicializarFiltros();
        setupTableColumns();
        aplicarPermissoesPorPerfil();
        carregarRestaurantes();
    }

    private void inicializarFiltros() {
        comboFiltroCozinha.getItems().addAll(
                "", "Portuguesa", "Italiana", "Chinesa", "Japonesa", "Mexicana", "Indiana", "Tailandesa",
                "Americana", "Tradicional", "Outra");
        comboFiltroCozinha.setValue("");

        comboFiltroHorario.getItems().addAll("Todos", "Aberto", "Fechado");
        comboFiltroHorario.setValue("Todos");
    }

    private void aplicarPermissoesPorPerfil() {
        boolean podeGerirRestaurantes = "ADMIN".equalsIgnoreCase(perfilAtual);
        btnNovoRestaurante.setDisable(!podeGerirRestaurantes);
        btnNovoRestaurante.setVisible(podeGerirRestaurantes);
        btnNovoRestaurante.setManaged(podeGerirRestaurantes);
    }

    /**
     * Configurar colunas da tabela
     */
    private void setupTableColumns() {
        tblRestaurantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));
        colTipoCozinha.setCellValueFactory(new PropertyValueFactory<>("tipoCozinha"));
        colAvaliacao.setCellValueFactory(new PropertyValueFactory<>("avaliacao"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Adicionar botÃµes de aÃ§Ã£o
        colAcoes.setCellFactory(param -> new TableCell<RestauranteTableItem, Void>() {
            private final Button btnVerMenus = new Button("Ver Menus");
            private final Button btnAlternarEstado = new Button();

            {
                btnVerMenus.setStyle("-fx-font-size: 10; -fx-padding: 5 10;");
                btnAlternarEstado.setStyle("-fx-font-size: 10; -fx-padding: 5 10;");

                btnVerMenus.setOnAction(event -> {
                    RestauranteTableItem item = getTableView().getItems().get(getIndex());
                    verMenusDoRestaurante(item.getId(), item.getNome());
                });

                btnAlternarEstado.setOnAction(event -> {
                    RestauranteTableItem row = getTableView().getItems().get(getIndex());
                    alternarEstadoRestaurante(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                RestauranteTableItem row = getTableView().getItems().get(getIndex());
                btnAlternarEstado.setText(row.isAberto() ? "Fechar" : "Abrir");

                if ("ADMIN".equalsIgnoreCase(perfilAtual)) {
                    setGraphic(new HBox(5, btnVerMenus, btnAlternarEstado));
                } else {
                    setGraphic(new HBox(5, btnVerMenus));
                }
            }
        });
    }

    @FXML
    private void aplicarFiltros() {
        carregarRestaurantes();
    }

    @FXML
    private void limparFiltros() {
        txtFiltroNome.clear();
        txtFiltroMinPedidos.clear();
        txtFiltroMinAvaliacoes.clear();
        txtFiltroMorada.clear();
        txtFiltroMinPrecoMedio.clear();
        txtFiltroMaxPrecoMedio.clear();
        comboFiltroCozinha.setValue("");
        comboFiltroHorario.setValue("Todos");
        carregarRestaurantes();
    }

    /**
     * Carregar restaurantes do servidor gRPC
     */
    @FXML
    private void recarregarRestaurantes() {
        carregarRestaurantes();
    }

    private void carregarRestaurantes() {
        lblStatus.setText("Carregando restaurantes...");
        btnRecarregar.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                // Conectar ao gRPC se necessÃ¡rio
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                // Chamar gRPC com filtros disponÃ­veis no contrato.
                ListarRestaurantesRequest.Builder requestBuilder = ListarRestaurantesRequest.newBuilder();

                String nome = txtFiltroNome.getText() != null ? txtFiltroNome.getText().trim() : "";
                if (!nome.isBlank()) {
                    requestBuilder.setNome(nome);
                }

                String tipoCozinha = comboFiltroCozinha.getValue() != null ? comboFiltroCozinha.getValue().trim() : "";
                if (!tipoCozinha.isBlank()) {
                    requestBuilder.setTipoCozinha(tipoCozinha);
                }

                Integer minPedidos = parseIntOrNull(txtFiltroMinPedidos.getText());
                if (minPedidos != null) {
                    requestBuilder.setMinPedidos(minPedidos);
                }

                Integer minAvaliacoes = parseIntOrNull(txtFiltroMinAvaliacoes.getText());
                if (minAvaliacoes != null) {
                    requestBuilder.setMinAvaliacoes(minAvaliacoes);
                }

                Double minPreco = parseDoubleOrNull(txtFiltroMinPrecoMedio.getText());
                if (minPreco != null) {
                    requestBuilder.setMinPreco(minPreco);
                }

                Double maxPreco = parseDoubleOrNull(txtFiltroMaxPrecoMedio.getText());
                if (maxPreco != null) {
                    requestBuilder.setMaxPreco(maxPreco);
                }

                String filtroMorada = txtFiltroMorada.getText() != null ? txtFiltroMorada.getText().trim() : "";
                if (!filtroMorada.isBlank()) {
                    requestBuilder.setCidade(filtroMorada);
                }

                String filtroHorario = comboFiltroHorario.getValue();
                if ("Aberto".equalsIgnoreCase(filtroHorario)) {
                    requestBuilder.setHorario(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
                }

                ListarRestaurantesRequest request = requestBuilder.build();
                ListarRestaurantesResponse response = grpcClient.listarRestaurantes(request);

                // Atualizar UI
                Platform.runLater(() -> {
                    try {
                        restaurantesCache.clear();

                        List<RestauranteTableItem> items = response.getRestaurantesList().stream()
                                .map(r -> new RestauranteTableItem(
                                        (int) r.getId(),
                                        r.getNome(),
                                        r.getMorada().getRua() + ", " + r.getMorada().getCidade(),
                                        0.0,
                                        r.getAberto() ? "Aberto" : "Fechado",
                                        r.getTipoCozinha(),
                                        r.getAberto()))
                                .collect(Collectors.toList());

                        restaurantesCache.addAll(aplicarFiltrosLocais(items));
                        tblRestaurantes.getItems().setAll(restaurantesCache);
                        lblTotalRestaurantes.setText("Total: " + restaurantesCache.size() + " restaurantes");
                        lblStatus.setText(restaurantesCache.size() + " restaurantes carregados");
                        btnRecarregar.setDisable(false);

                    } catch (Exception e) {
                        lblStatus.setText("Erro ao processar resposta");
                        System.err.println("[RestaurantesController] Erro UI: " + e.getMessage());
                        btnRecarregar.setDisable(false);
                    }
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro gRPC: " + e.getStatus().getCode());
                    System.err.println("[RestaurantesController] gRPC Error: " + e.getStatus());
                    btnRecarregar.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro: " + e.getMessage());
                    System.err.println("[RestaurantesController] Error: " + e.getMessage());
                    e.printStackTrace();
                    btnRecarregar.setDisable(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private List<RestauranteTableItem> aplicarFiltrosLocais(List<RestauranteTableItem> items) {
        String filtroMorada = txtFiltroMorada.getText() == null ? "" : txtFiltroMorada.getText().trim().toLowerCase();
        String filtroCozinha = comboFiltroCozinha.getValue() == null ? ""
                : comboFiltroCozinha.getValue().trim().toLowerCase();
        String filtroHorario = comboFiltroHorario.getValue() == null ? "Todos" : comboFiltroHorario.getValue();

        return items.stream()
                .filter(r -> filtroMorada.isBlank() || r.getLocalizacao().toLowerCase().contains(filtroMorada))
                .filter(r -> filtroCozinha.isBlank() || r.getTipoCozinha().toLowerCase().contains(filtroCozinha))
                .filter(r -> {
                    if ("Fechado".equalsIgnoreCase(filtroHorario)) {
                        return !r.isAberto();
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Ver menus de um restaurante
     */
    private void verMenusDoRestaurante(int restauranteId, String restauranteNome) {
        System.out.println("[RestaurantesController] Ver menus do restaurante: " + restauranteNome);

        try {
            // Conectar ao gRPC se necessÃ¡rio
            if (grpcClient == null) {
                grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                grpcClient.connect();
            }

            // Carrega o FXML do MenusController
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/menus.fxml"));
            Parent root = loader.load();
            MenusController menusController = loader.getController();

            // Criar nova Stage para os menus
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Menus - " + restauranteNome);
            stage.setScene(new javafx.scene.Scene(root, 900, 600));

            // PrÃ©-selecionar restaurante
            menusController.presetarRestaurante(restauranteId);

            stage.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir menus do restaurante");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.err.println("[RestaurantesController] Erro ao carregar menus.fxml: " + e.getMessage());
        }
    }

    private void alternarEstadoRestaurante(RestauranteTableItem item) {
        if (!"ADMIN".equalsIgnoreCase(perfilAtual)) {
            return;
        }

        boolean novoEstado = !item.isAberto();
        lblStatus.setText("A atualizar estado de " + item.getNome() + "...");

        Thread thread = new Thread(() -> {
            try {
                Long currentAdminId = resolveCurrentAdminId();
                Long ownerAdminId = resolveOwnerAdminId(item.getId());

                if (currentAdminId == null || ownerAdminId == null || !currentAdminId.equals(ownerAdminId)) {
                    Platform.runLater(() -> lblStatus
                            .setText("Apenas o administrador dono pode abrir/fechar este restaurante"));
                    return;
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(RESTAURANTES_BASE_URL + "/" + item.getId() + "/estado?aberto=" + novoEstado))
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(6))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }

                Platform.runLater(() -> {
                    item.setAberto(novoEstado);
                    item.setStatus(novoEstado ? "Aberto" : "Fechado");
                    tblRestaurantes.refresh();
                    lblStatus.setText("Estado atualizado com sucesso");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao alterar estado: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Long resolveOwnerAdminId(int restauranteId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESTAURANTES_BASE_URL + "/" + restauranteId))
                    .GET()
                    .timeout(Duration.ofSeconds(6))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                long adminId = root.path("adminId").asLong(0L);
                if (adminId > 0) {
                    return adminId;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long resolveCurrentAdminId() {
        try {
            AuthenticationService auth = AuthenticationService.getInstance();
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().userId;
                if (userId != null && userId.matches("\\d+")) {
                    return Long.parseLong(userId);
                }

                Long byEmail = findUserIdByEmail(auth.getCurrentUser().email);
                if (byEmail != null) {
                    return byEmail;
                }

                if ("admin@example.com".equalsIgnoreCase(auth.getCurrentUser().email)) {
                    Long fallbackDemo = findUserIdByEmail("admin@tascaeats.pt");
                    if (fallbackDemo != null) {
                        return fallbackDemo;
                    }
                }
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ADMINS_FILTER_URL))
                    .GET()
                    .timeout(Duration.ofSeconds(6))
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
                    .timeout(Duration.ofSeconds(6))
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

    @FXML
    private void novoRestaurante() {
        String perfil = AuthenticationService.getInstance().getUserType();
        if (!"ADMIN".equalsIgnoreCase(perfil)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso negado");
            alert.setHeaderText("AÃ§Ã£o nÃ£o permitida");
            alert.setContentText("Apenas administradores podem criar restaurantes.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/restauranteForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Novo Restaurante");
            stage.setScene(new javafx.scene.Scene(root, 640, 520));
            stage.showAndWait();

            carregarRestaurantes();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir formulÃ¡rio de restaurante");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void verCarrinho() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pedidos.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Carrinho e Pedidos");
            stage.setScene(new javafx.scene.Scene(root, 1000, 650));
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir carrinho");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Classe para representar restaurante na tabela
     */
    public static class RestauranteTableItem {
        private int id;
        private String nome;
        private String localizacao;
        private double avaliacao;
        private String status;
        private String tipoCozinha;
        private boolean aberto;

        public RestauranteTableItem(int id, String nome, String localizacao, double avaliacao,
                String status, String tipoCozinha, boolean aberto) {
            this.id = id;
            this.nome = nome;
            this.localizacao = localizacao;
            this.avaliacao = avaliacao;
            this.status = status;
            this.tipoCozinha = tipoCozinha;
            this.aberto = aberto;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getLocalizacao() {
            return localizacao;
        }

        public double getAvaliacao() {
            return avaliacao;
        }

        public String getStatus() {
            return status;
        }

        public String getTipoCozinha() {
            return tipoCozinha;
        }

        public boolean isAberto() {
            return aberto;
        }

        public void setAberto(boolean aberto) {
            this.aberto = aberto;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
