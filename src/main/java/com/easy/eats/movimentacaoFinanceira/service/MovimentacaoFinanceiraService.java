package com.easy.eats.movimentacaoFinanceira.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.repository.MovimentacaoFinanceiraRepository;

@Service
public class MovimentacaoFinanceiraService {

    @Autowired
    MovimentacaoFinanceiraRepository repository;

    public MovimentacaoFinanceira salvar(MovimentacaoFinanceira movimentacao) {
        return repository.save(movimentacao);
    }

    public List<MovimentacaoFinanceira> listarTodos() {
        return repository.findAll();
    }

    public Optional<MovimentacaoFinanceira> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
