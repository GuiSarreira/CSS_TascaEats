package pt.ul.fc.css.tascaeats.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.ProdutoRepository;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import pt.ul.fc.css.tascaeats.services.AvaliacaoService;
import pt.ul.fc.css.tascaeats.services.MenuService;
import pt.ul.fc.css.tascaeats.services.RestauranteService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do serviço gRPC TascaEats.
 *
 * <p>Pessoa 2: Menus Partilhados + Restaurantes — implementação completa.</p>
 * <p>Pessoa 1 (Avaliações) e Pessoa 3 (Pedidos/Entregas/Pagamentos) — stub UNIMPLEMENTED.</p>
 */
@GrpcService
public class TascaEatsGrpcServiceImpl extends TascaEatsServiceGrpc.TascaEatsServiceImplBase {

    private final MenuService menuService;
    private final RestauranteService restauranteService;
    private final AvaliacaoService avaliacaoService;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;

    public TascaEatsGrpcServiceImpl(MenuService menuService,
                                    RestauranteService restauranteService,
                                    AvaliacaoService avaliacaoService,
                                    RestauranteRepository restauranteRepository,
                                    ProdutoRepository produtoRepository) {
        this.menuService = menuService;
        this.restauranteService = restauranteService;
        this.avaliacaoService = avaliacaoService;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Pessoa 2 — Menus Partilhados
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

    // ═════════════════════════════════════════════════════════════════════════
    // Pessoa 2 — Restaurantes (filtros avançados)
    // ═════════════════════════════════════════════════════════════════════════

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
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Pessoa 1 — Avaliações (implementação completa)
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
                avaliacoes = avaliacaoService.obterAvaliacoesPorRestaurante(request.getRestauranteId());
            } else if (request.hasClienteId()) {
                avaliacoes = avaliacaoService.obterAvaliacoesPorCliente(request.getClienteId());
            } else {
                avaliacoes = List.of();
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

    // ═════════════════════════════════════════════════════════════════════════
    // Pessoa 3 — Pedidos / Entregas / Pagamentos (stub — a implementar)
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void criarPedido(CriarPedidoRequest request,
                            StreamObserver<PedidoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("CriarPedido ainda não implementado").asRuntimeException());
    }

    @Override
    public void listarPedidos(ListarPedidosRequest request,
                              StreamObserver<ListarPedidosResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("ListarPedidos ainda não implementado").asRuntimeException());
    }

    @Override
    public void avancarEstadoPedido(AvancarEstadoPedidoRequest request,
                                    StreamObserver<PedidoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("AvancarEstadoPedido ainda não implementado").asRuntimeException());
    }

    @Override
    public void cancelarPedido(CancelarPedidoRequest request,
                               StreamObserver<Empty> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("CancelarPedido ainda não implementado").asRuntimeException());
    }

    @Override
    public void registarPagamento(RegistarPagamentoRequest request,
                                  StreamObserver<PagamentoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("RegistarPagamento ainda não implementado").asRuntimeException());
    }

    @Override
    public void obterEntrega(ObterEntregaRequest request,
                             StreamObserver<EntregaResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("ObterEntrega ainda não implementado").asRuntimeException());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers — conversão Entity → Proto message
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

    private List<Produto> resolverProdutos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(produtoRepository.findAllById(ids));
    }

    private List<Restaurante> resolverRestaurantes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(restauranteRepository.findAllById(ids));
    }
}
