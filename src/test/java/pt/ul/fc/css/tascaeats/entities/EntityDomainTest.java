package pt.ul.fc.css.tascaeats.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários diretos à camada de domínio (entidades).
 * Valida lógica de negócio encapsulada nos objetos de domínio,
 * sem dependências de Spring ou base de dados.
 */
class EntityDomainTest {

    private Endereco endereco;
    private Cliente cliente;
    private Restaurante restaurante;
    private Pedido pedido;
    private Entregador entregador;

    @BeforeEach
    void setUp() {
        endereco = new Endereco("Rua Dom, 1", "9000-001", "Funchal");
        cliente = new Cliente("c@test.com", "Ana", "pass", endereco);
        restaurante = new Restaurante("Funchal Sabores", endereco, "333444555");
        restaurante.setAberto(true);
        pedido = new Pedido(cliente, endereco);
        entregador = new Entregador("e@test.com", "Bruno", "pass", "carro", "Funchal");
    }

    // ─── Endereco ────────────────────────────────────────────────────────────

    @Test
    void endereco_GettersRetornamValoresCorretos() {
        assertThat(endereco.getRua()).isEqualTo("Rua Dom, 1");
        assertThat(endereco.getCodigoPostal()).isEqualTo("9000-001");
        assertThat(endereco.getCidade()).isEqualTo("Funchal");
    }

    // ─── Pedido — estado inicial ──────────────────────────────────────────────

