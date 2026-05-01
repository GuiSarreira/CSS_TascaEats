package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.services.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para gestão de Produtos, aninhado ao recurso de Restaurante.
 * Utiliza DTOs para garantir o desacoplamento entre a API e a camada de
 * persistência.
 */
@RestController
@RequestMapping("/api/restaurantes/{restauranteId}/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Cria um novo produto no menu de um restaurante.
     *
     * @param restauranteId ID do restaurante pai.
     * @param request       DTO com os dados do novo produto.
     * @return ProdutoResponse com os dados do produto criado e status 201.
     */
    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@PathVariable Long restauranteId,
            @Valid @RequestBody CriarProdutoRequest request) {

        Produto produtoParaCriar = new Produto(
                request.getNome(),
                request.getDescricao(),
                request.getPreco(),
                new java.util.ArrayList<>(),
                request.getCategoria());
        produtoParaCriar.setDisponivel(request.isDisponivel());

        Produto novoProduto = produtoService.criarProduto(restauranteId, produtoParaCriar);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.from(novoProduto));
    }

    /**
     * Lista o menu ativo de um restaurante.
     *
     * @param restauranteId ID do restaurante.
     * @return Lista de DTOs ProdutoResponse.
     */
    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarMenu(@PathVariable Long restauranteId) {
        List<ProdutoResponse> menu = produtoService.listarMenuDoRestaurante(restauranteId)
                .stream()
                .map(ProdutoResponse::from)
                .toList();

        return ResponseEntity.ok(menu);
    }

    /**
     * Procura um produto específico pelo seu ID.
     *
     * @param id ID do produto.
     * @return ProdutoResponse correspondente.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long restauranteId, @PathVariable Long id) {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(ProdutoResponse.from(produto));
    }

    /**
     * Atualiza os dados de um produto.
     *
     * @param id      ID do produto a editar.
     * @param request DTO com os novos dados.
     * @return ProdutoResponse atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long restauranteId, @PathVariable Long id,
            @RequestBody CriarProdutoRequest request) {
        Produto produtoAtualizado = produtoService.atualizarProduto(
                id,
                request.getNome(),
                request.getDescricao(),
                request.getPreco());
        return ResponseEntity.ok(ProdutoResponse.from(produtoAtualizado));
    }

    /**
     * Altera a disponibilidade de um produto sem o remover do menu.
     *
     * @param id         ID do produto.
     * @param disponivel Novo estado de disponibilidade.
     * @return 204 No Content.
     */
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> alternarDisponibilidade(@PathVariable Long restauranteId, @PathVariable Long id,
            @RequestParam boolean disponivel) {
        produtoService.alternarDisponibilidade(id, disponivel);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove um produto do menu (Soft-Delete).
     *
     * @param id ID do produto.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long restauranteId, @PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Filtra produtos com múltiplos critérios avançados (endpoint global, não aninhado).
     * 
     * Filtros suportados (todos opcionais):
     * - nome: substring search (case-insensitive)
     * - precoMin: preço mínimo em euros
     * - precoMax: preço máximo em euros
     * - categoria: categoria exata (ex: "Entrada", "Prato Principal")
     * - disponivel: boolean (true/false); requer admin/entregador (controlo de acesso)
     * - minPopularidade: número mínimo de vezes que o produto foi pedido
     * - dataInicio: data/hora inicial para filtro de popularidade (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * - dataFim: data/hora final para filtro de popularidade (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     *
     * @param nome Parte do nome a procurar (opcional)
     * @param precoMin Preço mínimo (opcional)
     * @param precoMax Preço máximo (opcional)
     * @param categoria Categoria exata (opcional)
     * @param disponivel Filtro de disponibilidade (opcional; apenas admin/entregador devem usar)
     * @param minPopularidade Número mínimo de vezes pedido (opcional)
     * @param dataInicio Data/hora inicial (opcional)
     * @param dataFim Data/hora final (opcional)
     * @return Lista de ProdutoResponse que satisfazem os critérios
     */
    @GetMapping("/filtros")
    public ResponseEntity<List<ProdutoResponse>> filtrar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double precoMin,
            @RequestParam(required = false) Double precoMax,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean disponivel,
            @RequestParam(required = false) Integer minPopularidade,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim) {

        List<ProdutoResponse> produtos = produtoService.filtrarProdutos(
                nome, precoMin, precoMax, categoria, disponivel,
                minPopularidade, dataInicio, dataFim)
                .stream()
                .map(ProdutoResponse::from)
                .toList();

        return ResponseEntity.ok(produtos);
    }
}