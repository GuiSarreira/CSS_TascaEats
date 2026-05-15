package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ul.fc.css.tascaeats.grpc.ListarPagamentosRequest;
import pt.ul.fc.css.tascaeats.grpc.ListarPagamentosResponse;
import pt.ul.fc.css.tascaeats.grpc.ListarPedidosRequest;
import pt.ul.fc.css.tascaeats.grpc.ListarPedidosResponse;
import pt.ul.fc.css.tascaeats.grpc.PedidoResponse;
import pt.ul.fc.css.tascaeats.grpc.PagamentoResponse;
import pt.ul.fc.css.tascaeats.grpc.RegistarPagamentoRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller para a vista de Pagamentos
 * Requisito K: Registo e processamento do pagamento associado a um pedido
 */
public class PagamentosController {

    private static final String PEDIDOS_BASE_URL = "http://localhost:8082/api/pedidos";

    @FXML
    private TableView<PagamentoTableItem> tblPagamentos;
    @FXML
    private TableColumn<PagamentoTableItem, Integer> colPagamentoId;
    @FXML
    private TableColumn<PagamentoTableItem, Integer> colPedidoId;
    @FXML
    private TableColumn<PagamentoTableItem, String> colPagamentoData;
    @FXML
    private TableColumn<PagamentoTableItem, Double> colPagamentoValor;
    @FXML
    private TableColumn<PagamentoTableItem, String> colPagamentoMetodo;
    @FXML
    private TableColumn<PagamentoTableItem, String> colPagamentoStatus;

    @FXML
    private Label lblPagamentoStatus;
    @FXML
    private Label lblTotalPagamentos;
    @FXML
    private Label lblValorTotal;
    @FXML
    private Button btnRecarregarPagamentos;
    @FXML
    private Button btnNovoPagamento;

