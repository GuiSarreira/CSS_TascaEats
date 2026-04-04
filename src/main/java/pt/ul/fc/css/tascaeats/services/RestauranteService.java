package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.Admin;
import pt.ul.fc.css.tascaeats.entities.Restaurante;
import pt.ul.fc.css.tascaeats.entities.User;
import pt.ul.fc.css.tascaeats.repositories.RestauranteRepository;
import java.util.List;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    /**
     * Regra: Apenas um utilizador do tipo Admin pode criar um restaurante.
     */
    @Transactional
    public Restaurante criarRestaurante(Restaurante restaurante, User user) {
        if (!(user instanceof Admin)) {
            throw new SecurityException("Acesso Negado: Apenas administradores podem criar restaurantes.");
        }

        // Verificamos se já existe um restaurante com o mesmo NIF antes de salvar
        if (restauranteRepository.findByNif(restaurante.getNif()).isPresent()) {
            throw new IllegalArgumentException("Já existe um restaurante registado com este NIF.");
        }

        return restauranteRepository.save(restaurante);
    }

    /**
     * Altera o estado de funcionamento do restaurante.
     */
    @Transactional
    public void alterarEstadoAbertura(String nif, boolean abrir) {
        Restaurante restaurante = restauranteRepository.findByNif(nif)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o NIF: " + nif));

        restaurante.setAberto(abrir);
        restauranteRepository.save(restaurante);
    }

    /**
     * Procura restaurantes por nome (pesquisa parcial).
     */
    public List<Restaurante> buscarPorNome(String nome) {
        return restauranteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Procura restaurantes por cidade.
     */
    public List<Restaurante> buscarPorCidade(String cidade) {
        return restauranteRepository.findByCidadeIgnoreCase(cidade);
    }

    /**
     * Lista todos os restaurantes registados no sistema.
     * 
     * @return Lista completa de restaurantes.
     */
    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    /**
     * Atualiza os dados de um restaurante existente.
     * 
     * @param id         ID do restaurante a editar.
     * @param novosDados Objeto com as novas informações.
     * @return O restaurante atualizado.
     */
    @Transactional
    public Restaurante atualizarRestaurante(String nif, Restaurante novosDados) {
        Restaurante restauranteExistente = restauranteRepository.findByNif(nif)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com NIF: " + nif));

        // O NIF nunca deve ser alterado após o registo
        restauranteExistente.setNome(novosDados.getNome());
        restauranteExistente.setMorada(novosDados.getMorada());
        restauranteExistente.setCidade(novosDados.getCidade());

        return restauranteRepository.save(restauranteExistente);
    }

    /**
     * Remove um restaurante do sistema.
     * 
     * @param id ID do restaurante a remover.
     */
    @Transactional
    public void removerRestaurante(String nif) {
        Restaurante restaurante = restauranteRepository.findByNif(nif)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o NIF: " + nif));

        if (!restaurante.getPedidos().isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível remover um restaurante que já processou pedidos. Considere desativá-lo.");
        }

        restauranteRepository.delete(restaurante);
    }

    /**
     * Procura um restaurante específico pelo ID.
     */
    // public Restaurante buscarPorId(Long id) {
    // return restauranteRepository.findById(id)
    // .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));
    // }
}