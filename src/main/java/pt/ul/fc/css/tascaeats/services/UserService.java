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

    public UserService(UserRepository userRepository,
                       ClienteRepository clienteRepository,
                       AdminRepository adminRepository,
                       EntregadorRepository entregadorRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.adminRepository = adminRepository;
        this.entregadorRepository = entregadorRepository;
    }

    /**
     * Regista um novo Cliente no sistema.
     * Regra: Email deve ser único.
     */
    @Transactional
    public Cliente registarCliente(String email, String nome, String password, String morada) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já registado: " + email);
        }
        return clienteRepository.save(new Cliente(email, nome, password, morada));
    }

    /**
     * Regista um novo Administrador no sistema.
     * Regra: Email deve ser único.
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
     * Regra: Email deve ser único.
     */
    @Transactional
    public Entregador registarEntregador(String email, String nome, String password, String veiculo, String zonaAtuacao) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já registado: " + email);
        }
        return entregadorRepository.save(new Entregador(email, nome, password, veiculo, zonaAtuacao));
    }

    /**
     * Busca um utilizador pelo seu ID.
     */
    public User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com ID: " + id));
    }

    /**
     * Busca um utilizador pelo seu email.
     */
    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Lista todos os utilizadores ativos.
     */
    public List<User> listarTodosAtivos() {
        return userRepository.findByAtivoTrue();
    }

    /**
     * Lista entregadores disponíveis para novas entregas.
     */
    public List<Entregador> listarEntregadoresDisponiveis() {
        return entregadorRepository.findByDisponivelTrueAndAtivoTrue();
    }

    /**
     * Atualiza os dados de um utilizador existente.
     */
    @Transactional
    public User atualizarUser(Long id, String novoNome, String novaPassword, String novaMorada) {
        User user = buscarPorId(id);
        if (novoNome != null && !novoNome.isBlank()) user.setNome(novoNome);
        if (novaPassword != null && !novaPassword.isBlank()) user.setPassword(novaPassword);
        if (user instanceof Cliente && novaMorada != null && !novaMorada.isBlank()) {
            ((Cliente) user).setMorada(novaMorada);
        }
        return userRepository.save(user);
    }

    /**
     * Remove (soft-delete) um utilizador do sistema.
     * Regra: Utilizador com pedidos ativos ou entregas pendentes não pode ser removido.
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
                    .anyMatch(e -> e.getStatus() == EntregaStatus.ATRIBUIDA || e.getStatus() == EntregaStatus.A_CAMINHO);
            if (temEntregasAtivas) {
                throw new IllegalStateException("Não é possível remover entregador com entregas ativas.");
            }
        }

        user.desativar();
        userRepository.save(user);
    }

    /**
     * Reativa um utilizador previamente desativado.
     */
    @Transactional
    public User reativarUser(Long id) {
        User user = buscarPorId(id);
        user.ativar();
        return userRepository.save(user);
    }

    /**
     * Identifica clientes que se registaram mas ainda não realizaram nenhuma compra.
     */
    public List<Cliente> buscarClientesSemCompras() {
        return clienteRepository.findClientesSemCompras();
    }
}