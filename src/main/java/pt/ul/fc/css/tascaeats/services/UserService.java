package pt.ul.fc.css.tascaeats.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.tascaeats.entities.*;
import pt.ul.fc.css.tascaeats.repositories.*;

import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela gestão da lógica de negócio de Utilizadores.
 * Coordena operações de registo, atualização, consulta e remoção (soft-delete)
 * de Clientes, Administradores e Entregadores.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;
    private final AdminRepository adminRepository;
    private final EntregadorRepository entregadorRepository;

    /**
     * Construtor para injeção de dependências dos repositórios necessários.
     *
     * @param userRepository       repositório de utilizadores (base)
     * @param clienteRepository    repositório de clientes
     * @param adminRepository      repositório de administradores
     * @param entregadorRepository repositório de entregadores
     */
    public UserService(UserRepository userRepository, ClienteRepository clienteRepository,
            AdminRepository adminRepository, EntregadorRepository entregadorRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.adminRepository = adminRepository;
        this.entregadorRepository = entregadorRepository;
    }

    /**
     * Regista um novo Cliente no sistema.
     *
     * @param email    endereço de email único do novo cliente
     * @param nome     nome completo
     * @param password password (mock auth — não é cifrada)
     * @param morada   morada de entrega por omissão
     * @return o cliente criado e persistido
     * @throws IllegalArgumentException se o email já estiver registado
     */
    @Transactional
    public Cliente registarCliente(String email, String nome, String password, Endereco morada) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já registado: " + email);
        }
        return clienteRepository.save(new Cliente(email, nome, password, morada));
    }

    /**
     * Regista um novo Administrador no sistema.
     *
     * @param email    endereço de email único do novo administrador
     * @param nome     nome completo
     * @param password password (mock auth — não é cifrada)
     * @return o administrador criado e persistido
     * @throws IllegalArgumentException se o email já estiver registado
     */
    @Transactional
    public Admin registarAdmin(String email, String nome, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já registado: " + email);
        }
        return adminRepository.save(new Admin(email, nome, password));
    }

    /**
     * Regista um novo Entregador no sistema.
     *
     * @param email       endereço de email único do novo entregador
     * @param nome        nome completo
     * @param password    password (mock auth — não é cifrada)
     * @param veiculo     tipo de veículo utilizado (ex: moto, bicicleta, carro)
     * @param zonaAtuacao zona geográfica onde o entregador opera
     * @return o entregador criado e persistido, com {@code disponivel = true}
     * @throws IllegalArgumentException se o email já estiver registado
     */
    @Transactional
    public Entregador registarEntregador(String email, String nome, String password, String veiculo,
            String zonaAtuacao) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já registado: " + email);
        }
        return entregadorRepository.save(new Entregador(email, nome, password, veiculo, zonaAtuacao));
    }

    /**
     * Procura um utilizador pelo seu identificador único.
     *
     * @param id ID do utilizador
     * @return o utilizador encontrado (qualquer subtipo: {@link Cliente},
     *         {@link Admin}, {@link Entregador})
     * @throws RuntimeException se nenhum utilizador existir com o ID fornecido
     */
    public User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + id));
    }

    /**
     * Procura um utilizador pelo endereço de email.
     *
     * @param email email a pesquisar
     * @return {@link Optional} com o utilizador, ou vazio se não existir
     */
    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Lista todos os utilizadores com conta ativa ({@code ativo = true}).
     *
     * @return lista de utilizadores ativos (qualquer subtipo)
     */
    public List<User> listarTodosAtivos() {
        return userRepository.findByAtivoTrue();
    }

    /**
     * Lista entregadores com conta ativa e prontos para novas entregas.
     *
     * @return lista de entregadores com {@code disponivel = true} e
     *         {@code ativo = true}
     */
    public List<Entregador> listarEntregadoresDisponiveis() {
        return entregadorRepository.findByDisponivelTrueAndAtivoTrue();
    }

    /**
     * Atualiza os dados pessoais de um utilizador.
     *
     * Apenas os campos não nulos e não em branco são atualizados.
     * O campo {@code novaMorada} só é aplicado se o utilizador for um
     * {@link Cliente}.
     *
     * @param id           ID do utilizador a atualizar
     * @param novoNome     novo nome (ignorado se {@code null} ou em branco)
     * @param novaPassword nova password (ignorada se {@code null} ou em branco)
     * @param novaMorada   nova morada — apenas aplicada a utilizadores do tipo
     *                     {@link Cliente}
     * @return o utilizador atualizado e persistido
     * @throws RuntimeException se o utilizador não for encontrado
     */
    @Transactional
    public User atualizarUser(Long id, String novoNome, String novaPassword, Endereco novaMorada) {
        User user = buscarPorId(id);
        if (novoNome != null && !novoNome.isBlank())
            user.setNome(novoNome);
        if (novaPassword != null && !novaPassword.isBlank())
            user.setPassword(novaPassword);
        if (user instanceof Cliente && novaMorada != null) {
            ((Cliente) user).adicionarMorada(novaMorada);
        }
        return userRepository.save(user);
    }

    /**
     * Desativa (soft-delete) um utilizador, repondo {@code ativo = false}.
     *
     * Regras aplicadas:
     * - {@link Cliente} com pedidos nos estados ativos não pode ser removido.
     * - {@link Entregador} com entregas {@code ATRIBUIDA} ou {@code A_CAMINHO}
     * não pode ser removido.
     *
     * @param id ID do utilizador a desativar
     * @throws RuntimeException      se o utilizador não for encontrado
     * @throws IllegalStateException se o utilizador tiver pedidos ou entregas
     *                               ativos
     */
    @Transactional
    public void removerUser(Long id) {
        User user = buscarPorId(id);

        if (user instanceof Cliente) {
            boolean temPedidosAtivos = ((Cliente) user).getPedidos().stream()
                    .anyMatch(p -> p.getStatus() != PedidoStatus.DELIVERED && p.getStatus() != PedidoStatus.CANCELLED);
            if (temPedidosAtivos) {
                throw new IllegalStateException("Não é possível remover cliente com pedidos ativos.");
            }
        }

        if (user instanceof Entregador) {
            boolean temEntregasAtivas = ((Entregador) user).getEntregas().stream()
                    .anyMatch(
                            e -> e.getStatus() == EntregaStatus.ATRIBUIDA || e.getStatus() == EntregaStatus.A_CAMINHO);
            if (temEntregasAtivas) {
                throw new IllegalStateException("Não é possível remover entregador com entregas ativas.");
            }
        }

        user.desativar();
        userRepository.save(user);
    }

    /**
     * Reativa um utilizador previamente desativado, repondo {@code ativo = true}.
     *
     * @param id ID do utilizador a reativar
     * @return o utilizador reativado e persistido
     * @throws RuntimeException se o utilizador não for encontrado
     */
    @Transactional
    public User reativarUser(Long id) {
        User user = buscarPorId(id);
        user.ativar();
        return userRepository.save(user);
    }

    /**
     * Lista clientes registados que ainda não realizaram nenhuma compra.
     *
     * Responde à query de negócio: "Clientes registados sem compras."
     *
     * @return lista de clientes sem nenhum pedido associado
     */
    public List<Cliente> buscarClientesSemCompras() {
        return clienteRepository.findClientesSemCompras();
    }
}