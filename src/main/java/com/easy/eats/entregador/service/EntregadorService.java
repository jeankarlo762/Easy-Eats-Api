package com.easy.eats.entregador.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.entregador.model.Entregador;
import com.easy.eats.entregador.repository.EntregadorRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class EntregadorService {

    @Autowired
    private EntregadorRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Entregador> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    public Optional<Entregador> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public Entregador criar(Entregador entregador) {
        entregador.setId(null);
        entregador.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        if (entregador.getFlAtivo() == null) {
            entregador.setFlAtivo(true);
        }
        return repository.save(entregador);
    }

    public Entregador atualizar(Integer id, Entregador entregadorAtualizado) {
        Entregador existente = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entregador não encontrado"));

        existente.setNome(entregadorAtualizado.getNome());
        existente.setTelefone(entregadorAtualizado.getTelefone());
        existente.setFlAtivo(entregadorAtualizado.getFlAtivo());

        return repository.save(existente);
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }
}
