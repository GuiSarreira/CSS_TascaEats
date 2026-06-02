package pt.ul.fc.css.entrega.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pt.ul.fc.css.entrega.entities.Entregador;
import pt.ul.fc.css.entrega.repositories.EntregadorRepository;

/**
 * Inicializa dados de teste na startup do entrega-service.
 * Idempotente: usa o email como chave única, não insere duplicados.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EntregadorRepository entregadorRepository;

    public DataInitializer(EntregadorRepository entregadorRepository) {
        this.entregadorRepository = entregadorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedEntregador("Bruno Silva", "bruno@entrega.pt", "Mota", "Lisboa");
        seedEntregador("Ana Costa", "ana@entrega.pt", "Bicicleta", "Porto");
        seedEntregador("Carlos Matos", "carlos@entrega.pt", "Carro", "Lisboa");
        seedEntregador("Filipa Nunes", "filipa@entrega.pt", "Mota", "leiria");
        seedEntregador("Ricardo Alves", "ricardo@entrega.pt", "Carro", "Braga");
        seedEntregador("Sofia Martins", "sofia@entrega.pt", "Bicicleta", "Faro");
        log.info("DataInitializer: entregadores de teste verificados/inseridos.");
    }

    private void seedEntregador(String nome, String email, String veiculo, String zonaAtuacao) {
        entregadorRepository.findByEmail(email).ifPresentOrElse(
            e -> {
                if (!e.isDisponivel()) {
                    e.setDisponivel(true);
                    entregadorRepository.save(e);
                    log.info("DataInitializer: entregador '{}' reposto como disponível.", nome);
                }
            },
            () -> {
                Entregador e = new Entregador(nome, email, veiculo, zonaAtuacao);
                entregadorRepository.save(e);
                log.info("DataInitializer: entregador '{}' criado na zona {}.", nome, zonaAtuacao);
            }
        );
    }
}
