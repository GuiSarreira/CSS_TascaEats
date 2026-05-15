package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para a vista de Pedidos
 * Requisito L: Atualização do estado do pedido
 * Requisito M: Atribuição de entregador
 * Requisito N: Cancelamento do pedido
 */
public class PedidosController {

    private static final String PEDIDOS_BASE_URL = "http://localhost:8082/api/pedidos";
    private static final String ENTREGAS_BASE_URL = "http://localhost:8082/api/pedidos";
    private static final String USERS_FILTER_URL = "http://localhost:8082/api/users/filtros?tipo=ENTREGADOR";

    @FXML
    private TableView<PedidoTableItem> tblPedidos;
    @FXML
    private TableColumn<PedidoTableItem, Integer> colPedidoId;
    @FXML
    private TableColumn<PedidoTableItem, String> colPedidoData;
    @FXML
    private TableColumn<PedidoTableItem, String> colPedidoRestaurante;
    @FXML
    private TableColumn<PedidoTableItem, Double> colPedidoTotal;
    @FXML
    private TableColumn<PedidoTableItem, String> colPedidoStatus;
    @FXML
    private TableColumn<PedidoTableItem, Void> colPedidoAcoes;

    @FXML
    private Label lblPedidoStatus;
    @FXML
    private Label lblTotalPedidos;
    @FXML
    private Button btnUpdateStatus;
    @FXML
    private Button btnAssignDelivery;
    @FXML
    private Button btnCancelPedido;

