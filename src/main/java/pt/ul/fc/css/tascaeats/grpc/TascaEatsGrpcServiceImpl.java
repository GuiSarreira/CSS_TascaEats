package pt.ul.fc.css.tascaeats.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.repositories.UserRepository;
import pt.ul.fc.css.tascaeats.repositories.EntregaRepository;
import pt.ul.fc.css.tascaeats.repositories.PagamentoRepository;
import pt.ul.fc.css.tascaeats.repositories.AvaliacaoRepository;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;
import pt.ul.fc.css.tascaeats.services.MenuService;
import pt.ul.fc.css.tascaeats.services.PedidoService;
import pt.ul.fc.css.tascaeats.services.RestauranteService;
import pt.ul.fc.css.tascaeats.services.PagamentoService;
import pt.ul.fc.css.tascaeats.services.UserService;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do serviço gRPC TascaEats.
 *
 * <p>{@code @Transactional(readOnly = true)} garante que a sessão Hibernate
 * permanece aberta durante toda a execução de cada método gRPC, evitando
 * {@code LazyInitializationException} ao aceder a relações lazy-loaded
 * (ex: Entrega → Entregador, Pedido → Cliente).
 */
@GrpcService
@Transactional(readOnly = true)
public class TascaEatsGrpcServiceImpl extends TascaEatsServiceGrpc.TascaEatsServiceImplBase {

    private final MenuService menuService;
    private final RestauranteService restauranteService;
    private final AvaliacaoService avaliacaoService;
    private final PagamentoService pagamentoService;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;
    private final UserRepository userRepository;
    private final EntregaRepository entregaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoRepository pedidoRepository;
    private final UserService userService;
    private final PedidoService pedidoService;

