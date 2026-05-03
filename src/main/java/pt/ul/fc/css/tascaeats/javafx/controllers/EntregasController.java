package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import pt.ul.fc.css.tascaeats.grpc.ListarEntregasRequest;
import pt.ul.fc.css.tascaeats.grpc.ListarEntregasResponse;
import pt.ul.fc.css.tascaeats.grpc.EntregaResponse;
import pt.ul.fc.css.tascaeats.grpc.AtualizarStatusEntregaRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * Controller para a vista de Entregas
 * Requisito M: Atribuição de um entregador a um pedido
 * v1.2 - Implementação completa
 */
public class EntregasController {

    private static final String PEDIDOS_BASE_URL = "http://localhost:8081/api/pedidos";
    private static final String ENTREGAS_BASE_URL = "http://localhost:8081/api/entregas";
    private static final String USERS_FILTER_URL = "http://localhost:8081/api/users/filtros?tipo=ENTREGADOR";
    private static final String USERS_BY_EMAIL_URL = "http://localhost:8081/api/users/email?email=";

    @FXML
    private TableView<EntregaTableItem> tblEntregas;
    @FXML
    private TableColumn<EntregaTableItem, Integer> colEntregaId;
    @FXML
    private TableColumn<EntregaTableItem, String> colEntregaData;
    @FXML
    private TableColumn<EntregaTableItem, String> colEntregaEndereco;
    @FXML
    private TableColumn<EntregaTableItem, String> colEntregaStatus;
    @FXML
    private TableColumn<EntregaTableItem, String> colEntregaTempo;

    @FXML
    private Label lblEntregaStatus;
    @FXML
    private Label lblTotalEntregas;
    @FXML
    private Button btnRecarregarEntregas;
    @FXML
    private Button btnNovaEntrega;
    @FXML
    private Button btnCancelarEntrega;

    private TascaEatsGrpcClient grpcClient;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String perfilAtual;

    @FXML
    public void initialize() {
        System.out.println("[EntregasController] Inicializando...");
        perfilAtual = AuthenticationService.getInstance().getUserType();
        setupTableColumns();
        aplicarPermissoesPorPerfil();
        loadEntregas();
    }

    private void aplicarPermissoesPorPerfil() {
        boolean entregador = "ENTREGADOR".equalsIgnoreCase(perfilAtual);
        if (btnCancelarEntrega != null) {
            btnCancelarEntrega.setVisible(entregador);
            btnCancelarEntrega.setManaged(entregador);
            btnCancelarEntrega.setDisable(!entregador);
        }
    }

    private void setupTableColumns() {
        tblEntregas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colEntregaId.setCellValueFactory(new PropertyValueFactory<>("entregaId"));
        colEntregaData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colEntregaEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colEntregaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEntregaTempo.setCellValueFactory(new PropertyValueFactory<>("tempoEstimado"));
    }