    private TascaEatsGrpcClient grpcClient;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("[PedidosController] Inicializando...");
        setupTableColumns();
        loadPedidos();
    }

    private void setupTableColumns() {
        tblPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colPedidoId.setCellValueFactory(new PropertyValueFactory<>("pedidoId"));
        colPedidoData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colPedidoRestaurante.setCellValueFactory(new PropertyValueFactory<>("restaurante"));
        colPedidoTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPedidoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadPedidos() {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                ListarPedidosRequest request = ListarPedidosRequest.newBuilder().build();
                ListarPedidosResponse response = grpcClient.listarPedidos(request);

                Platform.runLater(() -> {
                    try {
                        tblPedidos.getItems().clear();

                        List<PedidoTableItem> items = response.getPedidosList().stream()
                                .map(p -> new PedidoTableItem(
                                        (int) p.getId(),
                                        p.getDataHora(),
                                        p.getRestauranteNome(),
                                        p.getPrecoTotal(),
                                        p.getStatus()))
                                .collect(Collectors.toList());

                        tblPedidos.setItems(FXCollections.observableArrayList(items));
                        lblTotalPedidos.setText("Total: " + items.size() + " pedidos");
                        lblPedidoStatus.setText(items.size() + " pedidos carregados");

                    } catch (Exception e) {
                        lblPedidoStatus.setText("Erro ao processar");
                        System.err.println("[PedidosController] Erro UI: " + e.getMessage());
                    }
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblPedidoStatus.setText("Erro gRPC: " + e.getStatus().getCode());
                    System.err.println("[PedidosController] gRPC Error: " + e.getStatus());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblPedidoStatus.setText("Erro: " + e.getMessage());
                    System.err.println("[PedidosController] Error: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Requisito L: Atualizar estado do pedido
     */
    @FXML
    private void updateOrderStatus() {
        PedidoTableItem selected = tblPedidos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblPedidoStatus.setText("Selecione um pedido");
            return;
        }

        String estadoAtual = selected.getStatus();
        String proximoEstado;
        if ("PAID".equalsIgnoreCase(estadoAtual)) {
            proximoEstado = "PREPARING";
        } else if ("PREPARING".equalsIgnoreCase(estadoAtual)) {
            proximoEstado = "READY";
        } else {
            lblPedidoStatus.setText("Atualização só permitida em PAID ou PREPARING");
            return;
        }

        lblPedidoStatus.setText("A atualizar pedido #" + selected.getPedidoId() + " para " + proximoEstado + "...");

        Thread t = new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(PEDIDOS_BASE_URL + "/" + selected.getPedidoId() + "/avancar"))
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP " + response.statusCode() + " ao atualizar pedido");
                }

                Platform.runLater(() -> {
                    lblPedidoStatus.setText("Pedido #" + selected.getPedidoId() + " atualizado para " + proximoEstado);
                    loadPedidos();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblPedidoStatus.setText("Erro ao atualizar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Requisito M: Atribuir entregador a um pedido
     */
    @FXML
    private void assignDeliveryPerson() {
        PedidoTableItem selected = tblPedidos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblPedidoStatus.setText("Selecione um pedido");
            return;
        }

        if (!"READY".equalsIgnoreCase(selected.getStatus())) {
            lblPedidoStatus.setText("Atribuição só permitida em pedidos READY");
            return;
        }

        lblPedidoStatus.setText("A carregar entregadores...");

        Thread t = new Thread(() -> {
            try {
                List<EntregadorOption> entregadores = carregarEntregadoresDisponiveis();
                List<String> opcoes = new ArrayList<>();
                opcoes.add("AUTO (atribuição automática)");
                for (EntregadorOption e : entregadores) {
                    opcoes.add(e.nome + " (#" + e.id + ")");
                }

                Platform.runLater(() -> {
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(opcoes.get(0), opcoes);
                    dialog.setTitle("Atribuir Entregador");
                    dialog.setHeaderText("Pedido #" + selected.getPedidoId());
                    dialog.setContentText("Entregador:");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        lblPedidoStatus.setText("Atribuição cancelada");
                        return;
                    }

                    String escolha = result.get();
                    Thread assignThread = new Thread(
                            () -> atribuirEntregadorPedido(selected.getPedidoId(), escolha, entregadores));
                    assignThread.setDaemon(true);
                    assignThread.start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblPedidoStatus.setText("Erro ao carregar entregadores: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void atribuirEntregadorPedido(int pedidoId, String escolha, List<EntregadorOption> entregadores) {
        try {
            String requestBody = "{}";
            if (!escolha.startsWith("AUTO")) {
                EntregadorOption selectedEntregador = entregadores.stream()
                        .filter(e -> escolha.contains("#" + e.id + ")"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Entregador não encontrado"));
                requestBody = "{\"entregadorId\":" + selectedEntregador.id + "}";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENTREGAS_BASE_URL + "/" + pedidoId + "/entregar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + response.statusCode() + " ao atribuir entregador");
            }

            Platform.runLater(() -> {
                lblPedidoStatus.setText("Entregador atribuído ao pedido #" + pedidoId);
                loadPedidos();
            });
        } catch (Exception e) {
            Platform.runLater(() -> lblPedidoStatus.setText("Erro na atribuição: " + e.getMessage()));
        }
    }

    /**
     * Requisito N: Cancelar pedido
     */
    @FXML
    private void cancelOrder() {
        PedidoTableItem selected = tblPedidos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblPedidoStatus.setText("Selecione um pedido");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Pedido");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText(
                "Cancelar pedido #" + selected.getPedidoId() + " de €" + String.format("%.2f", selected.getTotal()));

        if (confirm.showAndWait().get() == ButtonType.OK) {
            lblPedidoStatus.setText("A cancelar pedido...");

            Thread t = new Thread(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(PEDIDOS_BASE_URL + "/" + selected.getPedidoId() + "/cancelar"))
                            .method("PATCH", HttpRequest.BodyPublishers.noBody())
                            .timeout(Duration.ofSeconds(8))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new RuntimeException("HTTP " + response.statusCode() + " ao cancelar pedido");
                    }

                    Platform.runLater(() -> {
                        lblPedidoStatus.setText("Pedido #" + selected.getPedidoId() + " cancelado");
                        loadPedidos();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> lblPedidoStatus.setText("Erro ao cancelar: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private List<EntregadorOption> carregarEntregadoresDisponiveis() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_FILTER_URL))
                .GET()
                .timeout(Duration.ofSeconds(6))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + " ao listar entregadores");
        }

        List<EntregadorOption> entregadores = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response.body());
        for (JsonNode node : root) {
            boolean disponivel = node.path("disponivel").asBoolean(false);
            if (!disponivel) {
                continue;
            }
            long id = node.path("id").asLong(0L);
            String nome = node.path("nome").asText("");
            if (id > 0 && !nome.isBlank()) {
                entregadores.add(new EntregadorOption(id, nome));
            }
        }
        return entregadores;
    }

    @FXML
    private void refresh() {
        loadPedidos();
    }

    public static class PedidoTableItem {
        private int pedidoId;
        private String data;
        private String restaurante;
        private double total;
        private String status;

        public PedidoTableItem(int pedidoId, String data, String restaurante, double total, String status) {
            this.pedidoId = pedidoId;
            this.data = data;
            this.restaurante = restaurante;
            this.total = total;
            this.status = status;
        }

        public int getPedidoId() {
            return pedidoId;
        }

        public String getData() {
            return data;
        }

        public String getRestaurante() {
            return restaurante;
        }

        public double getTotal() {
            return total;
        }

        public String getStatus() {
            return status;
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
