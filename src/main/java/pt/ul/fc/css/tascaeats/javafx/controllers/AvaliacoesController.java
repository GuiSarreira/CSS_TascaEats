package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.scene.text.Text;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para a vista de Avaliações
 * Responsável por listar e gerir avaliações de menus
 */
public class AvaliacoesController {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ENTREGADOR = "ENTREGADOR";

    // FXML Elements
    @FXML
    private TableView<AvaliacaoTableItem> tblAvaliacoes;
    @FXML
    private TableColumn<AvaliacaoTableItem, Integer> colAvaliacaoId;
    @FXML
    private TableColumn<AvaliacaoTableItem, String> colAvaliacaoMenu;
    @FXML
    private TableColumn<AvaliacaoTableItem, String> colAvaliacaoRestaurante;
    @FXML
    private TableColumn<AvaliacaoTableItem, Integer> colAvaliacaoClassificacao;
    @FXML
    private TableColumn<AvaliacaoTableItem, String> colAvaliacaoComentario;
    @FXML
    private TableColumn<AvaliacaoTableItem, String> colAvaliacaoData;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Label lblAvaliacaoStatus;
    @FXML
    private Label lblTotalAvaliacoes;
    @FXML
    private Button btnRecarregarAvaliacoes;

    private TascaEatsGrpcClient grpcClient;

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[AvaliacoesController] Inicializando...");
        setupTableColumns();
        configurarModoLeitura();
        carregarAvaliacoes();
    }

    /**
     * Configurar colunas da tabela
     */
    private void setupTableColumns() {
        tblAvaliacoes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colAvaliacaoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvaliacaoMenu.setCellValueFactory(new PropertyValueFactory<>("menu"));
        colAvaliacaoRestaurante.setCellValueFactory(new PropertyValueFactory<>("restaurante"));
        colAvaliacaoClassificacao.setCellValueFactory(new PropertyValueFactory<>("classificacao"));
        colAvaliacaoComentario.setCellValueFactory(new PropertyValueFactory<>("comentario"));
        colAvaliacaoData.setCellValueFactory(new PropertyValueFactory<>("data"));

        colAvaliacaoComentario.setCellFactory(column -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(12));
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText("");
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
    }

    private void configurarModoLeitura() {
        String role = AuthenticationService.getInstance().getUserType();

        if (ROLE_ADMIN.equals(role)) {
            lblTitulo.setText("Avaliações dos Restaurantes");
            lblSubtitulo.setText(
                    "O administrador pode consultar as avaliações já submetidas na interface web, sem criar, editar ou remover dados.");
            return;
        }

        if (ROLE_ENTREGADOR.equals(role)) {
            lblTitulo.setText("Avaliações dos Restaurantes");
            lblSubtitulo.setText(
                    "O entregador pode apenas consultar as avaliações já submetidas na interface web, sem criar, editar ou remover dados.");
            return;
        }

        lblTitulo.setText("Avaliações dos Restaurantes");
        lblSubtitulo.setText(
                "A interface nativa apresenta as avaliações já feitas. Novas avaliações são submetidas apenas na interface web.");
    }

    /**
     * Carregar avaliações do servidor gRPC
     */
    @FXML
    private void recarregarAvaliacoes() {
        carregarAvaliacoes();
    }

    private void carregarAvaliacoes() {
        lblAvaliacaoStatus.setText("Carregando avaliações...");
        btnRecarregarAvaliacoes.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9091);
                    grpcClient.connect();
                }

                ListarAvaliacoesRequest request = ListarAvaliacoesRequest.newBuilder().build();
                ListarAvaliacoesResponse response = grpcClient.listarAvaliacoes(request);

                Platform.runLater(() -> {
                    try {
                        tblAvaliacoes.getItems().clear();

                        List<AvaliacaoTableItem> items = response.getAvaliacoesList().stream()
                                .map(a -> new AvaliacaoTableItem(
                                        (int) a.getId(),
                                        a.getClienteNome(),
                                        a.getRestauranteNome(),
                                        a.getNota(),
                                        a.getComentario(),
                                        a.getDataAvaliacao()))
                                .collect(Collectors.toList());

                        tblAvaliacoes.getItems().addAll(items);
                        lblTotalAvaliacoes.setText("Total: " + items.size() + " avaliações");
                        lblAvaliacaoStatus.setText(items.isEmpty()
                                ? "Sem avaliações registadas"
                                : items.size() + " avaliações carregadas");
                        btnRecarregarAvaliacoes.setDisable(false);

                    } catch (Exception e) {
                        lblAvaliacaoStatus.setText("Erro ao processar");
                        System.err.println("[AvaliacoesController] Erro UI: " + e.getMessage());
                        btnRecarregarAvaliacoes.setDisable(false);
                    }
                });

            } catch (StatusRuntimeException e) {
                Platform.runLater(() -> {
                    lblAvaliacaoStatus.setText("Erro gRPC: " + e.getStatus().getCode());
                    System.err.println("[AvaliacoesController] gRPC Error: " + e.getStatus());
                    btnRecarregarAvaliacoes.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblAvaliacaoStatus.setText("Erro: " + e.getMessage());
                    System.err.println("[AvaliacoesController] Error: " + e.getMessage());
                    btnRecarregarAvaliacoes.setDisable(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Classe para representar avaliação na tabela
     */
    public static class AvaliacaoTableItem {
        private int id;
        private String menu;
        private String restaurante;
        private int classificacao;
        private String comentario;
        private String data;

        public AvaliacaoTableItem(int id, String menu, String restaurante, int classificacao, String comentario,
                String data) {
            this.id = id;
            this.menu = menu;
            this.restaurante = restaurante;
            this.classificacao = classificacao;
            this.comentario = comentario;
            this.data = data;
        }

        public int getId() {
            return id;
        }

        public String getMenu() {
            return menu;
        }

        public String getRestaurante() {
            return restaurante;
        }

        public int getClassificacao() {
            return classificacao;
        }

        public String getComentario() {
            return comentario;
        }

        public String getData() {
            return data;
        }
    }
}
