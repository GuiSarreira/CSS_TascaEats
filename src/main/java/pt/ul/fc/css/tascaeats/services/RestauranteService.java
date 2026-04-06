package pt.ul.fc.css.tascaeats.services;

import pt.ul.fc.css.tascaeats.repositories.*;
import pt.ul.fc.css.tascaeats.entities.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela gestão da lógica de negócio de Restaurantes.
 * Coordena operações de criação, atualização, abertura e remoção de estabelecimentos.
 */
@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    /**
     * Construtor para injeção de dependência do repositório.
     * @param restauranteRepository O repositório de dados de restaurante.
     */
    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    /**
     * Regra: Apenas um utilizador do tipo Admin pode criar um restaurante.
     * @param restaurante Objeto restaurante com os dados iniciais.
     * @param user Utilizador que tenta realizar a operação.
     * @return O Restaurante guardado na base de dados.
     * @throws SecurityException Se o utilizador não for um Administrador.
     * @throws IllegalArgumentException Se o NIF já estiver registado no sistema.
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
     * @param id O ID do restaurante a alterar.
     * @param abrir True para abrir o restaurante, False para fechar.
     * @throws RuntimeException Se o restaurante com o ID fornecido não existir.
     */
    @Transactional
    public void alterarEstadoAbertura(Long id, boolean abrir) {
        Restaurante restaurante = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));

        restaurante.setAberto(abrir);
        restauranteRepository.save(restaurante);
    }

    /**
     * Procura restaurantes por nome (pesquisa parcial).
     * @param nome Texto a pesquisar no nome do restaurante.
     * @return Lista de restaurantes que contêm o texto no nome.
     */
    public List<Restaurante> buscarPorNome(String nome) {
        return restauranteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Procura restaurantes por NIF.
     * @param nif Texto a pesquisar no NIF do restaurante.
     * @return Lista de restaurantes que contêm o texto no NIF.
     */
    public Optional<Restaurante> buscarPorNif(String nif) {
        return restauranteRepository.findByNif(nif);
    }

    /**
     * Procura restaurantes por cidade.
     * @param cidade Nome da cidade para filtrar.
     * @return Lista de restaurantes localizados na cidade.
     */
    public List<Restaurante> buscarPorCidade(String cidade) {
        return restauranteRepository.findByCidadeIgnoreCase(cidade);
    }

    /**
     * Lista todos os restaurantes registados no sistema.
     * @return Lista completa de restaurantes.
     */
    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    /**
     * Atualiza os dados de um restaurante existente.
     * @param id O ID do restaurante a editar.
     * @param novosDados Objeto com as novas informações (nome, morada, cidade).
     * @return O restaurante atualizado.
     * @throws RuntimeException Se não for encontrado nenhum restaurante com o ID indicado.
     */
    @Transactional
    public Restaurante atualizarRestaurante(Long id, Restaurante novosDados) {
        Restaurante restauranteExistente = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com ID: " + id));

        // O NIF nunca deve ser alterado após o registo
        restauranteExistente.setNome(novosDados.getNome());
        restauranteExistente.setMorada(novosDados.getMorada());
        restauranteExistente.setCidade(novosDados.getCidade());
        
        return restauranteRepository.save(restauranteExistente);
    }

    /**
     * Remove um restaurante do sistema se este não possuir histórico de pedidos.
     * @param id O ID do restaurante a remover.
     * @throws RuntimeException Se o restaurante não for encontrado.
     * @throws IllegalStateException Se o restaurante já tiver processado pedidos.
     */
    @Transactional
    public void removerRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));

        if (!restaurante.getPedidos().isEmpty()) {
            throw new IllegalStateException("Não é possível remover um restaurante que já processou pedidos. Considere desativá-lo.");
        }

        restauranteRepository.delete(restaurante);
    }

    /**
     * Procura um restaurante específico pelo seu identificador técnico (ID).
     * @param id O identificador único (Long) na base de dados.
     * @return O objeto Restaurante encontrado.
     * @throws RuntimeException Se o restaurante não for encontrado.
     */
    public Restaurante buscarPorId(Long id) {
        return restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado."));
    }
}