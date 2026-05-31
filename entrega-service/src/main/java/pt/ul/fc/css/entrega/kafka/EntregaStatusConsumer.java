package pt.ul.fc.css.tascaeats.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.dto.EntregaStatusEvent;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.PedidoStatus;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;

@Component
public class EntregaStatusConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EntregaStatusConsumer.class);
    private final ObjectMapper objectMapper;
    private final PedidoRepository pedidoRepository;

    public EntregaStatusConsumer(ObjectMapper objectMapper, PedidoRepository pedidoRepository) {
        this.objectMapper = objectMapper;
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaListener(topics = "entrega.status.atualizada", groupId = "monolito")
    @Transactional
    public void consumirStatusEntrega(String message) {
        try {
            EntregaStatusEvent event = objectMapper.readValue(message, EntregaStatusEvent.class);
            Pedido pedido = pedidoRepository.findById(event.getPedidoId()).orElse(null);
            if (pedido == null) {
                logger.warn("Pedido {} não encontrado para atualização de status", event.getPedidoId());
                return;
            }

            switch (event.getStatus()) {
                case "A_CAMINHO":
                    // Já está IN_DELIVERY, podemos apenas logar ou manter
                    break;
                case "CONCLUIDA":
                    pedido.setStatus(PedidoStatus.DELIVERED);
                    pedidoRepository.save(pedido);
                    logger.info("Pedido {} marcado como DELIVERED", event.getPedidoId());
                    break;
                case "CANCELADA":
                    pedido.setStatus(PedidoStatus.CANCELLED);
                    pedidoRepository.save(pedido);
                    logger.info("Pedido {} cancelado devido a cancelamento da entrega", event.getPedidoId());
                    break;
                default:
                    logger.warn("Status desconhecido: {}", event.getStatus());
            }
        } catch (Exception e) {
            logger.error("Erro ao processar evento entrega.status.atualizada", e);
        }
    }
}