    @Test
    void pedido_EstadoInicialCREATED() {
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.CREATED);
        assertThat(pedido.getPrecoTotal()).isEqualTo(0.0);
        assertThat(pedido.getCliente()).isEqualTo(cliente);
        assertThat(pedido.getEnderecoEntrega()).isEqualTo(endereco);
        assertThat(pedido.getDataHora()).isNotNull();
    }

    @Test
    void pedido_PodeSerCancelado_EmCREATED() {
        assertThat(pedido.podeSerCancelado()).isTrue();
    }

    @Test
    void pedido_PodeSerCancelado_EmPAID() {
        pedido.avancarEstado(); // → PAID
        assertThat(pedido.podeSerCancelado()).isTrue();
    }

    @Test
    void pedido_NaoPodeSerCancelado_EmPREPARING() {
        pedido.avancarEstado(); // → PAID
        pedido.avancarEstado(); // → PREPARING
        assertThat(pedido.podeSerCancelado()).isFalse();
    }

    // ─── Pedido — avancarEstado ───────────────────────────────────────────────

    @Test
    void pedido_AvancarEstado_FluxoCompleto() {
        pedido.avancarEstado();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.PAID);

        pedido.avancarEstado();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.PREPARING);

        pedido.avancarEstado();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.READY);

        pedido.avancarEstado();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.IN_DELIVERY);

        pedido.avancarEstado();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.DELIVERED);
    }

    @Test
    void pedido_AvancarEstado_EmDELIVERED_ThrowsIllegalStateException() {
        // Avançar até DELIVERED
        for (int i = 0; i < 5; i++)
            pedido.avancarEstado();

        assertThatThrownBy(pedido::avancarEstado)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pedido_Cancelar_EmCREATED() {
        pedido.cancelar();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.CANCELLED);
    }

    @Test
    void pedido_Cancelar_EmPAID() {
        pedido.avancarEstado(); // PAID
        pedido.cancelar();
        assertThat(pedido.getStatus()).isEqualTo(PedidoStatus.CANCELLED);
    }

    @Test
    void pedido_Cancelar_EmPREPARING_ThrowsIllegalStateException() {
        pedido.avancarEstado(); // PAID
        pedido.avancarEstado(); // PREPARING

        assertThatThrownBy(pedido::cancelar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED ou PAID");
    }

    // ─── Pedido — adicionarProduto / recalcularTotal ──────────────────────────

    @Test
    void pedido_AdicionarProduto_RecalculaTotal() {
        Produto produto = new Produto("Prego", "No Pão", 5.0, "Prato Principal");
        ProdutoPedido item = new ProdutoPedido(produto, 2);

        pedido.adicionarProduto(item);

        assertThat(pedido.getPrecoTotal()).isEqualTo(10.0);
        assertThat(pedido.getProdutosPedido()).hasSize(1);
    }

    // ─── Entrega — ciclo de vida ──────────────────────────────────────────────

    @Test
    void entrega_EstadoInicialATRIBUIDA() {
        Entrega entrega = new Entrega(pedido, entregador);
        assertThat(entrega.getStatus()).isEqualTo(EntregaStatus.ATRIBUIDA);
        assertThat(entrega.getHoraRetirada()).isNull(); // ainda não foi recolhido
        assertThat(entrega.getHoraEntrega()).isNull();
        assertThat(entrega.getPedido()).isEqualTo(pedido);
        assertThat(entrega.getEntregador()).isEqualTo(entregador);
    }

    @Test
    void entrega_IniciarEntrega_ATRIBUIDAparaACaminho() {
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.iniciarEntrega();
        assertThat(entrega.getStatus()).isEqualTo(EntregaStatus.A_CAMINHO);
        assertThat(entrega.getHoraRetirada()).isNotNull(); // preenchida ao iniciar
    }

    @Test
    void entrega_IniciarEntrega_NaoATRIBUIDA_ThrowsIllegalStateException() {
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.iniciarEntrega(); // A_CAMINHO

        assertThatThrownBy(entrega::iniciarEntrega)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entrega_Concluir_ACaminhoParaCONCLUIDA() {
        entregador.setDisponivel(false);
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.iniciarEntrega();
        entrega.concluir();

        assertThat(entrega.getStatus()).isEqualTo(EntregaStatus.CONCLUIDA);
        assertThat(entrega.getHoraEntrega()).isNotNull();
        assertThat(entregador.isDisponivel()).isTrue(); // reatribuído como disponível
    }

    @Test
    void entrega_Cancelar_ATRIBUIDAparaCANCELADA() {
        entregador.setDisponivel(false);
        Entrega entrega = new Entrega(pedido, entregador);
        entrega.cancelar();

        assertThat(entrega.getStatus()).isEqualTo(EntregaStatus.CANCELADA);
        assertThat(entregador.isDisponivel()).isTrue();
    }

    // ─── Pagamento — ciclo de vida ────────────────────────────────────────────

    @Test
    void pagamento_Multibanco_EstadoInicialPENDING() {
        Pagamento pg = new Multibanco(pedido, 20.0, "123 456 789", "Visa");
        assertThat(pg.getStatus()).isEqualTo(PagamentoStatus.PENDING);
        assertThat(pg.getPreco()).isEqualTo(20.0);
        assertThat(pg.isCompleto()).isFalse();
        assertThat(((Multibanco) pg).getBandeira()).isEqualTo("Visa");
    }

    @Test
    void pagamento_Processar_PENDINGparaCOMPLETED() {
        Pagamento pg = new Multibanco(pedido, 20.0, "ref", "Visa");
        pg.processar();

        assertThat(pg.getStatus()).isEqualTo(PagamentoStatus.COMPLETED);
        assertThat(pg.isCompleto()).isTrue();
        assertThat(pg.getDataPagamento()).isNotNull();
    }

    @Test
    void pagamento_Falhar_PENDINGparaFAILED() {
        Pagamento pg = new MBWay(pedido, 15.0, "912345678");
        pg.falhar();

        assertThat(pg.getStatus()).isEqualTo(PagamentoStatus.FAILED);
    }

    @Test
    void pagamento_Processar_NaoPENDING_ThrowsIllegalStateException() {
        Pagamento pg = new Multibanco(pedido, 20.0, "ref", "Visa");
        pg.processar(); // COMPLETED

        assertThatThrownBy(pg::processar)
                .isInstanceOf(IllegalStateException.class);
    }

    // ─── User — desativar / ativar ────────────────────────────────────────────

    @Test
    void user_Desativar_SetaAtivoFalse() {
        assertThat(cliente.isAtivo()).isTrue();
        cliente.desativar();
        assertThat(cliente.isAtivo()).isFalse();
    }

    @Test
    void user_GetNome_ReturnsCorrect() {
        assertThat(cliente.getNome()).isEqualTo("Ana");
        assertThat(cliente.getEmail()).isEqualTo("c@test.com");
    }

    // ─── Produto — soft delete ────────────────────────────────────────────────

    @Test
    void produto_DeleteLogicamente_SetaEliminado() {
        Produto produto = new Produto("Sopa", "Do dia", 3.5, "Entrada");
        assertThat(produto.isEliminado()).isFalse();
        assertThat(produto.isDisponivel()).isTrue();

        produto.deleteLogicamente();

        assertThat(produto.isEliminado()).isTrue();
    }

    // ─── Restaurante ──────────────────────────────────────────────────────────

    @Test
    void restaurante_EstadoInicial_FechadoPorDefeito() {
        Restaurante novo = new Restaurante("Novo", endereco, "999888777");
        assertThat(novo.isAberto()).isFalse();
        assertThat(novo.getNome()).isEqualTo("Novo");
        assertThat(novo.getNif()).isEqualTo("999888777");
    }

    @Test
    void restaurante_AddMenuItem_AdicionaProdutoAoMenu() {
        Produto produto = new Produto("Bolo", "De mel", 2.5, "Sobremesa");
        Menu menu = new Menu("Menu Teste", "desc", new ArrayList<>(), new ArrayList<>());
        restaurante.setMenu(menu);
        menu.getProdutos().add(produto);
        produto.getMenus().add(menu);

        assertThat(restaurante.getMenu().getProdutos()).contains(produto);
        assertThat(produto.getMenus()).contains(menu);
    }

    // ─── Entregador ───────────────────────────────────────────────────────────

    @Test
    void entregador_DisponibilidadeInicial_True() {
        assertThat(entregador.isDisponivel()).isTrue();
        assertThat(entregador.getVeiculo()).isEqualTo("carro");
        assertThat(entregador.getZonaAtuacao()).isEqualTo("Funchal");
    }

    @Test
    void entregador_PodeReceberEntrega_DisponivelEAtivo() {
        assertThat(entregador.podeReceberEntrega()).isTrue();
    }

    @Test
    void entregador_PodeReceberEntrega_IndisponivelRetornaFalse() {
        entregador.setDisponivel(false);
        assertThat(entregador.podeReceberEntrega()).isFalse();
    }

    @Test
    void entregador_PodeReceberEntrega_InativoRetornaFalse() {
        entregador.desativar();
        assertThat(entregador.podeReceberEntrega()).isFalse();
    }

    @Test
    void entregador_AddEntrega_AssociaEntregaAoEntregador() {
        Entrega entrega = new Entrega(pedido, entregador);
        entregador.setEntregas(new java.util.ArrayList<>());
        entregador.addEntrega(entrega);

        assertThat(entregador.getEntregas()).contains(entrega);
        assertThat(entrega.getEntregador()).isEqualTo(entregador);
    }

    @Test
    void entregador_SettersAtualizam() {
        entregador.setVeiculo("mota");
        entregador.setZonaAtuacao("Lisboa");

        assertThat(entregador.getVeiculo()).isEqualTo("mota");
        assertThat(entregador.getZonaAtuacao()).isEqualTo("Lisboa");
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @Test
    void admin_AddRestaurante_AssociaRestauranteAoAdmin() {
        Admin admin = new Admin("admin@t.com", "Admin", "pass");
        Restaurante r = new Restaurante("Lugar", endereco, "111222333");
        admin.addRestaurante(r);

        assertThat(admin.getRestaurantes()).contains(r);
        assertThat(r.getAdmin()).isEqualTo(admin);
    }

    // ─── User — ativar ────────────────────────────────────────────────────────

    @Test
    void user_Ativar_SetaAtivoTrue() {
        cliente.desativar();
        assertThat(cliente.isAtivo()).isFalse();
        cliente.ativar();
        assertThat(cliente.isAtivo()).isTrue();
    }

    @Test
    void user_SettersAtualizam() {
        cliente.setNome("Beatriz");
        cliente.setEmail("b@t.com");
        cliente.setPassword("newpass");

        assertThat(cliente.getNome()).isEqualTo("Beatriz");
        assertThat(cliente.getEmail()).isEqualTo("b@t.com");
        assertThat(cliente.getPassword()).isEqualTo("newpass");
        assertThat(cliente.getDataRegisto()).isNotNull();
    }

    // ─── Cliente ─────────────────────────────────────────────────────────────

    @Test
    void cliente_GetMorada_Correto() {
        assertThat(cliente.getMoradas()).contains(endereco);
    }

    @Test
    void cliente_AddPedido_AssociaPedidoAoCliente() {
        cliente.addPedido(pedido);
        assertThat(cliente.getPedidos()).contains(pedido);
        assertThat(pedido.getCliente()).isEqualTo(cliente);
    }

    // ─── ProdutoPedido ────────────────────────────────────────────────────────

    @Test
    void produtoPedido_GettersCorretos() {
        Produto produto = new Produto("Risotto", "Cogumelos", 9.5, "Prato Principal");
        ProdutoPedido item = new ProdutoPedido(produto, 3);

        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getPrecoCompra()).isEqualTo(9.5);
        assertThat(item.getProduto()).isEqualTo(produto);
    }

    @Test
    void produtoPedido_Setters_AtualizamValores() {
        Produto produto = new Produto("Sopa", "do Dia", 3.0, "Entrada");
        ProdutoPedido item = new ProdutoPedido(produto, 1);

        item.setQuantity(5);
        item.setPedido(pedido);

        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getPedido()).isEqualTo(pedido);
    }

    // ─── Pagamento subclasses ──────────────────────────────────────────────────

    @Test
    void multibanco_GettersCorretos() {
        Multibanco mb = new Multibanco(pedido, 30.0, "923 222 111", "MasterCard");
        assertThat(mb.getReferencia()).isEqualTo("923 222 111");
        assertThat(mb.getBandeira()).isEqualTo("MasterCard");

        mb.setReferencia("ABC-999");
        assertThat(mb.getReferencia()).isEqualTo("ABC-999");
    }

    @Test
    void mbway_GettersCorretos() {
        MBWay mbway = new MBWay(pedido, 12.0, "913000111");
        assertThat(mbway.getTelemovel()).isEqualTo("913000111");
        assertThat(mbway.getPreco()).isEqualTo(12.0);

        mbway.setTelemovel("923999888");
        assertThat(mbway.getTelemovel()).isEqualTo("923999888");
    }

    @Test
    void dinheiro_CriacaoEProcessamento() {
        Dinheiro din = new Dinheiro(pedido, 8.0, 2.5);
        assertThat(din.getStatus()).isEqualTo(PagamentoStatus.PENDING);
        assertThat(din.getTroco()).isEqualTo(2.5);
        din.processar();
        assertThat(din.getStatus()).isEqualTo(PagamentoStatus.COMPLETED);
    }
}
