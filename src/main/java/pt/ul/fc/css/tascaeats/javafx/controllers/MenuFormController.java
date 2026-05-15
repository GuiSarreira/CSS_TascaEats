package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para dialog de criar/editar menus
 */
public class MenuFormController {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNome;
    @FXML
    private TextArea txtDescricao;
    @FXML
    private ListView<RestauranteOption> lstRestaurantes;
    @FXML
    private ListView<ProdutoOption> lstProdutos;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblErro;

    private Stage stage;
    private TascaEatsGrpcClient grpcClient;
    private Integer menuIdEdicao = null; // null = criar, valor = editar
    private Runnable callbackRefresh;
    private final List<Long> restauranteIdsPreSelecionados = new ArrayList<>();
    private final List<Long> produtoIdsPreSelecionados = new ArrayList<>();

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[MenuFormController] Inicializando...");
        lblErro.setText("");
        lstRestaurantes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstProdutos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Configurar para modo CRIAR
     */
    public void configurarCriar(TascaEatsGrpcClient grpcClient, Runnable refresh) {
        this.grpcClient = grpcClient;
        this.callbackRefresh = refresh;
        this.menuIdEdicao = null;
        this.restauranteIdsPreSelecionados.clear();
        this.produtoIdsPreSelecionados.clear();

        lblTitulo.setText("Criar Novo Menu");
        txtNome.clear();
        txtDescricao.clear();
        lblErro.setText("");
        carregarListasAssociacoes();
    }

    /**
     * Configurar para modo EDITAR
     */
    public void configurarEditar(TascaEatsGrpcClient grpcClient, int menuId, String nome, String descricao,
            List<Long> produtoIds, List<Long> restauranteIds, Runnable refresh) {
        this.grpcClient = grpcClient;
        this.callbackRefresh = refresh;
        this.menuIdEdicao = menuId;
        this.produtoIdsPreSelecionados.clear();
        this.produtoIdsPreSelecionados.addAll(produtoIds != null ? produtoIds : new ArrayList<>());
        this.restauranteIdsPreSelecionados.clear();
        this.restauranteIdsPreSelecionados.addAll(restauranteIds != null ? restauranteIds : new ArrayList<>());

        lblTitulo.setText("Editar Menu");
        txtNome.setText(nome);
        txtDescricao.setText(descricao);
        lblErro.setText("");
        carregarListasAssociacoes();
    }

