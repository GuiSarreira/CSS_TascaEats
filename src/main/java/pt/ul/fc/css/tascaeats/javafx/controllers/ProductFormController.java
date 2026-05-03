package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;
import pt.ul.fc.css.tascaeats.grpc.CriarProdutoRequest;
import pt.ul.fc.css.tascaeats.grpc.AtualizarProdutoRequest;

/**
 * Controller para criar/editar produtos
 */
public class ProductFormController {

    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtPrice;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private CheckBox checkAvailable;

    @FXML
    private Label lblStatus;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private TascaEatsGrpcClient grpcClient;
    private Integer productId; // null = novo

    @FXML
    public void initialize() {
        System.out.println("[ProductFormController] Inicializando...");

        comboCategory.getItems().addAll("ENTRADA", "PRATO_PRINCIPAL", "SOBREMESA", "BEBIDA");
        comboCategory.setValue("PRATO_PRINCIPAL");
        checkAvailable.setSelected(true);
    }

    public void setMode(ProductController.ProductTableItem product) {
        if (product != null) {
            this.productId = product.getProductId();
            txtName.setText(product.getName());
            txtPrice.setText(product.getPrice().toString());
            comboCategory.setValue(product.getCategory());
            checkAvailable.setSelected(product.isAvailable());
            lblStatus.setText("A editar produto: " + product.getName());
        } else {
            lblStatus.setText("Novo produto");
        }
    }

    @FXML
    private void save() {
        String name = txtName.getText();
        String description = txtDescription.getText();
        String price = txtPrice.getText();
        String category = comboCategory.getValue();
        boolean available = checkAvailable.isSelected();

        if (name.isEmpty() || price.isEmpty()) {
            lblStatus.setText("Preencha nome e preço");
            return;
        }

        lblStatus.setText("A guardar produto...");

        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                double priceValue;
                try {
                    priceValue = Double.parseDouble(price.trim().replace(',', '.'));
                } catch (NumberFormatException ex) {
                    Platform.runLater(() -> lblStatus.setText("Preco invalido"));
                    return;
                }

                if (productId == null) {
                    // Criar novo produto
                    CriarProdutoRequest request = CriarProdutoRequest.newBuilder()
                            .setNome(name)
                            .setDescricao(description)
                            .setPreco(priceValue)
                            .setCategoria(category)
                            .setDisponivel(available)
                            .build();
                    grpcClient.criarProduto(request);
                } else {
                    // Atualizar produto existente
                    AtualizarProdutoRequest request = AtualizarProdutoRequest.newBuilder()
                            .setProdutoId(productId)
                            .setNome(name)
                            .setDescricao(description)
                            .setPreco(priceValue)
                            .setCategoria(category)
                            .setDisponivel(available)
                            .build();
                    grpcClient.atualizarProduto(request);
                }

                Platform.runLater(() -> {
                    lblStatus.setText("Produto '" + name + "' guardado com sucesso!");
                    cancel();
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Erro: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
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
        txtDescription.clear();
        txtPrice.clear();
        checkAvailable.setSelected(true);
    }

    /**
     * Static method to open ProductForm dialog
     */
    public static void openProductForm(ProductController.ProductTableItem product) throws Exception {
        FXMLLoader loader = new FXMLLoader(ProductFormController.class.getResource("/fxml/productForm.fxml"));
        Parent root = loader.load();
        ProductFormController controller = loader.getController();
        controller.setMode(product);

        Stage stage = new Stage();
        stage.setTitle(product == null ? "Novo Produto" : "Editar Produto");
        stage.setScene(new Scene(root, 640, 520));
        stage.showAndWait();

        if (product != null) {
            System.out.println("[ProductFormController] Opening form to edit product: " + product.getName());
        } else {
            System.out.println("[ProductFormController] Opening form to create new product");
        }
    }
}
