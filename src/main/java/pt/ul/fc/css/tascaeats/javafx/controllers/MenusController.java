package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.application.Platform;
import javafx.stage.Stage;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para a vista de Menus
 * ResponsÃ¡vel por listar e gerir menus de restaurantes
 */
public class MenusController {

    // FXML Elements
    @FXML
    private TextField txtFiltroNomeMenu;
    @FXML
    private TextField txtFiltroMinProdutos;
    @FXML
    private TextField txtFiltroMaxProdutos;
    @FXML
    private TextField txtFiltroMinPrecoMedio;
    @FXML
    private TextField txtFiltroMaxPrecoMedio;
    @FXML
    private TableView<MenuTableItem> tblMenus;
    @FXML
    private TableColumn<MenuTableItem, Integer> colMenuId;
    @FXML
    private TableColumn<MenuTableItem, String> colMenuNome;
    @FXML
    private TableColumn<MenuTableItem, String> colMenuDescricao;
    @FXML
    private TableColumn<MenuTableItem, Double> colMenuPreco;
    @FXML
    private TableColumn<MenuTableItem, Integer> colMenuNumProdutos;
    @FXML
    private TableColumn<MenuTableItem, String> colMenuRestaurante;
    @FXML
    private TableColumn<MenuTableItem, Void> colMenuAcoes;

    @FXML
    private Label lblMenuStatus;
    @FXML
    private Label lblTotalMenus;
    @FXML
    private Button btnRecarregarMenus;
    @FXML
    private Button btnNovoMenu;