    private TascaEatsGrpcClient grpcClient;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("[PagamentosController] Inicializando...");
        setupTableColumns();
        loadPagamentos();
    }

    private void setupTableColumns() {
        tblPagamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colPagamentoId.setCellValueFactory(new PropertyValueFactory<>("pagamentoId"));
        colPedidoId.setCellValueFactory(new PropertyValueFactory<>("pedidoId"));
        colPagamentoData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colPagamentoValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colPagamentoMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        colPagamentoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadPagamentos() {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }
                ObservableList<PagamentoTableItem> pagamentos = FXCollections.observableArrayList();

                ListarPedidosResponse pedidosResponse = grpcClient
                        .listarPedidos(ListarPedidosRequest.newBuilder().build());
                for (PedidoResponse pedido : pedidosResponse.getPedidosList()) {
                    Optional<PagamentoTableItem> paymentOpt = buscarPagamentoPorPedido(pedido.getId());
                    paymentOpt.ifPresent(pagamentos::add);
                }

                double total = pagamentos.stream().mapToDouble(PagamentoTableItem::getValor).sum();

                Platform.runLater(() -> {
                    tblPagamentos.setItems(pagamentos);
                    lblTotalPagamentos.setText("Total: " + pagamentos.size() + " pagamentos");
                    lblValorTotal.setText("Valor Total: EUR " + String.format("%.2f", total));
                    lblPagamentoStatus.setText("Pagamentos carregados");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblPagamentoStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Requisito K: Novo pagamento (criar e processar)
     */
    @FXML
    private void processPayment() {
        lblPagamentoStatus.setText("A preparar pagamento...");

        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                ListarPedidosResponse pedidosResponse = grpcClient
                        .listarPedidos(ListarPedidosRequest.newBuilder().build());
                List<PedidoResponse> pedidosCreated = pedidosResponse.getPedidosList().stream()
                        .filter(p -> "CREATED".equalsIgnoreCase(p.getStatus()))
                        .toList();

                if (pedidosCreated.isEmpty()) {
                    Platform.runLater(() -> lblPagamentoStatus.setText("Sem pedidos CREATED para pagar"));
                    return;
                }

                List<String> pedidoOptions = pedidosCreated.stream()
                        .map(p -> "Pedido #" + p.getId() + " - " + p.getRestauranteNome() + " (EUR "
                                + String.format("%.2f", p.getPrecoTotal()) + ")")
                        .toList();

                Platform.runLater(() -> {
                    ChoiceDialog<String> pedidoDialog = new ChoiceDialog<>(pedidoOptions.get(0), pedidoOptions);
                    pedidoDialog.setTitle("Selecionar Pedido");
                    pedidoDialog.setHeaderText("Processar pagamento de pedido CREATED");
                    pedidoDialog.setContentText("Pedido:");

                    Optional<String> selectedPedidoOption = pedidoDialog.showAndWait();
                    if (selectedPedidoOption.isEmpty()) {
                        lblPagamentoStatus.setText("Pagamento cancelado");
                        return;
                    }

                    long pedidoId = extrairPedidoId(selectedPedidoOption.get());

                    ChoiceDialog<String> methodDialog = new ChoiceDialog<>("MBWAY", "MBWAY", "MULTIBANCO", "DINHEIRO");
                    methodDialog.setTitle("Método de Pagamento");
                    methodDialog.setHeaderText("Selecione o método de pagamento");
                    methodDialog.setContentText("Método:");

                    Optional<String> methodOpt = methodDialog.showAndWait();
                    if (methodOpt.isEmpty()) {
                        lblPagamentoStatus.setText("Pagamento cancelado");
                        return;
                    }

                    String metodo = methodOpt.get();
                    Dialog<ButtonType> paymentDialog = new Dialog<>();
                    paymentDialog.setTitle("Dados de Pagamento");
                    paymentDialog.setHeaderText("Pedido #" + pedidoId + " - " + metodo);

                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(10));

                    TextField txtDetalhe = new TextField();
                    txtDetalhe.setPromptText("Referência / Telemóvel / Troco");
                    grid.add(new Label("Detalhe (opcional):"), 0, 0);
                    grid.add(txtDetalhe, 1, 0);

                    paymentDialog.getDialogPane().setContent(grid);
                    paymentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    Optional<ButtonType> confirm = paymentDialog.showAndWait();
                    if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
                        lblPagamentoStatus.setText("Pagamento cancelado");
                        return;
                    }

                    Thread processThread = new Thread(
                            () -> processarPagamentoGrpc(pedidoId, metodo, txtDetalhe.getText().trim()));
                    processThread.setDaemon(true);
                    processThread.start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblPagamentoStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Optional<PagamentoTableItem> buscarPagamentoPorPedido(long pedidoId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PEDIDOS_BASE_URL + "/" + pedidoId + "/pagamento"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + response.statusCode() + " ao consultar pagamento");
            }

            JsonNode node = objectMapper.readTree(response.body());
            String dataPagamento = node.path("dataPagamento").isNull() ? "-" : node.path("dataPagamento").asText("-");
            return Optional.of(new PagamentoTableItem(
                    (int) node.path("id").asLong(),
                    (int) pedidoId,
                    dataPagamento,
                    node.path("preco").asDouble(0.0),
                    node.path("tipoPagamento").asText(""),
                    node.path("status").asText("")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private long extrairPedidoId(String option) {
        String onlyDigits = option.replaceAll("[^0-9]", " ").trim().split("\\s+")[0];
        return Long.parseLong(onlyDigits);
    }

    private void processarPagamentoGrpc(long pedidoId, String metodo, String detalhe) {
        try {
            RegistarPagamentoRequest.Builder requestBuilder = RegistarPagamentoRequest.newBuilder()
                    .setPedidoId(pedidoId)
                    .setTipoPagamento(metodo);

            if ("MULTIBANCO".equals(metodo) && !detalhe.isEmpty()) {
                requestBuilder.setReferencia(detalhe);
            } else if ("MBWAY".equals(metodo) && !detalhe.isEmpty()) {
                requestBuilder.setTelemovel(detalhe);
            } else if ("DINHEIRO".equals(metodo) && !detalhe.isEmpty()) {
                requestBuilder.setTroco(Double.parseDouble(detalhe));
            }

            PagamentoResponse pagamento = grpcClient.registarPagamento(requestBuilder.build());
            Platform.runLater(() -> {
                lblPagamentoStatus.setText("Pagamento #" + pagamento.getId() + " processado em pedido #" + pedidoId);
                loadPagamentos();
            });
        } catch (Exception e) {
            Platform.runLater(() -> lblPagamentoStatus.setText("Erro ao processar: " + e.getMessage()));
        }
    }

    @FXML
    private void refresh() {
        loadPagamentos();
    }

    public static class PagamentoTableItem {
        private Integer pagamentoId;
        private Integer pedidoId;
        private String data;
        private Double valor;
        private String metodo;
        private String status;

        public PagamentoTableItem(Integer pagamentoId, Integer pedidoId, String data, Double valor, String metodo,
                String status) {
            this.pagamentoId = pagamentoId;
            this.pedidoId = pedidoId;
            this.data = data;
            this.valor = valor;
            this.metodo = metodo;
            this.status = status;
        }

        public Integer getPagamentoId() {
            return pagamentoId;
        }

        public Integer getPedidoId() {
            return pedidoId;
        }

        public String getData() {
            return data;
        }

        public Double getValor() {
            return valor;
        }

        public String getMetodo() {
            return metodo;
        }

        public String getStatus() {
            return status;
        }
    }
}
