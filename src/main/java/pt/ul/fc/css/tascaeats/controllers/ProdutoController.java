package pt.ul.fc.css.tascaeats.controllers;

import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller REST para gestão de Produtos, aninhado ao recurso de Restaurante.
 * Segue o padrão /api/restaurantes/{restauranteId}/produtos
 */
@RestController
@RequestMapping("/api/restaurantes/{restauranteId}/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Lista todos os produtos ativos (menu) de um restaurante específico.
     * @param restauranteId ID do restaurante pai.
     * @return Lista de produtos e status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Produto>> listarMenu(@PathVariable Long restauranteId) {
        return ResponseEntity.ok(produtoService.listarMenuDoRestaurante(restauranteId));
    }

    /**
     * Cria um novo produto dentro do menu de um restaurante.
     * @param restauranteId ID do restaurante onde o produto será inserido.
     * @param produto Dados do novo produto.
     * @return O produto criado e status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Produto> criar(@PathVariable Long restauranteId, @RequestBody Produto produto) {
        Produto novoProduto = produtoService.criarProduto(restauranteId, produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    /**
     * Procura um produto específico pelo ID dentro do contexto de um restaurante.
     * @param id ID do produto.
     * @return O produto encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    /**
     * Atualiza os dados de um produto existente.
     * @param id ID do produto a editar.
     * @param produto Novos dados do produto.
     * @return O produto atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        return ResponseEntity.ok(produtoService.atualizarProduto(id, produto));
    }

    /**
     * Remove logicamente um produto (Soft-Delete).
     * @param id ID do produto a remover.
     * @return Status 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pesquisa produtos por nome dentro do menu do restaurante indicado.
     * @param restauranteId ID do restaurante.
     * @param nome Nome ou parte do nome do produto.
     * @return Lista de produtos encontrados.
     */
    @GetMapping("/pesquisa")
    public ResponseEntity<List<Produto>> buscarNoMenu(@PathVariable Long restauranteId, @RequestParam String nome) {
        return ResponseEntity.ok(produtoService.buscarNoMenu(restauranteId, nome));
    }

    /**
     * Lista produtos do restaurante que não ultrapassam um determinado preço.
     * @param restauranteId ID do restaurante.
     * @param max Preço máximo permitido.
     * @return Lista de produtos económicos.
     */
    @GetMapping("/preco")
    public ResponseEntity<List<Produto>> listarPorPreco(@PathVariable Long restauranteId, @RequestParam(name = "max") Double max) {
        return ResponseEntity.ok(produtoService.listarPorPreco(restauranteId, max));
    }

    /**
     * Altera apenas a disponibilidade de um produto (ex: marcar como esgotado).
     * @param id ID do produto.
     * @param disponivel Novo estado de disponibilidade.
     * @return 204 No Content após sucesso.
     */
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> alternarDisponibilidade(@PathVariable Long id, @RequestParam boolean disponivel) {
        produtoService.alternarDisponibilidade(id, disponivel);
        return ResponseEntity.noContent().build();
    }
}