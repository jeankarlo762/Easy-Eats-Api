package com.easy.eats.empresa.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.easy.eats.empresa.model.model.Empresa;
import com.easy.eats.empresa.repository.EmpresaRepository;

@Service
public class EmpresaService {
    private final EmpresaRepository repository;

    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    public List<Empresa> listarTodos() {
        return repository.findAll();
    }

    public Empresa buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
    }

    public Empresa salvar(Empresa empresa) {
        return repository.save(empresa);
    }

    public Empresa atualizar(Integer id, Empresa empresa) {

        Empresa empresaExistente = buscarPorId(id);

        empresaExistente.setNome(empresa.getNome());
        empresaExistente.setCnpj(empresa.getCnpj());
        empresaExistente.setEmail(empresa.getEmail());
        empresaExistente.setTelefone(empresa.getTelefone());
        empresaExistente.setFlAtivo(empresa.getFlAtivo());
        empresaExistente.setDtCriacao(empresa.getDtCriacao());
        empresaExistente.setDtAlteracao(empresa.getDtAlteracao());

        return repository.save(empresaExistente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
