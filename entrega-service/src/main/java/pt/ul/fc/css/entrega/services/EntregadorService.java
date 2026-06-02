package pt.ul.fc.css.entrega.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.css.entrega.entities.Entregador;
import pt.ul.fc.css.entrega.repositories.EntregadorRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EntregadorService {

    private final EntregadorRepository entregadorRepository;

    public EntregadorService(EntregadorRepository entregadorRepository) {
        this.entregadorRepository = entregadorRepository;
    }

    public List<Entregador> listar(String zona, Boolean disponivel) {
        if (zona != null && disponivel != null) {
            if (disponivel) {
                return entregadorRepository.findByZonaAtuacaoIgnoreCaseAndDisponivelTrue(zona);
            }
            return entregadorRepository.findByZonaAtuacao(zona).stream()
                    .filter(e -> !e.isDisponivel())
                    .toList();
        }
        if (zona != null) {
            return entregadorRepository.findByZonaAtuacao(zona);
        }
        if (disponivel != null) {
            return disponivel
                    ? entregadorRepository.findByDisponivelTrue()
                    : entregadorRepository.findAll().stream().filter(e -> !e.isDisponivel()).toList();
        }
        return entregadorRepository.findAll();
    }

    public Entregador buscarPorId(Long id) {
        return entregadorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Entregador não encontrado: " + id));
    }

    @Transactional
    public Entregador criar(Entregador entregador) {
        if (entregadorRepository.findByEmail(entregador.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um entregador com o email: " + entregador.getEmail());
        }
        return entregadorRepository.save(entregador);
    }

    @Transactional
    public Entregador atualizar(Long id, Entregador dados) {
        Entregador existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setVeiculo(dados.getVeiculo());
        existente.setZonaAtuacao(dados.getZonaAtuacao());
        existente.setDisponivel(dados.isDisponivel());
        return entregadorRepository.save(existente);
    }

    @Transactional
    public void atualizarDisponibilidade(Long id, boolean disponivel) {
        Entregador entregador = buscarPorId(id);
        entregador.setDisponivel(disponivel);
        entregadorRepository.save(entregador);
    }

    @Transactional
    public void remover(Long id) {
        if (!entregadorRepository.existsById(id)) {
            throw new NoSuchElementException("Entregador não encontrado: " + id);
        }
        entregadorRepository.deleteById(id);
    }
}
