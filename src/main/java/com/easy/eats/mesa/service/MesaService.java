package com.easy.eats.mesa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;

@Service
public class MesaService {

    @Autowired
    MesaRepository repository;

    public Mesa salvar(Mesa mesa) {
        return repository.save(mesa);
    }

    public List<Mesa> listarTodos() {
        return repository.findAll();
    }

    public Optional<Mesa> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
