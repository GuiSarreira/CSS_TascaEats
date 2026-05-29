package pt.ul.fc.css.tascaeats.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuração do produtor Kafka no monólito.
 *
 * <p>Responsável por publicar eventos como {@code pedido.pago} para o broker Kafka,
 * que serão consumidos pelo microserviço {@code entrega-service}.
 *
 * <p>O endereço do broker é configurável via {@code spring.kafka.bootstrap-servers}
 * (definido no docker-compose.yml como variável de ambiente).
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Configura as propriedades do produtor Kafka.
     *
     * @return mapa de configuração do produtor
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Garantir entrega fiável dos eventos
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return props;
    }

    /**
     * Cria a fábrica de produtores Kafka com as configurações definidas.
     *
     * @return fábrica de produtores
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Cria o KafkaTemplate utilizado pelos serviços para publicar mensagens.
     *
     * <p>Exemplo de uso:
     * <pre>
     * kafkaTemplate.send("pedido.pago", pedidoId, jsonPayload);
     * </pre>
     *
     * @return template Kafka para envio de mensagens
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
