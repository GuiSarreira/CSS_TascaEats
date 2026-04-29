package pt.ul.fc.css.tascaeats.services;

import pt.ul.fc.css.tascaeats.repositories.*;
import pt.ul.fc.css.tascaeats.repositories.specs.RestauranteSpecifications;
import pt.ul.fc.css.tascaeats.entities.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela gestão da lógica de negócio de Restaurantes.
 * Coordena operações de criação, atualização, abertura e remoção de estabelecimentos.
 */
@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final UserRepository userRepository;

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     *
     * @param restauranteRepository repositório de restaurantes
     * @param userRepository        repositório de utilizadores (necessário para validar o admin)
     */
    public RestauranteService(RestauranteRepository restauranteRepository, UserRepository userRepository) {
        this.restauranteRepository = restauranteRepository;
        this.userRepository = userRepository;
    }

    /**
     * Cria um novo restaurante.
     *
     * Regra: apenas utilizadores do tipo {@link Admin} podem criar restaurantes.
     *
     * @param nome            nome do restaurante
     * @param morada          morada do restaurante
     * @param nif             NIF único do restaurante
     * @param tipoCozinha     tipo de cozinha (ex: Portuguesa, Italiana); pode ser {@code null}
     * @param horarioAbertura horário de abertura; pode ser {@code null}
     * @param horarioFecho    horário de fecho; pode ser {@code null}
     * @param adminId         ID do utilizador que cria o restaurante (deve ser Admin)
     * @return o restaurante persistido
     * @throws RuntimeException         se o utilizador indicado por {@code adminId} não existir
     * @throws SecurityException        se o utilizador não for um administrador
     * @throws IllegalArgumentException se já existir um restaurante com o mesmo NIF
     */
    @Transactional
    public Restaurante criarRestaurante(String nome, Endereco morada, String nif,
            String tipoCozinha, LocalTime horarioAbertura, LocalTime horarioFecho, Long adminId) {
        User user = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Utilizador não encontrado: id=" + adminId));

        if (!(user instanceof Admin)) {
            throw new SecurityException("Acesso Negado: Apenas administradores podem criar restaurantes.");
        }

        if (restauranteRepository.findByNif(nif).isPresent()) {
            throw new IllegalArgumentException("Já existe um restaurante registado com este NIF.");
        }

        Restaurante restaurante = new Restaurante(nome, morada, nif, tipoCozinha, horarioAbertura, horarioFecho);
        restaurante.setAdmin((Admin) user);

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
        return restauranteRepository.findByMoradaCidadeIgnoreCase(cidade);
    }

    /**
     * Lista todos os restaurantes registados no sistema.
     * @return Lista completa de restaurantes.
     */
    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    /**
     * Procura restaurantes por tipo de cozinha.
     * @param tipoCozinha tipo de cozinha a pesquisar (ex: "Portuguesa", "Italiana").
     * @return Lista de restaurantes com o tipo de cozinha indicado.
     */
    public List<Restaurante> buscarPorTipoCozinha(String tipoCozinha) {
        return restauranteRepository.findByTipoCozinhaIgnoreCase(tipoCozinha);
    }

    /**
     * Lista todos os restaurantes que estão atualmente abertos.
     * @return Lista de restaurantes abertos.
     */
    public List<Restaurante> listarAbertos() {
        return restauranteRepository.findByAbertoTrue();
    }

    /**
     * Atualiza os dados de um restaurante existente.
     *
     * Apenas o administrador dono do restaurante pode editá-lo.
     * O NIF não pode ser alterado após o registo.
     *
     * @param id      ID do restaurante a editar
     * @param nome    novo nome do restaurante
     * @param morada  nova morada do restaurante
     * @param cidade  nova cidade do restaurante
     * @param adminId ID do utilizador que solicita a edição (deve ser o dono)
     * @return o restaurante atualizado e persistido
     * @throws RuntimeException  se o restaurante não for encontrado
     * @throws SecurityException se o utilizador não for o admin dono do restaurante
     */
    @Transactional
    public Restaurante atualizarRestaurante(Long id, String nome, Endereco morada, Long adminId) {
        Restaurante restauranteExistente = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com ID: " + id));

        if (!restauranteExistente.getAdmin().getId().equals(adminId)) {
            throw new SecurityException("Não tem permissão para alterar este restaurante.");
        }

        // O NIF nunca deve ser alterado após o registo
        restauranteExistente.setNome(nome);
        restauranteExistente.setMorada(morada);

        return restauranteRepository.save(restauranteExistente);
    }

    /**
     * Remove um restaurante do sistema se este não possuir histórico de pedidos.
     *
     * Apenas o administrador dono do restaurante pode removê-lo.
     *
     * @param id      ID do restaurante a remover
     * @param adminId ID do utilizador que solicita a remoção
     * @throws RuntimeException      se o restaurante não for encontrado
     * @throws SecurityException     se o utilizador não for o admin dono do restaurante
     * @throws IllegalStateException se o restaurante já tiver pedidos associados
     */
    @Transactional
    public void removerRestaurante(Long id, Long adminId) {
        Restaurante restaurante = restauranteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));

        if (!restaurante.getAdmin().getId().equals(adminId)) {
            throw new SecurityException("Não tem permissão para remover este restaurante.");
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

    /**
     * Lista restaurantes aplicando filtros dinâmicos.
     *
     * @param nome             parte do nome (opcional)
     * @param tipoCozinha      tipo de cozinha exacto (opcional)
     * @param horario          horário a que deve estar aberto (opcional)
     * @param minPreco         preço médio mínimo dos produtos (opcional)
     * @param maxPreco         preço médio máximo dos produtos (opcional)
     * @param minAvaliacoes    número mínimo de avaliações (opcional)
     * @param cidade           cidade da morada (opcional)
     * @param minPedidos       número mínimo de pedidos realizados (opcional)
     * @return lista de restaurantes que satisfazem todos os critérios
     */
    public List<Restaurante> listarRestaurantesComFiltros(String nome, String tipoCozinha,
            LocalTime horario, Double minPreco, Double maxPreco, Integer minAvaliacoes,
            String cidade, Integer minPedidos) {
        Specification<Restaurante> spec = Specification
                .where(RestauranteSpecifications.comNome(nome))
                .and(RestauranteSpecifications.comTipoCozinha(tipoCozinha))
                .and(RestauranteSpecifications.abertoNoHorario(horario))
                .and(RestauranteSpecifications.precoMedioEntre(minPreco, maxPreco))
                .and(RestauranteSpecifications.comMinimoAvaliacoes(minAvaliacoes))
                .and(RestauranteSpecifications.comCidade(cidade))
                .and(RestauranteSpecifications.comMinimoPedidos(minPedidos));
        return restauranteRepository.findAll(spec);
    }
}