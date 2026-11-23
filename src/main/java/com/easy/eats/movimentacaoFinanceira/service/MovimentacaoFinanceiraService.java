package com.easy.eats.movimentacaoFinanceira.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.movimentacaoFinanceira.model.MovimentacaoFinanceira;
import com.easy.eats.movimentacaoFinanceira.repository.MovimentacaoFinanceiraRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class MovimentacaoFinanceiraService {

    @Autowired
    MovimentacaoFinanceiraRepository repository;

    @Autowired
    EmpresaRepository empresaRepository;

    public MovimentacaoFinanceira criar(MovimentacaoFinanceira movimentacao) {
        movimentacao.setId(null);
        movimentacao.setEmpresa(empresaRepository.getReferenceById(SecurityUtils.getEmpresaId()));
        return repository.save(movimentacao);
    }

    public MovimentacaoFinanceira salvar(MovimentacaoFinanceira movimentacao) {
        return repository.save(movimentacao);
    }

    public List<MovimentacaoFinanceira> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());
    }

    /** Lançamentos da empresa dentro do período [inicio, fim], mais recentes primeiro. */
    public List<MovimentacaoFinanceira> listarPorPeriodo(java.time.LocalDateTime inicio, java.time.LocalDateTime fim) {
        return repository.findAllByEmpresaIdAndDataMovimentacaoBetweenOrderByDataMovimentacaoDesc(
                SecurityUtils.getEmpresaId(), inicio, fim);
    }

    public Optional<MovimentacaoFinanceira> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }
}
