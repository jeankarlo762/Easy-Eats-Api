package com.easy.eats.pagamento.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.pagamento.repository.PagamentoRepository;

@Service
public class PagamentoService {

    @Autowired
    PagamentoRepository repository;

    public Pagamento salvar(Pagamento pagamento) {
        return repository.save(pagamento);
    }

    public List<Pagamento> listarTodos() {
        return repository.findAll();
    }

    public Optional<Pagamento> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