    private TascaEatsGrpcClient grpcClient;
    private boolean podeGerirMenus;

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[MenusController] Inicializando...");
        podeGerirMenus = "ADMIN".equalsIgnoreCase(AuthenticationService.getInstance().getUserType());
        setupTableColumns();
        aplicarPermissoesPorPerfil();
        carregarMenus();
    }

    private void aplicarPermissoesPorPerfil() {
        btnNovoMenu.setDisable(!podeGerirMenus);
        btnNovoMenu.setVisible(podeGerirMenus);
        btnNovoMenu.setManaged(podeGerirMenus);
    }

    /**
     * Pre-selecionar um restaurante especifico (usado quando navegado de
     * RestaurantesController)
     */
    public void presetarRestaurante(int restauranteId) {
        carregarMenus();
    }

    /**
     * Configurar colunas da tabela
     */
    private void setupTableColumns() {
        tblMenus.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colMenuId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMenuNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMenuDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colMenuPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colMenuNumProdutos.setCellValueFactory(new PropertyValueFactory<>("numeroProdutos"));
        colMenuRestaurante.setCellValueFactory(new PropertyValueFactory<>("restaurante"));

        // Adicionar botoes de acao
        colMenuAcoes.setCellFactory(param -> new TableCell<MenuTableItem, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnRemover = new Button("Remover");

            {
                btnEditar.setStyle("-fx-font-size: 10; -fx-padding: 5 10;");
                btnRemover.setStyle("-fx-font-size: 10; -fx-padding: 5 10; -fx-text-fill: #d32f2f;");

                btnEditar.setVisible(podeGerirMenus);
                btnEditar.setManaged(podeGerirMenus);
                btnRemover.setVisible(podeGerirMenus);
                btnRemover.setManaged(podeGerirMenus);

                btnEditar.setOnAction(event -> {
                    MenuTableItem item = getTableView().getItems().get(getIndex());
                    editarMenu(item.getId(), item.getNome());
                });

                btnRemover.setOnAction(event -> {
                    MenuTableItem item = getTableView().getItems().get(getIndex());
                    removerMenu(item.getId(), item.getNome());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, btnEditar, btnRemover));
            }
        });
    }

    /**
     * Carregar restaurantes para o ComboBox
     */
    private void carregarMenus() {

        lblMenuStatus.setText("Carregando menus...");
        btnRecarregarMenus.setDisable(true);

        String filtroNome = txtFiltroNomeMenu != null ? txtFiltroNomeMenu.getText().trim() : "";
        Integer minProdutos = parseOptionalInteger(txtFiltroMinProdutos != null ? txtFiltroMinProdutos.getText() : "",
                "min produtos");
        if (minProdutos == null && txtFiltroMinProdutos != null && !txtFiltroMinProdutos.getText().trim().isEmpty()) {
            btnRecarregarMenus.setDisable(false);
            return;
        }

        Integer maxProdutos = parseOptionalInteger(txtFiltroMaxProdutos != null ? txtFiltroMaxProdutos.getText() : "",
                "max produtos");
        if (maxProdutos == null && txtFiltroMaxProdutos != null && !txtFiltroMaxProdutos.getText().trim().isEmpty()) {
            btnRecarregarMenus.setDisable(false);
            return;
        }

        Double minPrecoMedio = parseOptionalDouble(
                txtFiltroMinPrecoMedio != null ? txtFiltroMinPrecoMedio.getText() : "",
                "preco medio min");
        if (minPrecoMedio == null && txtFiltroMinPrecoMedio != null
                && !txtFiltroMinPrecoMedio.getText().trim().isEmpty()) {
            btnRecarregarMenus.setDisable(false);
            return;
        }

        Double maxPrecoMedio = parseOptionalDouble(
                txtFiltroMaxPrecoMedio != null ? txtFiltroMaxPrecoMedio.getText() : "",
                "preco medio max");
        if (maxPrecoMedio == null && txtFiltroMaxPrecoMedio != null
                && !txtFiltroMaxPrecoMedio.getText().trim().isEmpty()) {
            btnRecarregarMenus.setDisable(false);
            return;
        }

        if (minProdutos != null && maxProdutos != null && minProdutos > maxProdutos) {
            lblMenuStatus.setText("Filtro invalido: min produtos maior que max produtos");
            btnRecarregarMenus.setDisable(false);
            return;
        }

        if (minPrecoMedio != null && maxPrecoMedio != null && minPrecoMedio > maxPrecoMedio) {
            lblMenuStatus.setText("Filtro invalido: preco medio min maior que max");
            btnRecarregarMenus.setDisable(false);
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                ListarMenusRequest.Builder requestBuilder = ListarMenusRequest.newBuilder();
                if (!filtroNome.isBlank()) {
                    requestBuilder.setNome(filtroNome);
                }
                if (minProdutos != null) {
                    requestBuilder.setMinProdutos(minProdutos);
                }
                if (maxProdutos != null) {
                    requestBuilder.setMaxProdutos(maxProdutos);
                }
                if (minPrecoMedio != null) {
                    requestBuilder.setMinPreco(minPrecoMedio);
                }
                if (maxPrecoMedio != null) {
                    requestBuilder.setMaxPreco(maxPrecoMedio);
                }

                ListarMenusRequest request = requestBuilder.build();

                ListarMenusResponse response = grpcClient.listarMenus(request);

                Platform.runLater(() -> {
                    try {
                        tblMenus.getItems().clear();

                        List<MenuTableItem> items = response.getMenusList().stream()
                                .map(m -> new MenuTableItem(
                                        (int) m.getId(),
                                        m.getNome(),
                                        m.getDescricao(),
                                        m.getProdutosList().stream()
                                                .mapToDouble(ProdutoInfo::getPreco)
                                                .average()
                                                .orElse(0.0),
                                        m.getProdutosList().size(),
                                        m.getRestaurantesList().isEmpty()
                                                ? "Sem restaurante"
                                                : m.getRestaurantesList().stream()
                                                        .map(RestauranteInfo::getNome)
                                                        .collect(Collectors.joining(", "))))
                                .collect(Collectors.toList());

                        tblMenus.getItems().addAll(items);
                        lblTotalMenus.setText("Total: " + items.size() + " menus");
                        lblMenuStatus.setText(items.size() + " menus carregados");
                        btnRecarregarMenus.setDisable(false);

                    } catch (Exception e) {
                        lblMenuStatus.setText("Erro ao processar");
                        System.err.println("[MenusController] Erro UI: " + e.getMessage());
                        btnRecarregarMenus.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblMenuStatus.setText("Erro: " + e.getMessage());
                    System.err.println("[MenusController] Error: " + e.getMessage());
                    btnRecarregarMenus.setDisable(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private Integer parseOptionalInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                lblMenuStatus.setText("Valor invalido para " + fieldName + " (deve ser >= 0)");
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            lblMenuStatus.setText("Valor invalido para " + fieldName);
            return null;
        }
    }

    private Double parseOptionalDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            lblMenuStatus.setText("Valor invalido para " + fieldName);
            return null;
        }
    }

    /**
     * Recarregar menus
     */
    @FXML
    public void recarregarMenus() {
        carregarMenus();
    }

    /**
     * Criar novo menu
     */
    @FXML
    public void novoMenu() {
        if (!podeGerirMenus) {
            mostrarAcessoNegadoMenus();
            return;
        }

        abrirMenuFormDialog(null, null, null, null, null);
    }

    /**
     * Abrir dialog para criar/editar menu
     */
    private void abrirMenuFormDialog(Integer menuId, String menuNome, String menuDescricao,
            List<Long> produtoIdsEdicao,
            List<Long> restauranteIdsEdicao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menuForm.fxml"));
            Parent root = loader.load();
            MenuFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(menuId == null ? "Criar Menu" : "Editar Menu");
            stage.setScene(new Scene(root, 500, 400));

            if (menuId == null) {
                // Modo CRIAR
                controller.configurarCriar(grpcClient, this::recarregarMenus);
            } else {
                // Modo EDITAR
                controller.configurarEditar(grpcClient, menuId, menuNome, menuDescricao,
                        produtoIdsEdicao != null ? produtoIdsEdicao : new ArrayList<>(),
                        restauranteIdsEdicao != null ? restauranteIdsEdicao : new ArrayList<>(),
                        this::recarregarMenus);
            }

            stage.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir formulario");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.err.println("[MenusController] Erro ao carregar menuForm.fxml: " + e.getMessage());
        }
    }

    /**
     * Editar menu
     */
    private void editarMenu(int menuId, String menuNome) {
        if (!podeGerirMenus) {
            mostrarAcessoNegadoMenus();
            return;
        }

        System.out.println("[MenusController] Editar menu: " + menuNome);

        lblMenuStatus.setText("Carregando dados do menu...");

        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                ObterMenuRequest request = ObterMenuRequest.newBuilder()
                        .setMenuId(menuId)
                        .build();

                MenuResponse response = grpcClient.obterMenu(request);

                Platform.runLater(() -> {
                    List<Long> produtoIds = response.getProdutosList().stream()
                            .map(ProdutoInfo::getId)
                            .collect(Collectors.toList());
                    List<Long> restauranteIds = response.getRestaurantesList().stream()
                            .map(RestauranteInfo::getId)
                            .collect(Collectors.toList());

                    abrirMenuFormDialog(menuId, response.getNome(), response.getDescricao(),
                            produtoIds, restauranteIds);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblMenuStatus.setText("Erro ao obter menu: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Adicionar ao carrinho
     */
    private void adicionarAoCarrinho(int menuId, String menuNome) {
        System.out.println("[MenusController] Adicionar ao carrinho: " + menuNome);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Carrinho");
        alert.setHeaderText("Adicionar ao carrinho");
        alert.setContentText(menuNome + " foi adicionado ao carrinho!");
        alert.showAndWait();
    }

    /**
     * Remover menu
     */
    private void removerMenu(int menuId, String menuNome) {
        if (!podeGerirMenus) {
            mostrarAcessoNegadoMenus();
            return;
        }

        System.out.println("[MenusController] Remover menu: " + menuNome);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remocao");
        alert.setHeaderText("Remover menu");
        alert.setContentText("Tem certeza que deseja remover este menu?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            lblMenuStatus.setText("Removendo menu...");
            removerMenuViaGRPC(menuId, menuNome);
        }
    }

    /**
     * Remover menu via gRPC
     */
    private void removerMenuViaGRPC(int menuId, String menuNome) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                RemoverMenuRequest request = RemoverMenuRequest.newBuilder()
                        .setMenuId(menuId)
                        .build();

                grpcClient.removerMenu(request);

                Platform.runLater(() -> {
                    lblMenuStatus.setText("Menu removido com sucesso!");
                    recarregarMenus();
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblMenuStatus.setText("Erro gRPC: " + e.getStatus().getCode());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblMenuStatus.setText("Erro: " + e.getMessage());
                    System.err.println("[MenusController] Erro ao remover: " + e.getMessage());
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void mostrarAcessoNegadoMenus() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso negado");
        alert.setHeaderText("Gestao de menus indisponivel");
        alert.setContentText("Apenas administradores podem criar, editar ou remover menus na interface nativa.");
        alert.showAndWait();
    }

    /**
     * Classe para representar menu na tabela
     */
    public static class MenuTableItem {
        private int id;
        private String nome;
        private String descricao;
        private double preco;
        private int numeroProdutos;
        private String restaurante;

        public MenuTableItem(int id, String nome, String descricao, double preco, int numeroProdutos,
                String restaurante) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.preco = preco;
            this.numeroProdutos = numeroProdutos;
            this.restaurante = restaurante;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public double getPreco() {
            return preco;
        }

        public int getNumeroProdutos() {
            return numeroProdutos;
        }

        public String getRestaurante() {
            return restaurante;
        }
    }
}