    private void loadEntregas() {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                // Chamar gRPC listarEntregas()
                ListarEntregasRequest request = ListarEntregasRequest.newBuilder().build();
                ListarEntregasResponse response = grpcClient.listarEntregas(request);

                ObservableList<EntregaTableItem> entregas = FXCollections.observableArrayList();
                for (EntregaResponse e : response.getEntregasList()) {
                    String data = !e.getHoraRetirada().isEmpty() ? e.getHoraRetirada() : "-";
                    String endereco = "Pedido #" + e.getPedidoId();
                    String tempo = "CONCLUIDA".equals(e.getStatus()) ? "0 min" : "--";
                    entregas.add(new EntregaTableItem(
                            (int) e.getId(),
                            data,
                            endereco,
                            e.getStatus(),
                            tempo));
                }

                Platform.runLater(() -> {
                    tblEntregas.setItems(entregas);
                    lblTotalEntregas.setText("Total: " + entregas.size() + " entregas");
                    lblEntregaStatus.setText("Entregas carregadas");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEntregaStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Requisito M: Atualizar status da entrega / Rastreamento
     */
    @FXML
    private void trackDelivery() {
        EntregaTableItem selected = tblEntregas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblEntregaStatus.setText("Selecione uma entrega");
            return;
        }

        // Mostrar status atual
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Rastreamento de Entrega");
        info.setHeaderText("Entrega #" + selected.getEntregaId());
        info.setContentText("Status: " + selected.getStatus() + "\nTempo Estimado: " + selected.getTempoEstimado()
                + "\nEndereço: " + selected.getEndereco());
        info.showAndWait();
    }

    /**
     * Atualizar status da entrega
     */
    @FXML
    private void updateDeliveryStatus() {
        EntregaTableItem selected = tblEntregas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblEntregaStatus.setText("Selecione uma entrega");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("A_CAMINHO", "ATRIBUIDA", "A_CAMINHO", "CONCLUIDA",
                "CANCELADA");
        dialog.setTitle("Atualizar Status");
        dialog.setHeaderText("Novo status para entrega #" + selected.getEntregaId());
        dialog.setContentText("Status:");

        if (dialog.showAndWait().isPresent()) {
            String novoStatus = dialog.getResult();
            lblEntregaStatus.setText("⏳ Atualizando status...");

            Thread t = new Thread(() -> {
                try {
                    if (grpcClient == null) {
                        grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                        grpcClient.connect();
                    }

                    // Chamar gRPC atualizarStatusEntrega()
                    AtualizarStatusEntregaRequest request = AtualizarStatusEntregaRequest.newBuilder()
                            .setEntregaId(selected.getEntregaId())
                            .setNovoStatus(novoStatus)
                            .build();

                    grpcClient.atualizarStatusEntrega(request);

                    Platform.runLater(() -> {
                        lblEntregaStatus
                                .setText("Entrega #" + selected.getEntregaId() + " atualizada para: " + novoStatus);
                        loadEntregas();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> lblEntregaStatus.setText("Erro: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    @FXML
    private void assignDeliveryManual() {
        TextInputDialog pedidoDialog = new TextInputDialog();
        pedidoDialog.setTitle("Atribuição Manual");
        pedidoDialog.setHeaderText("Atribuir entregador a pedido READY");
        pedidoDialog.setContentText("ID do pedido READY:");

        Optional<String> pedidoInput = pedidoDialog.showAndWait();
        if (pedidoInput.isEmpty()) {
            return;
        }

        long pedidoId;
        try {
            pedidoId = Long.parseLong(pedidoInput.get().trim());
        } catch (NumberFormatException e) {
            lblEntregaStatus.setText("ID de pedido inválido");
            return;
        }

        if ("ENTREGADOR".equalsIgnoreCase(perfilAtual)) {
            Long meuEntregadorId = resolverEntregadorAtualPorEmail();
            if (meuEntregadorId == null || meuEntregadorId <= 0) {
                lblEntregaStatus.setText("Nao foi possivel identificar o entregador atual");
                return;
            }

            Thread assignSelfThread = new Thread(
                    () -> atribuirEntregador(pedidoId, "{\"entregadorId\":" + meuEntregadorId + "}"));
            assignSelfThread.setDaemon(true);
            assignSelfThread.start();
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                List<EntregadorOption> entregadores = carregarEntregadoresDisponiveis();
                if (entregadores.isEmpty()) {
                    Platform.runLater(() -> lblEntregaStatus.setText("Sem entregadores disponíveis"));
                    return;
                }

                List<String> options = entregadores.stream()
                        .map(e -> e.nome + " (#" + e.id + ")")
                        .toList();

                Platform.runLater(() -> {
                    ChoiceDialog<String> entregadorDialog = new ChoiceDialog<>(options.get(0), options);
                    entregadorDialog.setTitle("Entregador");
                    entregadorDialog.setHeaderText("Selecione entregador");
                    entregadorDialog.setContentText("Entregador:");

                    Optional<String> selected = entregadorDialog.showAndWait();
                    if (selected.isEmpty()) {
                        return;
                    }

                    long entregadorId = entregadores.stream()
                            .filter(e -> selected.get().contains("#" + e.id + ")"))
                            .findFirst()
                            .map(e -> e.id)
                            .orElse(0L);
                    if (entregadorId <= 0) {
                        lblEntregaStatus.setText("Entregador inválido");
                        return;
                    }

                    Thread assignThread = new Thread(
                            () -> atribuirEntregador(pedidoId, "{\"entregadorId\":" + entregadorId + "}"));
                    assignThread.setDaemon(true);
                    assignThread.start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEntregaStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void assignDeliveryAuto() {
        TextInputDialog pedidoDialog = new TextInputDialog();
        pedidoDialog.setTitle("Atribuição Automática");
        pedidoDialog.setHeaderText("Atribuir automaticamente para pedido READY");
        pedidoDialog.setContentText("ID do pedido READY:");

        Optional<String> pedidoInput = pedidoDialog.showAndWait();
        if (pedidoInput.isEmpty()) {
            return;
        }

        try {
            long pedidoId = Long.parseLong(pedidoInput.get().trim());
            Thread assignThread = new Thread(() -> atribuirEntregador(pedidoId, "{}"));
            assignThread.setDaemon(true);
            assignThread.start();
        } catch (NumberFormatException e) {
            lblEntregaStatus.setText("ID de pedido inválido");
        }
    }

    @FXML
    private void startSelectedDelivery() {
        EntregaTableItem selected = tblEntregas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblEntregaStatus.setText("Selecione uma entrega");
            return;
        }
        callEntregaPatch(selected.getEntregaId(), "/iniciar", "Entrega iniciada");
    }

    @FXML
    private void concludeSelectedDelivery() {
        EntregaTableItem selected = tblEntregas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblEntregaStatus.setText("Selecione uma entrega");
            return;
        }
        callEntregaPatch(selected.getEntregaId(), "/concluir", "Entrega concluída");
    }

    @FXML
    private void cancelSelectedDelivery() {
        if (!"ENTREGADOR".equalsIgnoreCase(perfilAtual)) {
            lblEntregaStatus.setText("Apenas entregadores podem cancelar entregas");
            return;
        }

        EntregaTableItem selected = tblEntregas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblEntregaStatus.setText("Selecione uma entrega");
            return;
        }

        if (!"A_CAMINHO".equalsIgnoreCase(selected.getStatus())) {
            lblEntregaStatus.setText("So pode cancelar entregas no estado A_CAMINHO");
            return;
        }

        callEntregaPatch(selected.getEntregaId(), "/cancelar", "Entrega cancelada");
    }

    private void atribuirEntregador(long pedidoId, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PEDIDOS_BASE_URL + "/" + pedidoId + "/entregar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + response.statusCode() + " na atribuição");
            }

            Platform.runLater(() -> {
                lblEntregaStatus.setText("Entregador atribuído ao pedido #" + pedidoId);
                loadEntregas();
            });
        } catch (Exception e) {
            Platform.runLater(() -> lblEntregaStatus.setText("Atribuição inválida: " + e.getMessage()));
        }
    }

    private void callEntregaPatch(int entregaId, String action, String successMessage) {
        Thread thread = new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ENTREGAS_BASE_URL + "/" + entregaId + action))
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }

                Platform.runLater(() -> {
                    lblEntregaStatus.setText(successMessage + " (#" + entregaId + ")");
                    loadEntregas();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEntregaStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private List<EntregadorOption> carregarEntregadoresDisponiveis() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_FILTER_URL))
                .GET()
                .timeout(Duration.ofSeconds(6))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        List<EntregadorOption> result = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response.body());
        for (JsonNode user : root) {
            if (!user.path("disponivel").asBoolean(false)) {
                continue;
            }
            long id = user.path("id").asLong(0L);
            String nome = user.path("nome").asText("");
            if (id > 0 && !nome.isBlank()) {
                result.add(new EntregadorOption(id, nome));
            }
        }
        return result;
    }

    private Long resolverEntregadorAtualPorEmail() {
        try {
            if (AuthenticationService.getInstance().getCurrentUser() == null) {
                return null;
            }

            String email = AuthenticationService.getInstance().getCurrentUser().email;
            if (email == null || email.isBlank()) {
                return null;
            }

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

            if ("entregador@example.com".equalsIgnoreCase(email)) {
                String encodedDemo = URLEncoder.encode("entregador@tascaeats.pt", StandardCharsets.UTF_8);
                HttpRequest fallback = HttpRequest.newBuilder()
                        .uri(URI.create(USERS_BY_EMAIL_URL + encodedDemo))
                        .GET()
                        .timeout(Duration.ofSeconds(8))
                        .build();
                HttpResponse<String> fallbackResponse = httpClient.send(fallback, HttpResponse.BodyHandlers.ofString());
                if (fallbackResponse.statusCode() >= 200 && fallbackResponse.statusCode() < 300) {
                    JsonNode root = objectMapper.readTree(fallbackResponse.body());
                    long id = root.path("id").asLong(0L);
                    if (id > 0) {
                        return id;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @FXML
    private void refresh() {
        loadEntregas();
    }

    public static class EntregaTableItem {
        private Integer entregaId;
        private String data;
        private String endereco;
        private String status;
        private String tempoEstimado;

        public EntregaTableItem(Integer entregaId, String data, String endereco, String status, String tempoEstimado) {
            this.entregaId = entregaId;
            this.data = data;
            this.endereco = endereco;
            this.status = status;
            this.tempoEstimado = tempoEstimado;
        }

        public Integer getEntregaId() {
            return entregaId;
        }

        public String getData() {
            return data;
        }

        public String getEndereco() {
            return endereco;
        }

        public String getStatus() {
            return status;
        }

        public String getTempoEstimado() {
            return tempoEstimado;
        }
    }

    private static class EntregadorOption {
        private final long id;
        private final String nome;

        private EntregadorOption(long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }
}