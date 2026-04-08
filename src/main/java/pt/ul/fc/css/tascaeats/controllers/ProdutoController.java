package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.services.*;
import java.util.List;

/**
 * Controller REST para gestão de Produtos, aninhado ao recurso de Restaurante.
 * Utiliza DTOs para garantir o desacoplamento entre a API e a camada de persistência.
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
     * @param request DTO com os dados do novo produto.
     * @return ProdutoResponse com os dados do produto criado e status 201.
     */
    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@PathVariable Long restauranteId, @Valid @RequestBody CriarProdutoRequest request) {
        
        Produto produtoParaCriar = new Produto(
            request.getNome(),
            request.getDescricao(),
            request.getPreco(),
            null
        );
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
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(ProdutoResponse.from(produto));
    }

    /**
     * Atualiza os dados de um produto.
     *
     * @param id ID do produto a editar.
     * @param request DTO com os novos dados.
     * @return ProdutoResponse atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @RequestBody CriarProdutoRequest request) {
        Produto produtoAtualizado = produtoService.atualizarProduto(
            id,
            request.getNome(),
            request.getDescricao(),
            request.getPreco()
        );
        return ResponseEntity.ok(ProdutoResponse.from(produtoAtualizado));
    }

    /**
     * Altera a disponibilidade de um produto sem o remover do menu.
     *
     * @param id ID do produto.
     * @param disponivel Novo estado de disponibilidade.
     * @return 204 No Content.
     */
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> alternarDisponibilidade(@PathVariable Long id, @RequestParam boolean disponivel) {
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
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }
}