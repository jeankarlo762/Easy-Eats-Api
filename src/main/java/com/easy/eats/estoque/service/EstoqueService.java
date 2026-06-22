package com.easy.eats.estoque.service;

import org.springframework.stereotype.Service;

import com.easy.eats.estoque.bst.ArvoreInsumos;
import com.easy.eats.estoque.model.Insumo;

@Service
public class EstoqueService {

    private final ArvoreInsumos arvore = new ArvoreInsumos();

    public void cadastrarInsumo(Insumo insumo) {
        arvore.inserir(insumo);
    }

    public Insumo buscarInsumo(String nome) {
        return arvore.buscar(nome);
    }
}

