package com.easy.eats.despesa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.despesa.model.Despesa;
import com.easy.eats.despesa.repository.DespesaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class DespesaService {

    @Autowired
    private DespesaRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Despesa> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaIdOrderByDtDespesaDesc(SecurityUtils.getEmpresaId());
    }

    public Optional<Despesa> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public Despesa criar(Despesa despesa) {
        Integer empresaId = SecurityUtils.getEmpresaId();
        despesa.setId(null);
        despesa.setEmpresa(empresaRepository.getReferenceById(empresaId));
        return repository.save(despesa);
    }

    public Despesa salvar(Despesa despesa) {
        return repository.save(despesa);
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }
}
