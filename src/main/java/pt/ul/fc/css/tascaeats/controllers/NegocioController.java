package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.css.tascaeats.dto.RestauranteResponse;
import pt.ul.fc.css.tascaeats.dto.UserResponse;
import pt.ul.fc.css.tascaeats.services.EntregaService;
import pt.ul.fc.css.tascaeats.services.MenuService;
import pt.ul.fc.css.tascaeats.services.PagamentoService;

/**
 * Controller REST para queries de negócio analíticas.
 *
 * Expõe os endpoints correspondentes às queries de negócio definidas no
 * enunciado da Fase 2 — queries que agregam ou calculam métricas sobre
 * o estado do sistema.
 *
 * Endpoints:
 * GET /api/negocio/pagamentos/troco/media — Query 1: média do troco (Dinheiro)
 * GET /api/negocio/restaurantes/{id}/melhor-entregador — Query 3: entregador com
 * mais entregas
 * GET /api/negocio/menus/{id}/restaurante-popular — Query 4: restaurante mais
 * popular da franquia
 */
@RestController
@RequestMapping("/api/negocio")
public class NegocioController {

    private final PagamentoService pagamentoService;
    private final EntregaService entregaService;
    private final MenuService menuService;

    public NegocioController(PagamentoService pagamentoService,
            EntregaService entregaService,
            MenuService menuService) {
        this.pagamentoService = pagamentoService;
        this.entregaService = entregaService;
        this.menuService = menuService;
    }

    /**
     * Query 1 — Qual é a média do troco nos pagamentos a dinheiro concluídos?
     *
     * @return 200 com o valor médio do troco, ou 204 se não houver pagamentos a
     *         dinheiro
     */
    @GetMapping("/pagamentos/troco/media")
    public ResponseEntity<Double> mediaTroco() {
        Double media = pagamentoService.calcularMediaTroco();
        if (media == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(media);
    }

    /**
     * Query 3 — Qual o entregador com mais entregas concluídas para um restaurante
     * específico?
     *
     * @param restauranteId ID do restaurante
     * @return 200 com o entregador, ou 204 se não houver entregas concluídas para
     *         esse restaurante
     */
    @GetMapping("/restaurantes/{restauranteId}/melhor-entregador")
    public ResponseEntity<UserResponse> melhorEntregadorPorRestaurante(
            @PathVariable Long restauranteId) {
        return entregaService.entregadorComMaisEntregasParaRestaurante(restauranteId)
                .map(e -> ResponseEntity.ok(UserResponse.from(e)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Query 4 — Qual o restaurante mais popular de uma franquia (menu partilhado)?
     * Popularidade medida pelo número de avaliações recebidas.
     *
     * @param menuId ID do menu partilhado (franquia)
     * @return 200 com o restaurante mais avaliado, ou 204 se não houver avaliações
     */
    @GetMapping("/menus/{menuId}/restaurante-popular")
    public ResponseEntity<RestauranteResponse> restaurantePopularDaFranquia(
            @PathVariable Long menuId) {
        return menuService.restauranteMaisPopularDoMenu(menuId)
                .map(r -> ResponseEntity.ok(RestauranteResponse.from(r)))
                .orElse(ResponseEntity.noContent().build());
    }
}