    private void carregarListasAssociacoes() {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                ListarRestaurantesResponse restaurantesResponse = grpcClient
                        .listarRestaurantes(ListarRestaurantesRequest.newBuilder().build());
                ListarProdutosResponse produtosResponse = grpcClient
                        .listarProdutos(ListarProdutosRequest.newBuilder().build());

                List<RestauranteOption> restaurantes = restaurantesResponse.getRestaurantesList().stream()
                        .map(r -> new RestauranteOption(r.getId(), r.getNome()))
                        .collect(Collectors.toList());

                List<ProdutoOption> produtos = produtosResponse.getProdutosList().stream()
                        .map(p -> new ProdutoOption(p.getId(), p.getNome()))
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    ObservableList<RestauranteOption> restaurantesItems = FXCollections
                            .observableArrayList(restaurantes);
                    lstRestaurantes.setItems(restaurantesItems);

                    ObservableList<ProdutoOption> produtosItems = FXCollections.observableArrayList(produtos);
                    lstProdutos.setItems(produtosItems);

                    if (!restauranteIdsPreSelecionados.isEmpty()) {
                        for (int i = 0; i < restaurantesItems.size(); i++) {
                            if (restauranteIdsPreSelecionados.contains(restaurantesItems.get(i).id)) {
                                lstRestaurantes.getSelectionModel().select(i);
                            }
                        }
                    } else if (!restaurantesItems.isEmpty()) {
                        lstRestaurantes.getSelectionModel().select(0);
                    }

                    if (!produtoIdsPreSelecionados.isEmpty()) {
                        for (int i = 0; i < produtosItems.size(); i++) {
                            if (produtoIdsPreSelecionados.contains(produtosItems.get(i).id)) {
                                lstProdutos.getSelectionModel().select(i);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblErro.setText("Erro ao carregar listas: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Guardar menu (criar ou editar)
     */
    @FXML
    public void guardarMenu() {
        if (!"ADMIN".equalsIgnoreCase(AuthenticationService.getInstance().getUserType())) {
            lblErro.setText("Apenas administradores podem gerir menus");
            return;
        }

        String nome = txtNome.getText().trim();
        String descricao = txtDescricao.getText().trim();

        // ValidaÃ§Ã£o
        if (nome.isEmpty()) {
            lblErro.setText("Nome do menu Ã© obrigatÃ³rio");
            return;
        }

        if (descricao.isEmpty()) {
            lblErro.setText("DescriÃ§Ã£o do menu Ã© obrigatÃ³ria");
            return;
        }

        List<RestauranteOption> restaurantesSelecionados = new ArrayList<>(
                lstRestaurantes.getSelectionModel().getSelectedItems());
        if (restaurantesSelecionados.isEmpty()) {
            lblErro.setText("Selecione pelo menos um restaurante");
            return;
        }

        List<ProdutoOption> produtosSelecionados = new ArrayList<>(lstProdutos.getSelectionModel().getSelectedItems());

        btnGuardar.setDisable(true);
        lblErro.setText("Guardando...");

        if (menuIdEdicao == null) {
            // Criar novo menu
            criarMenu(nome, descricao, restaurantesSelecionados, produtosSelecionados);
        } else {
            // Editar menu existente
            editarMenu(menuIdEdicao, nome, descricao, restaurantesSelecionados, produtosSelecionados);
        }
    }

    /**
     * Criar novo menu via gRPC
     */
    private void criarMenu(String nome, String descricao,
            List<RestauranteOption> restaurantesSelecionados,
            List<ProdutoOption> produtosSelecionados) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                CriarMenuRequest request = CriarMenuRequest.newBuilder()
                        .setNome(nome)
                        .setDescricao(descricao)
                        .build();

                CriarMenuRequest.Builder requestBuilder = request.toBuilder();
                for (RestauranteOption restaurante : restaurantesSelecionados) {
                    requestBuilder.addRestauranteIds(restaurante.id);
                }
                for (ProdutoOption produto : produtosSelecionados) {
                    requestBuilder.addProdutoIds(produto.id);
                }

                grpcClient.criarMenu(requestBuilder.build());

                Platform.runLater(() -> {
                    lblErro.setText("Menu criado com sucesso!");

                    // Fechar dialog apÃ³s 1 segundo
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Platform.runLater(() -> {
                                if (callbackRefresh != null) {
                                    callbackRefresh.run();
                                }
                                cancelar();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblErro.setText("Erro gRPC: " + e.getStatus().getCode());
                    btnGuardar.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblErro.setText("Erro: " + e.getMessage());
                    btnGuardar.setDisable(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Editar menu existente via gRPC
     */
    private void editarMenu(int menuId, String nome, String descricao,
            List<RestauranteOption> restaurantesSelecionados,
            List<ProdutoOption> produtosSelecionados) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                AtualizarMenuRequest request = AtualizarMenuRequest.newBuilder()
                        .setMenuId(menuId)
                        .setNome(nome)
                        .setDescricao(descricao)
                        .build();

                AtualizarMenuRequest.Builder requestBuilder = request.toBuilder();
                for (RestauranteOption restaurante : restaurantesSelecionados) {
                    requestBuilder.addRestauranteIds(restaurante.id);
                }
                for (ProdutoOption produto : produtosSelecionados) {
                    requestBuilder.addProdutoIds(produto.id);
                }

                grpcClient.atualizarMenu(requestBuilder.build());

                Platform.runLater(() -> {
                    lblErro.setText("Menu atualizado com sucesso!");

                    // Fechar dialog apÃ³s 1 segundo
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Platform.runLater(() -> {
                                if (callbackRefresh != null) {
                                    callbackRefresh.run();
                                }
                                cancelar();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblErro.setText("Erro gRPC: " + e.getStatus().getCode());
                    btnGuardar.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblErro.setText("Erro: " + e.getMessage());
                    btnGuardar.setDisable(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Cancelar dialog
     */
    @FXML
    public void cancelar() {
        stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public static class RestauranteOption {
        private final long id;
        private final String nome;

        public RestauranteOption(long id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    public static class ProdutoOption {
        private final long id;
        private final String nome;

        public ProdutoOption(long id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }
}
