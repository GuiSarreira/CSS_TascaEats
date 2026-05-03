package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import pt.ul.fc.css.tascaeats.grpc.ListarProdutosRequest;
import pt.ul.fc.css.tascaeats.grpc.ListarProdutosResponse;
import pt.ul.fc.css.tascaeats.grpc.ProdutoResponse;
import pt.ul.fc.css.tascaeats.grpc.RemoverProdutoRequest;
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

/**
 * Controller para gerir Produtos (Requisito H)
 */
public class ProductController {

    private static final String PRODUTOS_FILTROS_URL = "http://localhost:8081/api/restaurantes/1/produtos/filtros";
    private static final String PRODUTOS_BASE_URL = "http://localhost:8081/api/restaurantes/1/produtos";

    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private TextField txtFilterName;
    @FXML
    private TextField txtMinPrice;
    @FXML
    private TextField txtMaxPrice;
    @FXML
    private ComboBox<String> comboAvailability;
    @FXML
    private TextField txtMinPopularidade;
    @FXML
    private TableView<ProductTableItem> tblProducts;
    @FXML
    private TableColumn<ProductTableItem, Integer> colProductId;
    @FXML
    private TableColumn<ProductTableItem, String> colProductName;
    @FXML
    private TableColumn<ProductTableItem, String> colProductCategory;
    @FXML
    private TableColumn<ProductTableItem, Double> colProductPrice;
    @FXML
    private TableColumn<ProductTableItem, String> colProductAvailable;
    @FXML
    private TableColumn<ProductTableItem, Void> colProductActions;

    @FXML
    private Label lblStatus;
    @FXML
    private Button btnNewProduct;
    @FXML
    private Button btnRefresh;

    private TascaEatsGrpcClient grpcClient;
    private boolean podeGerirProdutos;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("[ProductController] Inicializando...");
        podeGerirProdutos = "ADMIN".equalsIgnoreCase(AuthenticationService.getInstance().getUserType());

        ObservableList<String> categorias = FXCollections.observableArrayList(
                "Todas", "Entrada", "Prato Principal", "Sobremesa", "Bebida");
        comboCategory.setItems(categorias);
        comboCategory.setValue("Todas");

        comboAvailability.setItems(FXCollections.observableArrayList("Todas", "Disponivel", "Indisponivel"));
        comboAvailability.setValue("Todas");

        if (btnNewProduct != null) {
            btnNewProduct.setDisable(!podeGerirProdutos);
            btnNewProduct.setVisible(podeGerirProdutos);
            btnNewProduct.setManaged(podeGerirProdutos);
        }

