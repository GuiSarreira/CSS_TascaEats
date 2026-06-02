package pt.ul.fc.css.tascaeats.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.dto.EntregaAtribuidaEvent;
import pt.ul.fc.css.tascaeats.entities.Pedido;
import pt.ul.fc.css.tascaeats.entities.PedidoStatus;
import pt.ul.fc.css.tascaeats.repositories.PedidoRepository;

@Component
public class EntregaAtribuidaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EntregaAtribuidaConsumer.class);
    private final ObjectMapper objectMapper;
    private final PedidoRepository pedidoRepository;

    public EntregaAtribuidaConsumer(ObjectMapper objectMapper, PedidoRepository pedidoRepository) {
        this.objectMapper = objectMapper;
        this.pedidoRepository = pedidoRepository;
    }

    @KafkaListener(topics = "entrega.atribuida", groupId = "monolito")
    @Transactional
    public void consumirEntregaAtribuida(String message) {
        try {
            EntregaAtribuidaEvent event = objectMapper.readValue(message, EntregaAtribuidaEvent.class);
            Pedido pedido = pedidoRepository.findById(event.getPedidoId()).orElse(null);
            if (pedido != null && pedido.getStatus() == PedidoStatus.PAID) {
                pedido.setStatus(PedidoStatus.IN_DELIVERY);
                pedidoRepository.save(pedido);
                logger.info("Pedido {} atualizado para IN_DELIVERY após atribuição de entrega", event.getPedidoId());
            } else {
                logger.warn("Pedido {} não encontrado ou não está PAID para receber entrega", event.getPedidoId());
            }
        } catch (Exception e) {
            logger.error("Erro ao processar evento entrega.atribuida", e);
        }
    }
}
