package pt.ul.fc.css.entrega.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EntregaEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EntregaEventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EntregaEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicarEntregaAtribuida(Long pedidoId, Long entregaId, Long entregadorId) {
        try {
            String json = objectMapper.writeValueAsString(
                    new EntregaAtribuidaEvent(pedidoId, entregaId, entregadorId));
            kafkaTemplate.send("entrega.atribuida", pedidoId.toString(), json);
            logger.info("Evento 'entrega.atribuida' publicado para pedido {}", pedidoId);
        } catch (Exception e) {
            logger.error("Erro ao serializar evento entrega.atribuida", e);
        }
    }

    public void publicarStatusAtualizado(Long pedidoId, String novoStatus, Long entregaId) {
        try {
            String json = objectMapper.writeValueAsString(
                    new EntregaStatusEvent(pedidoId, novoStatus, entregaId));
            kafkaTemplate.send("entrega.status.atualizada", pedidoId.toString(), json);
            logger.info("Evento 'entrega.status.atualizada' publicado para pedido {}: {}", pedidoId, novoStatus);
        } catch (Exception e) {
            logger.error("Erro ao serializar evento entrega.status.atualizada", e);
        }
    }

    // Classes internas para os eventos
    public static class EntregaAtribuidaEvent {
        public Long pedidoId;
        public Long entregaId;
        public Long entregadorId;
        public EntregaAtribuidaEvent() {}
        public EntregaAtribuidaEvent(Long pedidoId, Long entregaId, Long entregadorId) {
            this.pedidoId = pedidoId;
            this.entregaId = entregaId;
            this.entregadorId = entregadorId;
        }
    }

    public static class EntregaStatusEvent {
        public Long pedidoId;
        public String status;
        public Long entregaId;
        public EntregaStatusEvent() {}
        public EntregaStatusEvent(Long pedidoId, String status, Long entregaId) {
            this.pedidoId = pedidoId;
            this.status = status;
            this.entregaId = entregaId;
        }
    }
}