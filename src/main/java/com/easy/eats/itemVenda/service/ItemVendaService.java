package com.easy.eats.itemVenda.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.repository.ItemVendaRepository;

@Service
public class ItemVendaService {

    @Autowired
    ItemVendaRepository repository;

    public ItemVenda salvar(ItemVenda venda) {
        return repository.save(venda);
    }

    public List<ItemVenda> listarTodos() {
        return repository.findAll();
    }

    public Optional<ItemVenda> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
