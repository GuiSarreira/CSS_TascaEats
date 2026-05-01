package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.services.ProdutoService;

/**
 * Controller MVC (Thymeleaf) para gestão de Produtos.
 * 
 * Fornece interfaces para listagem de produtos, busca com filtros,
 * e análise de negócio (ex: produto mais pedido de um restaurante).
 */
@Controller
@RequestMapping("/produtos")
public class WebProdutoController {

    private final ProdutoService produtoService;

    public WebProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // ─── Query 2: Produto mais pedido de um restaurante ────────────────────

    /**
     * Formulário para consultar o produto mais pedido de um restaurante.
     * Query de negócio: "Qual é o item mais pedido de um restaurante?"
     */
    @GetMapping("/mais-pedido")
    public String produtoMaisPedidoForm(Model model) {
        model.addAttribute("titulo", "Produto Mais Pedido");
        model.addAttribute("descricao", "Qual é o produto mais vezes encomendado de um restaurante?");
        return "produtos/mais-pedido";
    }

    /**
     * Resultado — produto mais pedido de um restaurante.
     */
    @PostMapping("/mais-pedido")
    public String produtoMaisPedidoResultado(
            @RequestParam Long restauranteId,
            Model model) {
        var resultado = produtoService.produtoMaisPedidoDoRestaurante(restauranteId);

        if (resultado.isPresent()) {
            Object[] dados = resultado.get();
            model.addAttribute("produto", dados[0]);
            model.addAttribute("totalVezesPedido", dados[1]);
            model.addAttribute("encontrado", true);
        } else {
            model.addAttribute("encontrado", false);
            model.addAttribute("mensagem", "Sem pedidos para este restaurante");
        }

        model.addAttribute("restauranteId", restauranteId);
        model.addAttribute("titulo", "Produto Mais Pedido");
        return "produtos/mais-pedido-resultado";
    }

    // ─── Query 4: Produtos mais vendidos da plataforma ─────────────────────

    /**
     * FASE 1 — Query 4: Produtos mais vendidos da plataforma
     * Página que lista todos os produtos ordenados pelo total vendido.
     */
    @GetMapping("/mais-vendidos")
    public String maisProdutosVendidos(Model model) {
        var resultados = produtoService.produtosMaisVendidos();
        model.addAttribute("resultados", resultados);
        model.addAttribute("titulo", "Produtos Mais Vendidos");
        model.addAttribute("descricao", "Produtos ordenados pela quantidade total vendida");
        return "produtos/mais-vendidos";
    }
}