        setupTableColumns();
        loadProducts();
    }

    private void setupTableColumns() {
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProductCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colProductPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProductAvailable.setCellValueFactory(new PropertyValueFactory<>("availableLabel"));

        colProductActions.setCellFactory(param -> new TableCell<ProductTableItem, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnRemover = new Button("Remover");

            {
                btnEditar.setStyle("-fx-font-size: 10; -fx-padding: 5 10;");
                btnRemover.setStyle("-fx-font-size: 10; -fx-padding: 5 10; -fx-text-fill: #d32f2f;");

                btnEditar.setOnAction(event -> {
                    ProductTableItem selected = getTableView().getItems().get(getIndex());
                    editSelectedProduct(selected);
                });

                btnRemover.setOnAction(event -> {
                    ProductTableItem selected = getTableView().getItems().get(getIndex());
                    deleteSelectedProduct(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                if (!podeGerirProdutos) {
                    setGraphic(null);
                    return;
                }

                setGraphic(new HBox(6, btnEditar, btnRemover));
            }
        });
    }

    @FXML
    private void loadProducts() {
        Thread thread = new Thread(() -> {
            try {
                ObservableList<ProductTableItem> products = loadProductsByFilters();

                Platform.runLater(() -> {
                    tblProducts.setItems(products);
                    lblStatus.setText(products.size() + " produtos carregados");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro ao carregar produtos: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<ProductTableItem> loadProductsByFilters() throws Exception {
        String nome = txtFilterName == null ? "" : txtFilterName.getText().trim();
        String categoria = comboCategory == null ? "Todas" : comboCategory.getValue();
        String disponibilidade = comboAvailability == null ? "Todas" : comboAvailability.getValue();

        Double minPrice = parseOptionalDouble(txtMinPrice == null ? "" : txtMinPrice.getText(), "preco min");
        Double maxPrice = parseOptionalDouble(txtMaxPrice == null ? "" : txtMaxPrice.getText(), "preco max");
        Integer minPopularidade = parseOptionalInteger(
                txtMinPopularidade == null ? "" : txtMinPopularidade.getText(),
                "min popularidade");

        List<String> params = new ArrayList<>();
        if (!nome.isBlank()) {
            params.add("nome=" + encode(nome));
        }
        if (minPrice != null) {
            params.add("precoMin=" + minPrice);
        }
        if (maxPrice != null) {
            params.add("precoMax=" + maxPrice);
        }
        if (categoria != null && !categoria.isBlank() && !"Todas".equalsIgnoreCase(categoria)) {
            params.add("categoria=" + encode(categoria.trim()));
        }
        if (disponibilidade != null && !"Todas".equalsIgnoreCase(disponibilidade)) {
            params.add("disponivel=" + ("Disponivel".equalsIgnoreCase(disponibilidade)));
        }
        if (minPopularidade != null) {
            params.add("minPopularidade=" + minPopularidade);
        }

        String url = PRODUTOS_FILTROS_URL + (params.isEmpty() ? "" : "?" + String.join("&", params));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            ObservableList<ProductTableItem> items = FXCollections.observableArrayList();
            JsonNode root = objectMapper.readTree(response.body());
            if (root.isArray()) {
                for (JsonNode p : root) {
                    items.add(new ProductTableItem(
                            p.path("id").asInt(),
                            p.path("nome").asText(""),
                            p.path("categoria").asText(""),
                            p.path("preco").asDouble(0.0),
                            p.path("disponivel").asBoolean(true)));
                }
            }
            return items;
        }

        if (grpcClient == null) {
            grpcClient = new TascaEatsGrpcClient("localhost", 9091);
            grpcClient.connect();
        }

        ListarProdutosRequest grpcRequest = ListarProdutosRequest.newBuilder().build();
        ListarProdutosResponse grpcResponse = grpcClient.listarProdutos(grpcRequest);
        ObservableList<ProductTableItem> products = FXCollections.observableArrayList();
        for (ProdutoResponse p : grpcResponse.getProdutosList()) {
            products.add(new ProductTableItem(
                    (int) p.getId(),
                    p.getNome(),
                    p.getCategoria(),
                    p.getPreco(),
                    p.getDisponivel()));
        }
        return products;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Double parseOptionalDouble(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            Platform.runLater(() -> lblStatus.setText("Valor invalido para " + field));
            return null;
        }
    }

    private Integer parseOptionalInteger(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            Platform.runLater(() -> lblStatus.setText("Valor invalido para " + field));
            return null;
        }
    }

    @FXML
    private void newProduct() {
        if (!podeGerirProdutos) {
            lblStatus.setText("Apenas administradores podem criar produtos");
            return;
        }

        lblStatus.setText("Criar novo produto");
        try {
            ProductFormController.openProductForm(null);
            refresh();
        } catch (Exception e) {
            lblStatus.setText("Erro ao abrir formulario: " + e.getMessage());
        }
    }

    @FXML
    private void editProduct() {
        ProductTableItem selected = tblProducts.getSelectionModel().getSelectedItem();
        editSelectedProduct(selected);
    }

    @FXML
    private void deleteProduct() {
        ProductTableItem selected = tblProducts.getSelectionModel().getSelectedItem();
        deleteSelectedProduct(selected);
    }

    private void editSelectedProduct(ProductTableItem selected) {
        if (!podeGerirProdutos) {
            lblStatus.setText("Apenas administradores podem editar produtos");
            return;
        }

        if (selected == null) {
            lblStatus.setText("Selecione um produto");
            return;
        }

        lblStatus.setText("A editar: " + selected.getName());
        try {
            ProductFormController.openProductForm(selected);
            refresh();
        } catch (Exception e) {
            lblStatus.setText("Erro ao abrir formulario: " + e.getMessage());
        }
    }

    private void deleteSelectedProduct(ProductTableItem selected) {
        if (!podeGerirProdutos) {
            lblStatus.setText("Apenas administradores podem remover produtos");
            return;
        }

        if (selected == null) {
            lblStatus.setText("Selecione um produto");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover " + selected.getName() + "?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            lblStatus.setText("A remover produto...");
            Thread thread = new Thread(() -> {
                try {
                    if (grpcClient == null) {
                        grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                        grpcClient.connect();
                    }

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(PRODUTOS_BASE_URL + "/" + selected.getProductId()))
                            .DELETE()
                            .timeout(Duration.ofSeconds(8))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        RemoverProdutoRequest grpcRequest = RemoverProdutoRequest.newBuilder()
                                .setProdutoId(selected.getProductId())
                                .build();
                        grpcClient.removerProduto(grpcRequest);
                    }

                    Platform.runLater(() -> {
                        lblStatus.setText("Produto removido com sucesso!");
                        refresh();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> lblStatus.setText("Erro ao remover: " + e.getMessage()));
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    private void refresh() {
        loadProducts();
    }

    @FXML
    private void clearFilters() {
        txtFilterName.clear();
        txtMinPrice.clear();
        txtMaxPrice.clear();
        comboCategory.setValue("Todas");
        comboAvailability.setValue("Todas");
        txtMinPopularidade.clear();
        loadProducts();
    }

    public static class ProductTableItem {
        private Integer productId;
        private String name;
        private String category;
        private Double price;
        private boolean available;

        public ProductTableItem(Integer productId, String name, String category, Double price, boolean available) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.available = available;
        }

        public Integer getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public Double getPrice() {
            return price;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getAvailableLabel() {
            return available ? "Sim" : "Nao";
        }
    }
}
