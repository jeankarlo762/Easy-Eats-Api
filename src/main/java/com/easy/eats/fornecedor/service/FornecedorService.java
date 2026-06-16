package com.easy.eats.fornecedor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.easy.eats.fornecedor.model.Fornecedor;
import com.easy.eats.fornecedor.repository.FornecedorRepository;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Fornecedor buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
    }

    public Fornecedor salvar(Fornecedor fornecedor) {

        fornecedor.setDtCriacao(LocalDateTime.now());

        return repository.save(fornecedor);
    }

    public Fornecedor atualizar(Integer id, Fornecedor fornecedor) {

        Fornecedor existente = buscarPorId(id);

        existente.setNome(fornecedor.getNome());
        existente.setCnpj(fornecedor.getCnpj());
        existente.setTelefone(fornecedor.getTelefone());
        existente.setEmail(fornecedor.getEmail());
        existente.setFlAtivo(fornecedor.getFlAtivo());

        return repository.save(existente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}