package pt.ul.fc.css.tascaeats.javafx.grpc;

import io.grpc.stub.StreamObserver;
import pt.ul.fc.css.tascaeats.grpc.*;

import java.util.function.Consumer;

/**
 * Extensões assíncronas para TascaEatsGrpcClient
 * 
 * Oferece métodos baseados em callbacks para operações verdadeiramente
 * não-bloqueantes
 * Reduz necessidade de threads daemon e melhor integração com JavaFX
 */
public class TascaEatsGrpcClientAsync {

    private final TascaEatsGrpcClient client;

    public TascaEatsGrpcClientAsync(TascaEatsGrpcClient client) {
        this.client = client;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pessoa 2 — Restaurantes (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar restaurantes de forma assíncrona
     * 
     * @param request   Requisição
     * @param onSuccess Callback de sucesso com resposta
     * @param onError   Callback de erro com mensagem
     */
    public void listarRestaurantesAsync(
            ListarRestaurantesRequest request,
            Consumer<ListarRestaurantesResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarRestaurantesResponse> responseObserver = new StreamObserver<ListarRestaurantesResponse>() {
            @Override
            public void onNext(ListarRestaurantesResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar restaurantes: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarRestaurantes: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarRestaurantes concluido");
            }
        };

        client.getAsyncStub().listarRestaurantes(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pessoa 2 — Menus (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar menus de forma assíncrona
     */
    public void listarMenusAsync(
            ListarMenusRequest request,
            Consumer<ListarMenusResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarMenusResponse> responseObserver = new StreamObserver<ListarMenusResponse>() {
            @Override
            public void onNext(ListarMenusResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar menus: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarMenus: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarMenus concluido");
            }
        };

        client.getAsyncStub().listarMenus(request, responseObserver);
    }

    /**
     * Criar menu de forma assíncrona
     */
    public void criarMenuAsync(
            CriarMenuRequest request,
            Consumer<MenuResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<MenuResponse> responseObserver = new StreamObserver<MenuResponse>() {
            @Override
            public void onNext(MenuResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao criar menu: " + t.getMessage());
                System.err.println("[gRPC ERROR] CriarMenu: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] CriarMenu concluido");
            }
        };

        client.getAsyncStub().criarMenu(request, responseObserver);
    }

    /**
     * Atualizar menu de forma assíncrona
     */
    public void atualizarMenuAsync(
            AtualizarMenuRequest request,
            Consumer<MenuResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<MenuResponse> responseObserver = new StreamObserver<MenuResponse>() {
            @Override
            public void onNext(MenuResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao atualizar menu: " + t.getMessage());
                System.err.println("[gRPC ERROR] AtualizarMenu: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] AtualizarMenu concluido");
            }
        };

        client.getAsyncStub().atualizarMenu(request, responseObserver);
    }

    /**
     * Remover menu de forma assíncrona
     */
    public void removerMenuAsync(
            RemoverMenuRequest request,
            Runnable onSuccess,
            Consumer<String> onError) {

        StreamObserver<com.google.protobuf.Empty> responseObserver = new StreamObserver<com.google.protobuf.Empty>() {
            @Override
            public void onNext(com.google.protobuf.Empty response) {
                onSuccess.run();
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao remover menu: " + t.getMessage());
                System.err.println("[gRPC ERROR] RemoverMenu: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] RemoverMenu concluido");
            }
        };

        client.getAsyncStub().removerMenu(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pessoa 1 — Avaliações (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar avaliações de forma assíncrona
     */
    public void listarAvaliacoesAsync(
            ListarAvaliacoesRequest request,
            Consumer<ListarAvaliacoesResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarAvaliacoesResponse> responseObserver = new StreamObserver<ListarAvaliacoesResponse>() {
            @Override
            public void onNext(ListarAvaliacoesResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar avaliações: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarAvaliacoes: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarAvaliacoes concluido");
            }
        };

        client.getAsyncStub().listarAvaliacoes(request, responseObserver);
    }

    /**
     * Criar avaliação de forma assíncrona
     */
    public void criarAvaliacaoAsync(
            CriarAvaliacaoRequest request,
            Consumer<AvaliacaoResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<AvaliacaoResponse> responseObserver = new StreamObserver<AvaliacaoResponse>() {
            @Override
            public void onNext(AvaliacaoResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao criar avaliação: " + t.getMessage());
                System.err.println("[gRPC ERROR] CriarAvaliacao: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] CriarAvaliacao concluido");
            }
        };

        client.getAsyncStub().criarAvaliacao(request, responseObserver);
    }

    /**
     * Atualizar avaliação de forma assíncrona
     */
    public void atualizarAvaliacaoAsync(
            AtualizarAvaliacaoRequest request,
            Consumer<AvaliacaoResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<AvaliacaoResponse> responseObserver = new StreamObserver<AvaliacaoResponse>() {
            @Override
            public void onNext(AvaliacaoResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao atualizar avaliação: " + t.getMessage());
                System.err.println("[gRPC ERROR] AtualizarAvaliacao: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] AtualizarAvaliacao concluido");
            }
        };

        client.getAsyncStub().atualizarAvaliacao(request, responseObserver);
    }

    /**
     * Remover avaliação de forma assíncrona
     */
    public void removerAvaliacaoAsync(
            RemoverAvaliacaoRequest request,
            Runnable onSuccess,
            Consumer<String> onError) {

        StreamObserver<com.google.protobuf.Empty> responseObserver = new StreamObserver<com.google.protobuf.Empty>() {
            @Override
            public void onNext(com.google.protobuf.Empty response) {
                onSuccess.run();
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao remover avaliação: " + t.getMessage());
                System.err.println("[gRPC ERROR] RemoverAvaliacao: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] RemoverAvaliacao concluido");
            }
        };

        client.getAsyncStub().removerAvaliacao(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Entregas — Listagem e Atualização (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar entregas de forma assíncrona
     */
    public void listarEntregasAsync(
            ListarEntregasRequest request,
            Consumer<ListarEntregasResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarEntregasResponse> responseObserver = new StreamObserver<ListarEntregasResponse>() {
            @Override
            public void onNext(ListarEntregasResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar entregas: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarEntregas: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarEntregas concluido");
            }
        };

        client.getAsyncStub().listarEntregas(request, responseObserver);
    }

    /**
     * Atualizar status entrega de forma assíncrona
     */
    public void atualizarStatusEntregaAsync(
            AtualizarStatusEntregaRequest request,
            Consumer<EntregaResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<EntregaResponse> responseObserver = new StreamObserver<EntregaResponse>() {
            @Override
            public void onNext(EntregaResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao atualizar status: " + t.getMessage());
                System.err.println("[gRPC ERROR] AtualizarStatusEntrega: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] AtualizarStatusEntrega concluido");
            }
        };

        client.getAsyncStub().atualizarStatusEntrega(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pagamentos — Listagem (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar pagamentos de forma assíncrona
     */
    public void listarPagamentosAsync(
            ListarPagamentosRequest request,
            Consumer<ListarPagamentosResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarPagamentosResponse> responseObserver = new StreamObserver<ListarPagamentosResponse>() {
            @Override
            public void onNext(ListarPagamentosResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar pagamentos: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarPagamentos: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarPagamentos concluido");
            }
        };

        client.getAsyncStub().listarPagamentos(request, responseObserver);
    }

    /**
     * Registar pagamento de forma assíncrona
     */
    public void registarPagamentoAsync(
            RegistarPagamentoRequest request,
            Consumer<PagamentoResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<PagamentoResponse> responseObserver = new StreamObserver<PagamentoResponse>() {
            @Override
            public void onNext(PagamentoResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao registar pagamento: " + t.getMessage());
                System.err.println("[gRPC ERROR] RegistarPagamento: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] RegistarPagamento concluido");
            }
        };

        client.getAsyncStub().registarPagamento(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Users — CRUD (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar utilizadores de forma assíncrona
     */
    public void listarUsersAsync(
            ListarUsersRequest request,
            Consumer<ListarUsersResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarUsersResponse> responseObserver = new StreamObserver<ListarUsersResponse>() {
            @Override
            public void onNext(ListarUsersResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar utilizadores: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarUsers: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarUsers concluido");
            }
        };

        client.getAsyncStub().listarUsers(request, responseObserver);
    }

    /**
     * Remover utilizador de forma assíncrona
     */
    public void removerUserAsync(
            RemoverUserRequest request,
            Runnable onSuccess,
            Consumer<String> onError) {

        StreamObserver<com.google.protobuf.Empty> responseObserver = new StreamObserver<com.google.protobuf.Empty>() {
            @Override
            public void onNext(com.google.protobuf.Empty response) {
                onSuccess.run();
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao remover utilizador: " + t.getMessage());
                System.err.println("[gRPC ERROR] RemoverUser: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] RemoverUser concluido");
            }
        };

        client.getAsyncStub().removerUser(request, responseObserver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Produtos — CRUD (Assíncrono)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Listar produtos de forma assíncrona
     */
    public void listarProdutosAsync(
            ListarProdutosRequest request,
            Consumer<ListarProdutosResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ListarProdutosResponse> responseObserver = new StreamObserver<ListarProdutosResponse>() {
            @Override
            public void onNext(ListarProdutosResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao listar produtos: " + t.getMessage());
                System.err.println("[gRPC ERROR] ListarProdutos: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] ListarProdutos concluido");
            }
        };

        client.getAsyncStub().listarProdutos(request, responseObserver);
    }

    /**
     * Criar produto de forma assíncrona
     */
    public void criarProdutoAsync(
            CriarProdutoRequest request,
            Consumer<ProdutoResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ProdutoResponse> responseObserver = new StreamObserver<ProdutoResponse>() {
            @Override
            public void onNext(ProdutoResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao criar produto: " + t.getMessage());
                System.err.println("[gRPC ERROR] CriarProduto: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] CriarProduto concluido");
            }
        };

        client.getAsyncStub().criarProduto(request, responseObserver);
    }

    /**
     * Atualizar produto de forma assíncrona
     */
    public void atualizarProdutoAsync(
            AtualizarProdutoRequest request,
            Consumer<ProdutoResponse> onSuccess,
            Consumer<String> onError) {

        StreamObserver<ProdutoResponse> responseObserver = new StreamObserver<ProdutoResponse>() {
            @Override
            public void onNext(ProdutoResponse response) {
                onSuccess.accept(response);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao atualizar produto: " + t.getMessage());
                System.err.println("[gRPC ERROR] AtualizarProduto: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] AtualizarProduto concluido");
            }
        };

        client.getAsyncStub().atualizarProduto(request, responseObserver);
    }

    /**
     * Remover produto de forma assíncrona
     */
    public void removerProdutoAsync(
            RemoverProdutoRequest request,
            Runnable onSuccess,
            Consumer<String> onError) {

        StreamObserver<com.google.protobuf.Empty> responseObserver = new StreamObserver<com.google.protobuf.Empty>() {
            @Override
            public void onNext(com.google.protobuf.Empty response) {
                onSuccess.run();
            }

            @Override
            public void onError(Throwable t) {
                onError.accept("Erro ao remover produto: " + t.getMessage());
                System.err.println("[gRPC ERROR] RemoverProduto: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("[gRPC] RemoverProduto concluido");
            }
        };

        client.getAsyncStub().removerProduto(request, responseObserver);
    }
}