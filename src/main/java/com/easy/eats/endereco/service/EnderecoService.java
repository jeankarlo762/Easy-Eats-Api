package com.easy.eats.endereco.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.endereco.model.Endereco;
import com.easy.eats.endereco.repository.EnderecoRepository;

@Service
public class EnderecoService {

    @Autowired
    EnderecoRepository repository;

    public Endereco salvar(Endereco endereco) {
        return repository.save(endereco);
    }

    public List<Endereco> listarTodos() {
        return repository.findAll();
    }

    public Optional<Endereco> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
