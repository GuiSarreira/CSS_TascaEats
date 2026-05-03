package pt.ul.fc.css.tascaeats.javafx.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ul.fc.css.tascaeats.grpc.*;
import java.util.concurrent.TimeUnit;

/**
 * Cliente gRPC para TascaEats — Comunicação assíncrona com o servidor gRPC
 * 
 * Gerencia a conexão e fornece stubs para chamar métodos gRPC
 */
public class TascaEatsGrpcClient {

    private static final Object CHANNEL_LOCK = new Object();
    private static ManagedChannel sharedChannel;
    private static TascaEatsServiceGrpc.TascaEatsServiceStub sharedAsyncStub;
    private static TascaEatsServiceGrpc.TascaEatsServiceBlockingStub sharedBlockingStub;

    private final String host;
    private final int port;
    private ManagedChannel channel;
    private TascaEatsServiceGrpc.TascaEatsServiceStub asyncStub;
    private TascaEatsServiceGrpc.TascaEatsServiceBlockingStub blockingStub;

    public TascaEatsGrpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.channel = null;
        this.asyncStub = null;
        this.blockingStub = null;
    }

    /**
     * Conectar ao servidor gRPC
     */
    public void connect() throws Exception {
        synchronized (CHANNEL_LOCK) {
            if (sharedChannel == null || sharedChannel.isShutdown() || sharedChannel.isTerminated()) {
                sharedChannel = ManagedChannelBuilder
                        .forAddress(host, port)
                        .usePlaintext() // Sem SSL/TLS para desenvolvimento
                        .build();

                sharedAsyncStub = TascaEatsServiceGrpc.newStub(sharedChannel);
                sharedBlockingStub = TascaEatsServiceGrpc.newBlockingStub(sharedChannel);

                System.out.println("[gRPC] Conectado ao servidor: " + host + ":" + port);
            }

            channel = sharedChannel;
            asyncStub = sharedAsyncStub;
            blockingStub = sharedBlockingStub;
        }
    }

    /**
     * Desconectar do servidor gRPC
     */
    public void disconnect() {
        if (channel != null && channel != sharedChannel && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                System.out.println("[gRPC] Desconectado do servidor");
            } catch (InterruptedException e) {
                channel.shutdownNow();
                System.out.println("[gRPC] Forcado shutdown (timeout)");
            }
        }
    }

    /**
     * Verificar se está conectado
     */
    public boolean isConnected() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Avaliações
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Obter stub assíncrono para CriarAvaliacao
     */
    public TascaEatsServiceGrpc.TascaEatsServiceStub getAsyncStub() {
        return asyncStub;
    }

    /**
     * Obter stub bloqueante (síncrono) para CriarAvaliacao
     */
    public TascaEatsServiceGrpc.TascaEatsServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Menus + Restaurantes
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarRestaurantes (síncrono)
     */
    public ListarRestaurantesResponse listarRestaurantes(ListarRestaurantesRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarRestaurantes(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarRestaurantes: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar CriarMenu (síncrono)
     */
    public MenuResponse criarMenu(CriarMenuRequest request) throws StatusRuntimeException {
        try {
            return blockingStub.criarMenu(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] CriarMenu: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar ListarMenus (síncrono)
     */
    public ListarMenusResponse listarMenus(ListarMenusRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarMenus(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarMenus: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar ObterMenu (síncrono)
     */
    public MenuResponse obterMenu(ObterMenuRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.obterMenu(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ObterMenu: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar AtualizarMenu (síncrono)
     */
    public MenuResponse atualizarMenu(AtualizarMenuRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.atualizarMenu(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] AtualizarMenu: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RemoverMenu (síncrono)
     */
    public void removerMenu(RemoverMenuRequest request) throws StatusRuntimeException {
        try {
            blockingStub.removerMenu(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RemoverMenu: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pedidos / Entregas / Pagamentos (v1.1)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar CriarAvaliacao (síncrono)
     */
    public AvaliacaoResponse criarAvaliacao(CriarAvaliacaoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.criarAvaliacao(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] CriarAvaliacao: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar ListarAvaliacoes (síncrono)
     */
    public ListarAvaliacoesResponse listarAvaliacoes(ListarAvaliacoesRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarAvaliacoes(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarAvaliacoes: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar AtualizarAvaliacao (síncrono)
     */
    public AvaliacaoResponse atualizarAvaliacao(AtualizarAvaliacaoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.atualizarAvaliacao(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] AtualizarAvaliacao: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar ObterAvaliacao (síncrono)
     */
    public AvaliacaoResponse obterAvaliacao(ObterAvaliacaoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.obterAvaliacao(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ObterAvaliacao: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RemoverAvaliacao (síncrono)
     */
    public void removerAvaliacao(RemoverAvaliacaoRequest request)
            throws StatusRuntimeException {
        try {
            blockingStub.removerAvaliacao(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RemoverAvaliacao: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Entregas — Listagem e Atualização
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarEntregas (síncrono)
     */
    public ListarEntregasResponse listarEntregas(ListarEntregasRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarEntregas(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarEntregas: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar AtualizarStatusEntrega (síncrono)
     */
    public EntregaResponse atualizarStatusEntrega(AtualizarStatusEntregaRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.atualizarStatusEntrega(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] AtualizarStatusEntrega: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pagamentos — Listagem
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarPagamentos (síncrono)
     */
    public ListarPagamentosResponse listarPagamentos(ListarPagamentosRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarPagamentos(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarPagamentos: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RegistarPagamento (síncrono)
     */
    public PagamentoResponse registarPagamento(RegistarPagamentoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.registarPagamento(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RegistarPagamento: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Users — CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarUsers (síncrono)
     */
    public ListarUsersResponse listarUsers(ListarUsersRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarUsers(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarUsers: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RemoverUser (síncrono)
     */
    public void removerUser(RemoverUserRequest request)
            throws StatusRuntimeException {
        try {
            blockingStub.removerUser(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RemoverUser: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RegistarUser (síncrono)
     */
    public UserResponse registarUser(RegistarUserRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.registarUser(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RegistarUser: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Produtos — CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarProdutos (síncrono)
     */
    public ListarProdutosResponse listarProdutos(ListarProdutosRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarProdutos(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarProdutos: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar CriarProduto (síncrono)
     */
    public ProdutoResponse criarProduto(CriarProdutoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.criarProduto(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] CriarProduto: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar AtualizarProduto (síncrono)
     */
    public ProdutoResponse atualizarProduto(AtualizarProdutoRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.atualizarProduto(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] AtualizarProduto: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar RemoverProduto (síncrono)
     */
    public void removerProduto(RemoverProdutoRequest request)
            throws StatusRuntimeException {
        try {
            blockingStub.removerProduto(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] RemoverProduto: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pedidos — Listagem
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar ListarPedidos (síncrono)
     */
    public ListarPedidosResponse listarPedidos(ListarPedidosRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.listarPedidos(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] ListarPedidos: " + e.getStatus());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Restaurantes — CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Chamar CriarRestaurante (síncrono)
     */
    public RestauranteResponse criarRestaurante(CriarRestauranteRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.criarRestaurante(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] CriarRestaurante: " + e.getStatus());
            throw e;
        }
    }

    /**
     * Chamar AtualizarRestaurante (síncrono)
     */
    public RestauranteResponse atualizarRestaurante(AtualizarRestauranteRequest request)
            throws StatusRuntimeException {
        try {
            return blockingStub.atualizarRestaurante(request);
        } catch (StatusRuntimeException e) {
            System.err.println("[gRPC ERROR] AtualizarRestaurante: " + e.getStatus());
            throw e;
        }
    }
}