    public TascaEatsGrpcServiceImpl(MenuService menuService,
            RestauranteService restauranteService,
            AvaliacaoService avaliacaoService,
            PagamentoService pagamentoService,
            RestauranteRepository restauranteRepository,
            ProdutoRepository produtoRepository,
            UserRepository userRepository,
            EntregaRepository entregaRepository,
            PagamentoRepository pagamentoRepository,
            AvaliacaoRepository avaliacaoRepository,
            PedidoRepository pedidoRepository,
            UserService userService,
            PedidoService pedidoService) {
        this.menuService = menuService;
        this.restauranteService = restauranteService;
        this.avaliacaoService = avaliacaoService;
        this.pagamentoService = pagamentoService;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
        this.userRepository = userRepository;
        this.entregaRepository = entregaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.userService = userService;
        this.pedidoService = pedidoService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Menus Partilhados + Restaurantes
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void criarMenu(CriarMenuRequest request,
            StreamObserver<MenuResponse> responseObserver) {
        try {
            List<Produto> produtos = resolverProdutos(request.getProdutoIdsList());
            List<Restaurante> restaurantes = resolverRestaurantes(request.getRestauranteIdsList());

            Menu menu = menuService.criarMenu(
                    request.getNome(), request.getDescricao(), produtos, restaurantes);

            responseObserver.onNext(toMenuResponse(menu));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listarMenus(ListarMenusRequest request,
            StreamObserver<ListarMenusResponse> responseObserver) {
        try {
            String nome = request.hasNome() ? request.getNome() : null;
            Integer minProd = request.hasMinProdutos() ? request.getMinProdutos() : null;
            Integer maxProd = request.hasMaxProdutos() ? request.getMaxProdutos() : null;
            Double minPreco = request.hasMinPreco() ? request.getMinPreco() : null;
            Double maxPreco = request.hasMaxPreco() ? request.getMaxPreco() : null;

            List<Menu> menus = menuService.listarMenusComFiltros(
                    nome, minProd, maxProd, minPreco, maxPreco);

            ListarMenusResponse.Builder builder = ListarMenusResponse.newBuilder();
            for (Menu m : menus) {
                builder.addMenus(toMenuResponse(m));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void atualizarMenu(AtualizarMenuRequest request,
            StreamObserver<MenuResponse> responseObserver) {
        try {
            List<Produto> produtos = resolverProdutos(request.getProdutoIdsList());
            List<Restaurante> restaurantes = resolverRestaurantes(request.getRestauranteIdsList());

            Menu menu = menuService.atualizarMenu(
                    request.getMenuId(), request.getNome(),
                    request.getDescricao(), produtos, restaurantes);

            responseObserver.onNext(toMenuResponse(menu));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void removerMenu(RemoverMenuRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            menuService.removerMenu(request.getMenuId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void associarMenuRestaurante(AssociarMenuRestauranteRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            menuService.associarMenuRestaurante(
                    request.getMenuId(), request.getRestauranteId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listarRestaurantes(ListarRestaurantesRequest request,
            StreamObserver<ListarRestaurantesResponse> responseObserver) {
        try {
            String nome = request.hasNome() ? request.getNome() : null;
            String tipoCozinha = request.hasTipoCozinha() ? request.getTipoCozinha() : null;
            String horarioStr = request.hasHorario() ? request.getHorario() : null;
            LocalTime horario = horarioStr != null
                    ? LocalTime.parse(horarioStr, DateTimeFormatter.ofPattern("HH:mm"))
                    : null;
            Double minPreco = request.hasMinPreco() ? request.getMinPreco() : null;
            Double maxPreco = request.hasMaxPreco() ? request.getMaxPreco() : null;
            Integer minAval = request.hasMinAvaliacoes() ? request.getMinAvaliacoes() : null;
            String cidade = request.hasCidade() ? request.getCidade() : null;
            Integer minPed = request.hasMinPedidos() ? request.getMinPedidos() : null;

            List<Restaurante> restaurantes = restauranteService
                    .listarRestaurantesComFiltros(
                            nome, tipoCozinha, horario, minPreco, maxPreco,
                            minAval, cidade, minPed);

            ListarRestaurantesResponse.Builder builder = ListarRestaurantesResponse.newBuilder();
            for (Restaurante r : restaurantes) {
                builder.addRestaurantes(toRestauranteInfo(r));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Avaliações
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void criarAvaliacao(CriarAvaliacaoRequest request,
            StreamObserver<AvaliacaoResponse> responseObserver) {
        try {
            Avaliacao a = avaliacaoService.criarAvaliacao(
                    request.getClienteId(), request.getRestauranteId(),
                    request.getPedidoId(), request.getNota(), request.getComentario());

            responseObserver.onNext(toAvaliacaoResponse(a));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listarAvaliacoes(ListarAvaliacoesRequest request,
            StreamObserver<ListarAvaliacoesResponse> responseObserver) {
        try {
            List<Avaliacao> avaliacoes;
            if (request.hasRestauranteId()) {
                avaliacoes = avaliacaoRepository.findByRestauranteIdWithFetch(request.getRestauranteId());
            } else if (request.hasClienteId()) {
                avaliacoes = avaliacaoRepository.findByClienteIdWithFetch(request.getClienteId());
            } else {
                avaliacoes = avaliacaoRepository.findAllWithClienteAndRestaurante();
            }

            ListarAvaliacoesResponse.Builder builder = ListarAvaliacoesResponse.newBuilder();
            for (Avaliacao a : avaliacoes) {
                builder.addAvaliacoes(toAvaliacaoResponse(a));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void atualizarAvaliacao(AtualizarAvaliacaoRequest request,
            StreamObserver<AvaliacaoResponse> responseObserver) {
        try {
            Avaliacao a = avaliacaoService.atualizarAvaliacao(
                    request.getAvaliacaoId(), request.getNota(),
                    request.getComentario(), request.getClienteId());

            responseObserver.onNext(toAvaliacaoResponse(a));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void removerAvaliacao(RemoverAvaliacaoRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            avaliacaoService.removerAvaliacao(
                    request.getAvaliacaoId(), request.getClienteId(), request.getIsAdmin());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obterAvaliacao(ObterAvaliacaoRequest request,
            StreamObserver<AvaliacaoResponse> responseObserver) {
        try {
            Avaliacao a = avaliacaoRepository.findById(request.getAvaliacaoId())
                    .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
            responseObserver.onNext(toAvaliacaoResponse(a));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obterMenu(ObterMenuRequest request,
            StreamObserver<MenuResponse> responseObserver) {
        try {
            Menu menu = menuService.buscarPorId(request.getMenuId());
            responseObserver.onNext(toMenuResponse(menu));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Pedidos / Entregas / Pagamentos
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void criarPedido(CriarPedidoRequest request,
            StreamObserver<PedidoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("CriarPedido — será implementado em v1.1").asRuntimeException());
    }

    @Override
    public void listarPedidos(ListarPedidosRequest request,
            StreamObserver<ListarPedidosResponse> responseObserver) {
        try {
            PedidoStatus status = request.hasStatus() ? PedidoStatus.valueOf(request.getStatus()) : null;
            List<Pedido> pedidos;

            if (request.getClienteId() > 0) {
                pedidos = pedidoService.buscarPorCliente(request.getClienteId(), status);
            } else if (status != null) {
                pedidos = pedidoRepository.findByStatus(status);
            } else {
                pedidos = pedidoRepository.findAll();
            }

            ListarPedidosResponse.Builder builder = ListarPedidosResponse.newBuilder();
            for (Pedido pedido : pedidos) {
                builder.addPedidos(toPedidoResponse(pedido));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Estado de pedido inválido: " + request.getStatus()).asRuntimeException());
        } catch (Throwable e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void avancarEstadoPedido(AvancarEstadoPedidoRequest request,
            StreamObserver<PedidoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("AvancarEstadoPedido — será implementado em v1.1").asRuntimeException());
    }

    @Override
    public void cancelarPedido(CancelarPedidoRequest request,
            StreamObserver<Empty> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("CancelarPedido — será implementado em v1.1").asRuntimeException());
    }

    @Override
    public void registarPagamento(RegistarPagamentoRequest request,
            StreamObserver<PagamentoResponse> responseObserver) {
        try {
            String dadosExtra = null;
            if (request.hasReferencia()) {
                dadosExtra = request.getReferencia();
            } else if (request.hasTelemovel()) {
                dadosExtra = request.getTelemovel();
            }

            String bandeira = request.hasBandeira() ? request.getBandeira() : null;
            Double troco = request.hasTroco() ? request.getTroco() : null;

            Pagamento pagamento = pagamentoService.processarPagamento(
                    request.getPedidoId(),
                    request.getTipoPagamento(),
                    dadosExtra,
                    bandeira,
                    troco);

            responseObserver.onNext(toPagamentoResponse(pagamento));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalStateException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage()).asRuntimeException());
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obterEntrega(ObterEntregaRequest request,
            StreamObserver<EntregaResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("ObterEntrega — será implementado em v1.1").asRuntimeException());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Entregas — Listagem e Atualização de Status
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void listarEntregas(ListarEntregasRequest request,
            StreamObserver<ListarEntregasResponse> responseObserver) {
        try {
            List<Entrega> entregas = entregaRepository.findAllWithEntregadorAndPedido();

            if (request.hasStatus()) {
                EntregaStatus status = EntregaStatus.valueOf(request.getStatus());
                entregas = entregas.stream()
                        .filter(e -> e.getStatus() == status)
                        .toList();
            }

            if (request.hasEntregadorId()) {
                long entregadorId = request.getEntregadorId();
                entregas = entregas.stream()
                        .filter(e -> e.getEntregador() != null
                                && e.getEntregador().getId() != null
                                && e.getEntregador().getId().equals(entregadorId))
                        .toList();
            }

            ListarEntregasResponse.Builder builder = ListarEntregasResponse.newBuilder();
            for (Entrega e : entregas) {
                builder.addEntregas(toEntregaResponse(e));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void atualizarStatusEntrega(AtualizarStatusEntregaRequest request,
            StreamObserver<EntregaResponse> responseObserver) {
        try {
            Entrega entrega = entregaRepository.findById(request.getEntregaId())
                    .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

            EntregaStatus novoEstado = EntregaStatus.valueOf(request.getNovoStatus());
            entrega.setStatus(novoEstado);
            Entrega updated = entregaRepository.save(entrega);

            responseObserver.onNext(toEntregaResponse(updated));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Estado de entrega inválido: " + request.getNovoStatus()).asRuntimeException());
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Pagamentos — Listagem
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void listarPagamentos(ListarPagamentosRequest request,
            StreamObserver<ListarPagamentosResponse> responseObserver) {
        try {
            List<Pagamento> pagamentos = pagamentoRepository.findAll();

            if (request.hasPedidoId()) {
                long pedidoId = request.getPedidoId();
                pagamentos = pagamentos.stream()
                        .filter(p -> p.getPedido() != null && p.getPedido().getId() != null
                                && p.getPedido().getId().equals(pedidoId))
                        .toList();
            }

            if (request.hasStatus()) {
                PagamentoStatus status = PagamentoStatus.valueOf(request.getStatus());
                pagamentos = pagamentos.stream()
                        .filter(p -> p.getStatus() == status)
                        .toList();
            }

            ListarPagamentosResponse.Builder builder = ListarPagamentosResponse.newBuilder();
            for (Pagamento p : pagamentos) {
                builder.addPagamentos(toPagamentoResponse(p));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Status de pagamento inválido: " + request.getStatus()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Users — CRUD
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void listarUsers(ListarUsersRequest request,
            StreamObserver<ListarUsersResponse> responseObserver) {
        try {
            List<User> users = userRepository.findAll();

            if (request.hasTipo() && !request.getTipo().isBlank()) {
                String tipo = request.getTipo().trim().toUpperCase();
                users = users.stream()
                        .filter(u -> resolveTipoUtilizador(u).equalsIgnoreCase(tipo))
                        .toList();
            }

            ListarUsersResponse.Builder builder = ListarUsersResponse.newBuilder();
            for (User u : users) {
                builder.addUsers(toUserInfo(u));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void registarUser(RegistarUserRequest request,
            StreamObserver<UserResponse> responseObserver) {
        try {
            String tipo = request.getTipo().trim().toUpperCase();
            User user;

            switch (tipo) {
                case "ADMIN" -> user = userService.registarAdmin(
                        request.getEmail(), request.getNome(), request.getPassword());
                case "ENTREGADOR" -> {
                    String zonaAtuacao = request.hasZonaAtuacao() && !request.getZonaAtuacao().isBlank()
                            ? request.getZonaAtuacao()
                            : "Lisboa";
                    user = userService.registarEntregador(
                            request.getEmail(), request.getNome(), request.getPassword(), "Mota", zonaAtuacao);
                }
                case "CLIENTE" -> user = userService.registarCliente(
                        request.getEmail(), request.getNome(), request.getPassword(),
                        new Endereco("Rua por definir", "0000-000", "Lisboa"));
                default -> throw new IllegalArgumentException("Tipo de utilizador inválido: " + request.getTipo());
            }

            if (request.hasTelemovel() && !request.getTelemovel().isBlank()) {
                user.setTelemovel(request.getTelemovel());
                user = userRepository.save(user);
            }

            UserResponse.Builder builder = UserResponse.newBuilder()
                    .setId(user.getId())
                    .setNome(user.getNome())
                    .setEmail(user.getEmail())
                    .setTipo(resolveTipoUtilizador(user))
                    .setAtivo(user.isAtivo());

            if (user instanceof Entregador entregador && entregador.getZonaAtuacao() != null) {
                builder.setZonaAtuacao(entregador.getZonaAtuacao());
            }
            if (user.getTelemovel() != null && !user.getTelemovel().isBlank()) {
                builder.setTelemovel(user.getTelemovel());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void removerUser(RemoverUserRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
            userRepository.delete(user);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Produtos — CRUD
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void listarProdutos(ListarProdutosRequest request,
            StreamObserver<ListarProdutosResponse> responseObserver) {
        try {
            List<Produto> produtos = produtoRepository.findAll();

            ListarProdutosResponse.Builder builder = ListarProdutosResponse.newBuilder();
            for (Produto p : produtos) {
                builder.addProdutos(toProdutoResponse(p));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void criarProduto(CriarProdutoRequest request,
            StreamObserver<ProdutoResponse> responseObserver) {
        try {
            Produto produto = new Produto(
                    request.getNome(),
                    request.getDescricao(),
                    request.getPreco(),
                    request.getCategoria());
            produto.setDisponivel(request.getDisponivel());

            Produto created = produtoRepository.save(produto);
            responseObserver.onNext(toProdutoResponse(created));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void atualizarProduto(AtualizarProdutoRequest request,
            StreamObserver<ProdutoResponse> responseObserver) {
        try {
            Produto produto = produtoRepository.findById(request.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            produto.setNome(request.getNome());
            produto.setDescricao(request.getDescricao());
            produto.setPreco(request.getPreco());
            produto.setCategoria(request.getCategoria());
            produto.setDisponivel(request.getDisponivel());

            Produto updated = produtoRepository.save(produto);
            responseObserver.onNext(toProdutoResponse(updated));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void removerProduto(RemoverProdutoRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            Produto produto = produtoRepository.findById(request.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            produtoRepository.delete(produto);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — Conversão Entity → Proto Message
    // ═════════════════════════════════════════════════════════════════════════

    private MenuResponse toMenuResponse(Menu menu) {
        MenuResponse.Builder b = MenuResponse.newBuilder()
                .setId(menu.getId())
                .setNome(menu.getNome())
                .setDescricao(menu.getDescricao() != null ? menu.getDescricao() : "");

        for (Produto p : menu.getProdutos()) {
            b.addProdutos(toProdutoInfo(p));
        }
        for (Restaurante r : menu.getRestaurantes()) {
            b.addRestaurantes(toRestauranteInfo(r));
        }
        return b.build();
    }

    private ProdutoInfo toProdutoInfo(Produto p) {
        return ProdutoInfo.newBuilder()
                .setId(p.getId())
                .setNome(p.getNome())
                .setDescricao(p.getDescricao() != null ? p.getDescricao() : "")
                .setPreco(p.getPreco())
                .setCategoria(p.getCategoria() != null ? p.getCategoria() : "")
                .setDisponivel(p.isDisponivel())
                .build();
    }

    private RestauranteInfo toRestauranteInfo(Restaurante r) {
        RestauranteInfo.Builder b = RestauranteInfo.newBuilder()
                .setId(r.getId())
                .setNome(r.getNome())
                .setNif(r.getNif())
                .setTipoCozinha(r.getTipoCozinha() != null ? r.getTipoCozinha() : "")
                .setAberto(r.isAberto())
                .setHorarioAbertura(r.getHorarioAbertura() != null ? r.getHorarioAbertura().toString() : "")
                .setHorarioFecho(r.getHorarioFecho() != null ? r.getHorarioFecho().toString() : "");

        if (r.getMorada() != null) {
            b.setMorada(EnderecoInfo.newBuilder()
                    .setRua(r.getMorada().getRua())
                    .setCodigoPostal(r.getMorada().getCodigoPostal())
                    .setCidade(r.getMorada().getCidade())
                    .build());
        }
        return b.build();
    }

    private AvaliacaoResponse toAvaliacaoResponse(Avaliacao a) {
        return AvaliacaoResponse.newBuilder()
                .setId(a.getId())
                .setNota(a.getNota())
                .setComentario(a.getComentario() != null ? a.getComentario() : "")
                .setDataAvaliacao(a.getDataAvaliacao().toString())
                .setClienteId(a.getCliente().getId())
                .setClienteNome(a.getCliente().getNome())
                .setRestauranteId(a.getRestaurante().getId())
                .setRestauranteNome(a.getRestaurante().getNome())
                .build();
    }

    private EntregaResponse toEntregaResponse(Entrega e) {
        String entregadorNome = e.getEntregador() != null ? e.getEntregador().getNome() : "";
        return EntregaResponse.newBuilder()
                .setId(e.getId())
                .setPedidoId(e.getPedido() != null && e.getPedido().getId() != null ? e.getPedido().getId() : 0L)
                .setEntregadorId(
                        e.getEntregador() != null && e.getEntregador().getId() != null ? e.getEntregador().getId() : 0L)
                .setEntregadorNome(entregadorNome)
                .setStatus(e.getStatus() != null ? e.getStatus().name() : "")
                .setHoraRetirada(e.getHoraRetirada() != null ? e.getHoraRetirada().toString() : "")
                .setHoraEntrega(e.getHoraEntrega() != null ? e.getHoraEntrega().toString() : "")
                .build();
    }

    private PedidoResponse toPedidoResponse(Pedido p) {
        PedidoResponse.Builder builder = PedidoResponse.newBuilder()
                .setId(p.getId())
                .setStatus(p.getStatus() != null ? p.getStatus().name() : "")
                .setDataHora(p.getDataHora() != null ? p.getDataHora().toString() : "")
                .setPrecoTotal(p.getPrecoTotal() != null ? p.getPrecoTotal() : 0.0)
                .setClienteId(p.getCliente() != null && p.getCliente().getId() != null ? p.getCliente().getId() : 0L)
                .setRestauranteNome(resolverNomeRestauranteDoPedido(p));

        if (p.getEnderecoEntrega() != null) {
            builder.setEnderecoEntrega(EnderecoInfo.newBuilder()
                    .setRua(p.getEnderecoEntrega().getRua())
                    .setCodigoPostal(p.getEnderecoEntrega().getCodigoPostal())
                    .setCidade(p.getEnderecoEntrega().getCidade())
                    .build());
        }

        for (ProdutoPedido item : p.getProdutosPedido()) {
            builder.addItens(ProdutoPedidoInfo.newBuilder()
                    .setProdutoId(
                            item.getProduto() != null && item.getProduto().getId() != null ? item.getProduto().getId()
                                    : 0L)
                    .setProdutoNome(item.getProduto() != null ? item.getProduto().getNome() : "")
                    .setQuantidade(item.getQuantity())
                    .setPrecoCompra(item.getPrecoCompra() != null ? item.getPrecoCompra() : 0.0)
                    .build());
        }

        return builder.build();
    }

    private String resolverNomeRestauranteDoPedido(Pedido pedido) {
        if (pedido.getProdutosPedido().isEmpty()) {
            return "";
        }

        Produto produto = pedido.getProdutosPedido().get(0).getProduto();
        if (produto == null || produto.getMenus().isEmpty()) {
            return "";
        }

        Menu menu = produto.getMenus().get(0);
        if (menu.getRestaurantes().isEmpty()) {
            return "";
        }

        return menu.getRestaurantes().get(0).getNome();
    }

    private PagamentoResponse toPagamentoResponse(Pagamento p) {
        return PagamentoResponse.newBuilder()
                .setId(p.getId())
                .setTipoPagamento(resolveTipoPagamento(p))
                .setStatus(p.getStatus() != null ? p.getStatus().name() : "")
                .setValor(p.getPreco() != null ? p.getPreco() : 0.0)
                .build();
    }

    private UserInfo toUserInfo(User u) {
        return UserInfo.newBuilder()
                .setId(u.getId())
                .setNome(u.getNome())
                .setEmail(u.getEmail())
                .setTipo(resolveTipoUtilizador(u))
                .setAtivo(u.isAtivo())
                .build();
    }

    private String resolveTipoPagamento(Pagamento pagamento) {
        if (pagamento instanceof MBWay) {
            return "MBWAY";
        }
        if (pagamento instanceof Multibanco) {
            return "MULTIBANCO";
        }
        if (pagamento instanceof Dinheiro) {
            return "DINHEIRO";
        }
        return "DESCONHECIDO";
    }

    private String resolveTipoUtilizador(User user) {
        if (user instanceof Admin) {
            return "ADMIN";
        }
        if (user instanceof Entregador) {
            return "ENTREGADOR";
        }
        return "CLIENTE";
    }

    private ProdutoResponse toProdutoResponse(Produto p) {
        return ProdutoResponse.newBuilder()
                .setId(p.getId())
                .setNome(p.getNome())
                .setDescricao(p.getDescricao() != null ? p.getDescricao() : "")
                .setPreco(p.getPreco())
                .setCategoria(p.getCategoria() != null ? p.getCategoria() : "")
                .setDisponivel(p.isDisponivel())
                .build();
    }

    private List<Produto> resolverProdutos(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(produtoRepository.findAllById(ids));
    }

    private List<Restaurante> resolverRestaurantes(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(restauranteRepository.findAllById(ids));
    }
}
