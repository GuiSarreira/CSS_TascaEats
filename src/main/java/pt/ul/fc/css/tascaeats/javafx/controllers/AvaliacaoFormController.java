package pt.ul.fc.css.tascaeats.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import pt.ul.fc.css.tascaeats.javafx.auth.AuthenticationService;
import pt.ul.fc.css.tascaeats.javafx.grpc.TascaEatsGrpcClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para dialog de criar/editar avaliaÃ§Ãµes
 */
public class AvaliacaoFormController {

    @FXML
    private Label lblTitulo;
    @FXML
    private ComboBox<RestauranteComboItem> cmbRestaurante;
    @FXML
    private Slider sldrClassificacao;
    @FXML
    private Label lblClassificacao;
    @FXML
    private TextArea txtComentario;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblErro;

    private Stage stage;
    private TascaEatsGrpcClient grpcClient;
    private Integer avaliacaoIdEdicao = null;
    private Runnable callbackRefresh;

    public static class RestauranteComboItem {
        public int id;
        public String nome;

        public RestauranteComboItem(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    /**
     * Inicializar controller
     */
    @FXML
    public void initialize() {
        System.out.println("[AvaliacaoFormController] Inicializando...");
        lblErro.setText("");

        // Listener para slider de classificaÃ§Ã£o
        sldrClassificacao.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int valor = newValue.intValue();
                lblClassificacao.setText(valor + "/5");

                // Mudar cor baseado na classificaÃ§Ã£o
                if (valor <= 2) {
                    lblClassificacao.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                } else if (valor <= 3) {
                    lblClassificacao.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff9800;");
                } else {
                    lblClassificacao.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                }
            }
        });
    }

    /**
     * Configurar para modo CRIAR
     */
    public void configurarCriar(TascaEatsGrpcClient grpcClient, Runnable refresh) {
        this.grpcClient = grpcClient;
        this.callbackRefresh = refresh;
        this.avaliacaoIdEdicao = null;

        lblTitulo.setText("Nova AvaliaÃ§Ã£o");
        carregarRestaurantes();
    }

    /**
     * Configurar para modo EDITAR
     */
    public void configurarEditar(TascaEatsGrpcClient grpcClient, int avaliacaoId,
            int classificacao, String comentario, Runnable refresh) {
        this.grpcClient = grpcClient;
        this.callbackRefresh = refresh;
        this.avaliacaoIdEdicao = avaliacaoId;

        lblTitulo.setText("Editar AvaliaÃ§Ã£o");
        sldrClassificacao.setValue(classificacao);
        txtComentario.setText(comentario);

        carregarRestaurantes();
    }

    /**
     * Configurar para criar avaliaÃ§Ã£o de restaurante especÃ­fico
     * (prÃ©-selecionado)
     */
    public void configurarAvaliacao(TascaEatsGrpcClient grpcClient, int restauranteId, String restauranteNome,
            Runnable refresh) {
        this.grpcClient = grpcClient;
        this.callbackRefresh = refresh;
        this.avaliacaoIdEdicao = null;

        lblTitulo.setText("Avaliar: " + restauranteNome);
        carregarRestaurantesComPrescricao(restauranteId);
    }

    /**
     * Carregar restaurantes para o ComboBox
     */
    private void carregarRestaurantes() {
        carregarRestaurantesComPrescricao(-1);
    }

    /**
     * Carregar restaurantes para o ComboBox com opÃ§Ã£o de prÃ©-selecionar um
     * especÃ­fico
     */
    private void carregarRestaurantesComPrescricao(int restauranteIdPrescrito) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                ListarRestaurantesRequest request = ListarRestaurantesRequest.newBuilder().build();
                ListarRestaurantesResponse response = grpcClient.listarRestaurantes(request);

                Platform.runLater(() -> {
                    List<RestauranteComboItem> items = response.getRestaurantesList().stream()
                            .map(r -> new RestauranteComboItem((int) r.getId(), r.getNome()))
                            .collect(Collectors.toList());

                    ObservableList<RestauranteComboItem> observableItems = FXCollections.observableArrayList(items);
                    cmbRestaurante.setItems(observableItems);

                    if (!items.isEmpty()) {
                        // Se hÃ¡ restaurante prÃ©-prescrito, selecionar esse
                        if (restauranteIdPrescrito > 0) {
                            for (RestauranteComboItem item : items) {
                                if (item.id == restauranteIdPrescrito) {
                                    cmbRestaurante.getSelectionModel().select(item);
                                    cmbRestaurante.setDisable(true); // Desabilitar mudanÃ§as
                                    break;
                                }
                            }
                        } else {
                            cmbRestaurante.getSelectionModel().selectFirst();
                        }
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblErro.setText("Erro ao carregar restaurantes: " + e.getMessage());
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Guardar avaliaÃ§Ã£o (criar ou editar)
     */
    @FXML
    public void guardarAvaliacao() {
        RestauranteComboItem selectedRestaurante = cmbRestaurante.getSelectionModel().getSelectedItem();
        String comentario = txtComentario.getText().trim();
        int classificacao = (int) sldrClassificacao.getValue();

        // ValidaÃ§Ã£o
        if (selectedRestaurante == null) {
            lblErro.setText("Selecione um restaurante");
            return;
        }

        if (comentario.isEmpty()) {
            lblErro.setText("ComentÃ¡rio Ã© obrigatÃ³rio");
            return;
        }

        btnGuardar.setDisable(true);
        lblErro.setText("Guardando...");

        if (avaliacaoIdEdicao == null) {
            // Criar nova avaliaÃ§Ã£o
            criarAvaliacao(selectedRestaurante.id, classificacao, comentario);
        } else {
            // Editar avaliaÃ§Ã£o existente
            editarAvaliacao(avaliacaoIdEdicao, classificacao, comentario);
        }
    }

    /**
     * Criar nova avaliaÃ§Ã£o via gRPC
     */
    private void criarAvaliacao(int restauranteId, int classificacao, String comentario) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                Long clienteId = resolverClienteIdDaSessao();
                if (clienteId == null) {
                    Platform.runLater(() -> {
                        lblErro.setText("Cliente autenticado nÃ£o encontrado no backend");
                        btnGuardar.setDisable(false);
                    });
                    return;
                }

                CriarAvaliacaoRequest request = CriarAvaliacaoRequest.newBuilder()
                        .setClienteId(clienteId)
                        .setRestauranteId(restauranteId)
                        .setNota(classificacao)
                        .setComentario(comentario)
                        .build();

                grpcClient.criarAvaliacao(request);

                Platform.runLater(() -> {
                    lblErro.setText("AvaliaÃ§Ã£o criada com sucesso!");

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
     * Editar avaliaÃ§Ã£o existente via gRPC
     */
    private void editarAvaliacao(int avaliacaoId, int classificacao, String comentario) {
        Thread thread = new Thread(() -> {
            try {
                if (grpcClient == null) {
                    grpcClient = new TascaEatsGrpcClient("localhost", 9092);
                    grpcClient.connect();
                }

                Long clienteId = resolverClienteIdDaSessao();
                if (clienteId == null) {
                    Platform.runLater(() -> {
                        lblErro.setText("Cliente autenticado nÃ£o encontrado no backend");
                        btnGuardar.setDisable(false);
                    });
                    return;
                }

                AtualizarAvaliacaoRequest request = AtualizarAvaliacaoRequest.newBuilder()
                        .setAvaliacaoId(avaliacaoId)
                        .setNota(classificacao)
                        .setComentario(comentario)
                        .setClienteId(clienteId)
                        .build();

                grpcClient.atualizarAvaliacao(request);

                Platform.runLater(() -> {
                    lblErro.setText("AvaliaÃ§Ã£o atualizada com sucesso!");

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

    private Long resolverClienteIdDaSessao() {
        AuthenticationService authService = AuthenticationService.getInstance();
        AuthenticationService.CurrentUser currentUser = authService.getCurrentUser();
        if (currentUser == null || currentUser.email == null || currentUser.email.isBlank()) {
            return null;
        }

        ListarUsersResponse response = grpcClient.listarUsers(
                ListarUsersRequest.newBuilder().setTipo("CLIENTE").build());

        return response.getUsersList().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(currentUser.email))
                .map(UserInfo::getId)
                .findFirst()
                .orElseGet(() -> response.getUsersList().stream()
                        .map(UserInfo::getId)
                        .findFirst()
                        .orElse(null));
    }

    /**
     * Cancelar dialog
     */
    @FXML
    public void cancelar() {
        stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
