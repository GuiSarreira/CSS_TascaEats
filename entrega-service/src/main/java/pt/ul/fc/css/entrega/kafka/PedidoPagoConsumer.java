package pt.ul.fc.css.entrega.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.ul.fc.css.entrega.dto.PedidoPagoEvent;
import pt.ul.fc.css.entrega.services.EntregaService;

@Component
public class PedidoPagoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PedidoPagoConsumer.class);
    private final ObjectMapper objectMapper;
    private final EntregaService entregaService;

    public PedidoPagoConsumer(ObjectMapper objectMapper, EntregaService entregaService) {
        this.objectMapper = objectMapper;
        this.entregaService = entregaService;
    }

    @KafkaListener(topics = "pedido.pago", groupId = "entrega-service")
    public void consumirPedidoPago(String message) {
        try {
            PedidoPagoEvent event = objectMapper.readValue(message, PedidoPagoEvent.class);
            logger.info("Recebido evento pedido.pago: pedidoId={}, cidade={}", event.getPedidoId(), event.getCidade());
            entregaService.atribuirEntregadorAutomatico(
                    event.getPedidoId(),
                    event.getMoradaEntrega(),
                    event.getCidade()
            );
        } catch (Exception e) {
            logger.error("Erro ao processar evento pedido.pago", e);
        }
    }
}